package io.github.snd_r.komga.sse

//class KtorKomgaSSESession internal constructor(
//    private val json: Json,
//    private val session: ClientSSESession,
//) : KomgaSSESession {
//
//    override val coroutineContext: CoroutineContext = session.coroutineContext
//    override val incoming = session.incoming.map { json.toKomgaEvent(it.event, it.data) }
//    override fun cancel() {
//        session.cancel()
//    }
//}
