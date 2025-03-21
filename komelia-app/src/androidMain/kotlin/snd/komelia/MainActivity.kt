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
import io.github.snd_r.komelia.AndroidDependencyContainer
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowSizeClass
import io.github.snd_r.komelia.ui.MainView
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val initScope = CoroutineScope(Dispatchers.Default)
private val initMutex = Mutex()
private val dependencies = MutableStateFlow<AndroidDependencyContainer?>(null)
private val currentActivity = MutableStateFlow<Activity?>(null)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalExceptionHandler.initialize(applicationContext)
        FileKit.init(this)
        WebView.setWebContentsDebuggingEnabled(true);
        currentActivity.value = this

        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        initScope.launch {
            initMutex.withLock {
                if (dependencies.value == null) {
                    dependencies.value = initDependencies(
                        initScope = initScope,
                        context = this@MainActivity,
                        mainActivity = currentActivity
                    )
                }
            }
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
