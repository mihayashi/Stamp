package jp.gogo_south.stamp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.tooling.preview.Preview

import android.os.Bundle
import android.provider.Settings.Global.getString
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import jp.gogo_south.stamp.ui.theme.StampTheme
import android.util.Log
import android.webkit.WebSettings
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward

import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat

class WebViewActivity : ComponentActivity() {
    private var cameraPermissionGranted by mutableStateOf(false) // カメラ権限の状態を保持する変数

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UUIDを取得
        val uuid = UUIDManager.getUUID(this) // UUIDの取得方法に合わせて変更してください

        setContent {
            StampTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WebViewScreen(
                        uuid = uuid,
                        url = "https://cpwebop.xsrv.jp/stamp_app/?uuid=$uuid",
                        onQRCodeButtonClicked = {
                            // QRコード読み取り画面に遷移する処理をここに追加

                        })
                }
            }
        }
    }
}



@Composable
fun WebViewScreen(
    uuid: String,
    url: String,
    onQRCodeButtonClicked: () -> Unit, // QRコード読み取り画面への遷移コールバック
    context: Context = LocalContext.current // カメラ権限チェックに必要
) {
    var isBottomGroupVisible by remember { mutableStateOf(true) }
    var accumulatedScroll by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    var showPermissionReminderDialog by remember { mutableStateOf(false) }

    // WebViewを操作するための参照
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    // QRコード読取ボタンのクリック時の処理
    fun handleQRCodeButtonClick() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // 権限がある場合、QRコード画面に遷移
            onQRCodeButtonClicked()
        } else {
            // 権限がない場合、ダイアログを表示
            showPermissionReminderDialog = true
        }
    }

    // ダイアログが表示されている場合
    if (showPermissionReminderDialog) {
        Dialog(onDismissRequest = { showPermissionReminderDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
//                        text = stringResource(id = R.string.permission_reminder),
                        text = context.getString(R.string.permission_reminder, context.getString(R.string.app_name)),
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                    Button(onClick = { showPermissionReminderDialog = false }) {
                        Text("OK")
                    }
                }
            }
        }
    }

    // WebViewとUIのレイアウト部分
    Column(modifier = Modifier.fillMaxSize()) {
        // WebViewとボタングループ2を重ねる
        Box(modifier = Modifier.weight(1f)) {
            // WebViewの配置
            AndroidView(factory = { webView.apply {
                webViewClient = WebViewClient()

                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.domStorageEnabled = true
                Log.d("WebViewScreen", "loadUrlに設定するURL: $url")
                loadUrl(url)

                setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                    val scrollDelta = scrollY - oldScrollY
                    accumulatedScroll += scrollDelta

                    if (accumulatedScroll > 50) {
                        isBottomGroupVisible = false
                        accumulatedScroll = 0
                    } else if (accumulatedScroll < -50) {
                        isBottomGroupVisible = true
                        accumulatedScroll = 0
                    }
                }
            }}, modifier = Modifier.fillMaxSize())

            // ボタングループ2 - WebView上に重ねる
            val offsetY = if (isBottomGroupVisible) 0.dp else 100.dp
            ButtonGroup2(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, with(density) { offsetY.toPx().roundToInt() }) }
                    .background(Color.Black.copy(alpha = 0.5f)),
                webView = webView // WebViewを渡す
            )
        }

        // ボタングループ1 - 画面下部に配置
        ButtonGroup1(
//            onQRCodeButtonClicked = onQRCodeButtonClicked, // QRコード読み取り画面への遷移コールバックを渡す
            onQRCodeButtonClicked = ::handleQRCodeButtonClick,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black),
            webView = webView // WebViewを渡す
        )
    }
}

@Composable
fun ButtonGroup1(
    onQRCodeButtonClicked: () -> Unit, // QRコード読み取り画面への遷移コールバック
    modifier: Modifier = Modifier, webView: WebView) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ホームボタン
        IconButton(onClick = {
            webView.loadUrl("https://cpwebop.xsrv.jp/stamp_app/")
        }) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "ホーム",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // QR読取ボタン
        IconButton(onClick = {
            // QRコード読み取りの画面へ遷移
            onQRCodeButtonClicked()
        }) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = "QR読取",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // リロードボタン
        IconButton(onClick = {
            webView.reload() // WebViewをリロード
        }) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "リロード",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun ButtonGroup2(modifier: Modifier = Modifier, webView: WebView) {
    // ボタンの状態を保持
    val canGoBack = remember { mutableStateOf(webView.canGoBack()) }
    val canGoForward = remember { mutableStateOf(webView.canGoForward()) }

    // ページ読み込み完了時に「進む」「戻る」状態を更新
    DisposableEffect(webView) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                canGoBack.value = webView.canGoBack()
                canGoForward.value = webView.canGoForward()
            }
        }
        onDispose { /* リスナーのクリーンアップは特に不要 */ }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 戻るボタン
        IconButton(
            onClick = {
                if (canGoBack.value) {
                    webView.goBack()
                }
            },
            enabled = canGoBack.value // 履歴がない場合は無効
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                contentDescription = "戻る",
                tint = Color.White,
                modifier = Modifier
                    .size(50.dp)
                    .alpha(if (canGoBack.value) 1f else 0.5f) // 無効時は透明度50%
            )
        }

        // 進むボタン
        IconButton(
            onClick = {
                if (canGoForward.value) {
                    webView.goForward()
                }
            },
            enabled = canGoForward.value // 進むページがない場合は無効
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = "進む",
                tint = Color.White,
                modifier = Modifier
                    .size(50.dp)
                    .alpha(if (canGoForward.value) 1f else 0.5f) // 無効時は透明度50%
            )
        }
    }
}

