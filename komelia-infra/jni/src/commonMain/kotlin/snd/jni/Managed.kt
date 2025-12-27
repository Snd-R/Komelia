// modified version of https://github.com/JetBrains/skiko/blob/1bd3058d694ee22d2b8ddaa6219a88dcb153b768/skiko/src/jvmMain/kotlin/org/jetbrains/skia/impl/Managed.jvm.kt
// which is similar to jdk Cleaner implementation

package snd.jni

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

typealias NativePointer = Long

abstract class Managed(ptr: NativePointer, finalizer: Runnable) : AutoCloseable {
    private var _ptr = ptr

    @Suppress("LeakingThis")
    private val cleanable: Cleanable = CLEANER.register(this, finalizer)
    private val _isClosed = AtomicBoolean(false)
    val isClosed
        get() = _isClosed.get()

    override fun close() {
        if (!_isClosed.compareAndSet(false, true)) return
        cleanable.clean()
        _ptr = 0
    }

    companion object {
        private val CLEANER = Cleaner()
    }
}

// Android doesn't have Cleaner API, so use explicit phantom references + finalization queue.
internal interface Cleanable {
    fun clean()

    var prev: Cleanable?
    var next: Cleanable?
}

private class CleanableImpl(managed: Managed, action: Runnable, cleaner: Cleaner) :
    PhantomReference<Managed>(managed, cleaner.queue), Cleanable {

    override var prev: Cleanable? = this
    override var next: Cleanable? = this

    private val list: Cleanable = cleaner.list
    private var action: Runnable = action

    init {
        insert()

        //FIXME no reachabilityFence in jdk8 (Android API 26)
        // cleaner is stored in static variable and won't be GC'd
        // should be safe?
//        reachabilityFence(managed)
//        reachabilityFence(cleaner)
    }

    override fun clean() {
        if (remove()) {
            super.clear()
            action.run()
        }
    }

    override fun clear() {
        throw UnsupportedOperationException("clear() unsupported")
    }

    private fun insert() {
        synchronized(list) {
            prev = list
            next = list.next
            next?.prev = this
            list.next = this
        }
    }

    private fun remove(): Boolean {
        synchronized(list) {
            if (next !== this) {
                next?.prev = prev
                prev?.next = next
                prev = this
                next = this
                return true
            }
            return false
        }
    }
}

internal class Cleaner {
    val queue = ReferenceQueue<Managed>()
    var list: Cleanable = object : Cleanable {
        override fun clean() {
            TODO("Must not be called")
        }

        override var prev: Cleanable? = null
        override var next: Cleanable? = null
    }

    @Volatile
    var stopped = false

    init {
        thread(start = true, isDaemon = true, name = "Komelia Reference Cleaner") {
            while (!stopped) {
                val ref = queue.remove(60 * 1000L) as Cleanable?
                try {
                    ref?.clean()
                } catch (_: Throwable) {
                }
            }
        }
    }

    fun register(managed: Managed, action: Runnable): Cleanable {
        return CleanableImpl(managed, action, this)
    }

    fun stop() {
        stopped = true
    }
}
