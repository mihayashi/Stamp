package jp.gogo_south.stamp.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import jp.gogo_south.stamp.QRCodeScannerScreen
import jp.gogo_south.stamp.UUIDManager
import jp.gogo_south.stamp.WebViewScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current // Contextを取得
    // UUIDの取得
    val uuid = UUIDManager.getUUID(context)

    NavHost(navController, startDestination = "webview?scannedUrl={scannedUrl}") {
        composable(
            route = "webview?scannedUrl={scannedUrl}",
            arguments = listOf(navArgument("scannedUrl") { nullable = true })
        ) {
            backStackEntry ->
            val scannedUrl = backStackEntry.arguments?.getString("scannedUrl") ?: "https://cpwebop.xsrv.jp/stamp_app/?uuid=$uuid"
            WebViewScreen(
                uuid = uuid,
                url = scannedUrl,
                onQRCodeButtonClicked = {
                    navController.navigate("qr_scanner")
                }
            )
        }
        composable("qr_scanner") {
            QRCodeScannerScreen(
                onQRCodeScanned = { scannedUrl ->
                    navController.navigate("webview?scannedUrl=${scannedUrl}") {
                        popUpTo("webview") { inclusive = true } // 重複を避ける
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}