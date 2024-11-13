package jp.gogo_south.stamp
import jp.gogo_south.stamp.ui.PageIndicator


import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import androidx.compose.runtime.Composable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import jp.gogo_south.stamp.ui.theme.StampTheme
import jp.gogo_south.stamp.screens.MainScreen
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {
    private var cameraPermissionChecked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // カメラ権限が許可されているか確認
//        if (isCameraPermissionGranted()) {
//            // 許可されている場合、画面3 (WebViewActivity) に遷移
//            startActivity(Intent(this, WebViewActivity::class.java))
//            finish() // メイン画面を終了して直接画面3に移動
//        } else {
//            // 許可されていない場合、画面1 (PermissionActivity) に遷移
//            setContent {
//                StampTheme {
//                    Surface(
//                        modifier = Modifier.fillMaxSize(),
//                        color = MaterialTheme.colorScheme.background
//                    ) {
//                        InitialScreen(
//                            uuid = UUIDManager.getUUID(this),
//                            onSwipeLeft = {
//                                startActivity(Intent(this, PermissionActivity::class.java))
//                            }
//                        )
//                    }
//                }
//            }
//        }
        setContent {
            StampTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isCameraPermissionGranted()) {
                        // カメラ権限が設定されている場合、MainScreenへ移行
                        MainScreen()
                    } else {
                        // カメラ権限が設定されていない場合は、権限設定を促す画面を表示
                        InitialScreen(
                            uuid = UUIDManager.getUUID(this),
                            onSwipeLeft = {
                                startActivity(Intent(this, PermissionActivity::class.java))
                            }
                        )
                    }
//                    when {
//                        // カメラ権限が確認済みの場合、MainScreen を表示
//                        cameraPermissionChecked -> MainScreen()
//
//                        // カメラ権限が未確認の場合、InitialScreen を表示
//                        else -> InitialScreen(
//                            uuid = UUIDManager.getUUID(this),
//                            onSwipeLeft = {
//                                // PermissionActivity でカメラ権限を確認
//                                startActivityForResult(
//                                    Intent(this, PermissionActivity::class.java), REQUEST_CODE_PERMISSION
//                                )
//                            }
//                        )
//                    }
                }
            }
        }
    }

    // Activity の結果を受け取る
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            // カメラ権限を確認済みにする
            cameraPermissionChecked = true
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 1001
    }

    // カメラ権限が許可されているかチェックする関数
    private fun isCameraPermissionGranted(): Boolean {
//        no.1
//        return ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.CAMERA
//        ) == PackageManager.PERMISSION_GRANTED

//        no.2
//        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            true // 許可されている
//        } else {
//            // まだリクエストしていない場合のみ false を返す
//            !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
//        }

        val permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val firstRequest = !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)

        // パーミッションが許可済みか、拒否済みであれば true を返す。まだリクエストしていない場合は false
        return permissionGranted || !firstRequest
    }
}

@Composable
fun InitialScreen(uuid: String, onSwipeLeft: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    // 左スワイプで画面2へ遷移
                    if (dragAmount < -20) {
                        onSwipeLeft()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .padding(20.dp)
        ) {

            Text(
                text = stringResource(id = R.string.first_message),
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge

            )
        }

        PageIndicator(
            currentPage = 1,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// プレビュー用の@Preview関数
@Preview(showBackground = true)
@Composable
fun PreviewInitialScreen() {
    StampTheme {
        InitialScreen(
            uuid = "Sample-UUID",
            onSwipeLeft = {} // プレビュー用のダミー処理
        )
    }
}