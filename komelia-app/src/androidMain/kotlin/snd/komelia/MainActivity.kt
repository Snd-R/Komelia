package snd.komelia

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.window.layout.WindowMetricsCalculator
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import snd.komelia.ui.MainView
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.platform.WindowSizeClass

private val initScope = CoroutineScope(Dispatchers.Default)
private val initMutex = Mutex()
private val mainActivity = MutableStateFlow<MainActivity?>(null)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebView.setWebContentsDebuggingEnabled(false)
        FileKit.init(this)
        mainActivity.value = this

        initScope.launch {
            initMutex.withLock {
                if (dependencies.value == null) {
                    val module = AndroidAppModule(
                        context = applicationContext,
                        mainActivity = mainActivity
                    )
                    val deps = module.initDependencies()
                    dependencies.value = deps
                }
            }
        }

        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val windowSize = rememberWindowSize()
            MainView(
                dependencies = dependencies.collectAsState().value,
                windowWidth = WindowSizeClass.fromDp(windowSize.width),
                windowHeight = WindowSizeClass.fromDp(windowSize.height),
                platformType = PlatformType.MOBILE,
                keyEvents = MutableSharedFlow()
            )
        }
    }
}

@Composable
private fun Activity.rememberWindowSize(): DpSize {
    val configuration = LocalConfiguration.current
    val windowMetrics = remember(configuration) {
        WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this)
    }
    val windowDpSize = with(LocalDensity.current) {
        windowMetrics.bounds.toComposeRect().size.toDpSize()
    }
    return windowDpSize
}
