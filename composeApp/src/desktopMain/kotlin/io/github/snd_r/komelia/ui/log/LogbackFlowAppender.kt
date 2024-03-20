package io.github.snd_r.komelia.ui.log

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LogbackFlowAppender : AppenderBase<ILoggingEvent>() {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val coroutineScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))

    init {
        name = "flowAppender"
    }

    private val mutableLogEventsFlow = MutableSharedFlow<ILoggingEvent>()
    val logEventsFlow = mutableLogEventsFlow.asSharedFlow()
    override fun append(eventObject: ILoggingEvent) {
        coroutineScope.launch { mutableLogEventsFlow.emit(eventObject) }
    }

}