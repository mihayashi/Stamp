package jp.gogo_south.stamp
import jp.gogo_south.stamp.ui.PageIndicator


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import jp.gogo_south.stamp.ui.theme.StampTheme
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts



class PermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StampTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionScreen(
                        onSwipeRight = {
                            // 右スワイプで画面1に戻る
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionScreen(onSwipeRight: () -> Unit) {
    val context = LocalContext.current

    // カメラ権限リクエスト用のランチャー
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "カメラの使用が許可されました", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "カメラの使用が拒否されました", Toast.LENGTH_SHORT).show()
        }
        // 許可/拒否にかかわらずWebViewActivityへ遷移
        context.startActivity(Intent(context, WebViewActivity::class.java))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    // 右スワイプで画面1に戻る
                    if (dragAmount > 20) {
                        onSwipeRight()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.second_message),
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // "next" テキストリンクでカメラ権限リクエスト
            Text(
                text = stringResource(id = R.string.next),
                color = Color.Blue,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                // 既に許可されている場合もWebViewActivityへ遷移
                                Toast.makeText(context, "カメラの使用が既に許可されています", Toast.LENGTH_SHORT).show()
                                context.startActivity(Intent(context, WebViewActivity::class.java))
                            }
                            else -> {
                                // 許可されていない場合はリクエストを実行
                                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }
            )
        }

        // ページインジケーターを画面の最下部に配置
        PageIndicator(
            currentPage = 2,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
