package io.github.snd_r.komelia.worker

import org.w3c.dom.DedicatedWorkerGlobalScope

external val self: DedicatedWorkerGlobalScope

fun main() {
    if (isCrossOriginIsolated()) {
        self.importScripts("vips.js")
        VipsImageActor(self).launch()
    } else {
        CanvasImageActor(self).launch()
    }
}

private fun isCrossOriginIsolated(): Boolean {
    js("return self.crossOriginIsolated;")
}