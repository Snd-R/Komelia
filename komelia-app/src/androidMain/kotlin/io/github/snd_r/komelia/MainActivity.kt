package io.github.snd_r.komelia

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.window.layout.WindowMetricsCalculator
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowWidth
import io.github.snd_r.komelia.ui.MainView
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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScope.launch {
            initMutex.withLock {
                if (dependencies.value == null)
                    dependencies.value = AndroidDependencyContainer.createInstance(initScope, this@MainActivity)
            }
        }

        setContent {
            MainView(
                dependencies = dependencies.collectAsState().value,
                windowWidth = WindowWidth.fromDp(rememberWindowSize().width),
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
