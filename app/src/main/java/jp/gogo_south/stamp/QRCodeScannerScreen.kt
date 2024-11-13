package jp.gogo_south.stamp


import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.ui.tooling.preview.Preview
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.res.stringResource


@Composable
fun QRCodeScannerScreen(
    onQRCodeScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uuid = UUIDManager.getUUID(context)
    var scannedQRCode by remember { mutableStateOf<String?>(null) }
    var showInvalidQRCodeDialog by remember { mutableStateOf(false) }
    var lastScannedTime by remember { mutableStateOf(0L) }  // 最後にスキャンした時間


    // UIレイアウト
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // カメラプレビューの配置
        CameraPreview { qrCode ->
            val currentTime = System.currentTimeMillis()
            // ここでデバウンス：1秒以内の再スキャンは無視
            if (currentTime - lastScannedTime > 1000) {
                lastScannedTime = currentTime
                if (isValidUrl(qrCode)) {
                    // UUIDをクエリパラメーターとして追加
//                    val urlWithUUID = "$qrCode${if (qrCode.contains("?")) "&" else "?"}uiid=$uuid"
//                    onQRCodeScanned(urlWithUUID)
                    onQRCodeScanned(qrCode)
                } else {
                    scannedQRCode = qrCode
                    showInvalidQRCodeDialog = true
                }
            }
        }

        // 透明度50%の黒色オーバーレイ
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ){
            // 透過エリアを作成するためのSpacer配置
            Spacer(
                modifier = Modifier
                    .size(200.dp) // QRコードスキャンエリアの大きさ
                    .align(Alignment.Center)
                    .background(Color.Transparent) // 透過させる
            )
        }
        // QRコードスキャンエリア
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)
                .border(2.dp, Color.White)

        )


        // テキスト
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Text(
                stringResource(id = R.string.scan_qr),
                color = Color.White,
                fontSize = 18.sp
            )
            Text(
                stringResource(id = R.string.trademark),
                color = Color.White,
                fontSize = 12.sp
            )


            // 戻るボタン
            Button(
                onClick = { onBack() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA500), // オレンジ色
                    contentColor = Color.White // 白色の文字
                ),
                shape = RoundedCornerShape(16.dp), // 角丸
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    stringResource(id = R.string.back),
                )
            }
        }




    }


    // 無効なQRコードダイアログ
    if (showInvalidQRCodeDialog) {
        Dialog(onDismissRequest = { showInvalidQRCodeDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(id = R.string.not_url_message),
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                    Button(onClick = { showInvalidQRCodeDialog = false }) {
                        Text(
                            text = "OK"
                        )
                    }
                }
            }
        }
    }
}


// QRコードが有効なURLかどうかを確認する関数
private fun isValidUrl(qrCode: String): Boolean {
    return try {
        val uri = Uri.parse(qrCode)
        uri.scheme == "http" || uri.scheme == "https"
    } catch (e: Exception) {
        false
    }
}


@Composable
fun CameraPreview(onQRCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val executor = remember { Executors.newSingleThreadExecutor() }


    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val barcodeScanner = BarcodeScanning.getClient()
            val analysis = ImageAnalysis.Builder().build().apply {
                setAnalyzer(executor, { imageProxy ->
                    processImageProxy(barcodeScanner, imageProxy, onQRCodeScanned)
                })
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as LifecycleOwner,
                    cameraSelector,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            previewView
        },
        onRelease = {
            cameraProviderFuture.get().unbindAll()
            executor.shutdown()
        }
    )
}




@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onQRCodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.forEach { barcode ->
                    barcode.rawValue?.let { qrCode ->
                        Log.d("QRCodeScannerScreen", "QR Code Scanned: $qrCode")
                        onQRCodeScanned(qrCode)
                        return@forEach // `break`の代わりに使用
                    }
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

