package io.github.ppking2897.travelrecord

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlin.time.ExperimentalTime

@ExperimentalTime
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // App 只有淺色配色：手機開深色模式時，狀態列與導覽列也固定用深色圖示。
        // 導覽列的半透明深底只在 Android 8.0 以前用到（那時導覽列圖示不能改成深色）。
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.argb(0x80, 0x1B, 0x1B, 0x1B))
        )
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}
