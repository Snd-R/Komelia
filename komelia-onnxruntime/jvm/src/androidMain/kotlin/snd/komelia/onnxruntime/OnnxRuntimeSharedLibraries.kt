package snd.komelia.onnxruntime

object OnnxRuntimeSharedLibraries {
    var isAvailable = false
        private set

    private val libraries = listOf(
        "omp",
        "onnxruntime",
        "komelia_onnxruntime"
    )

    @Synchronized
    @Suppress("UnsafeDynamicallyLoadedCode")
    fun load() {
        libraries.forEach { System.loadLibrary(it) }
        isAvailable = true
    }

}