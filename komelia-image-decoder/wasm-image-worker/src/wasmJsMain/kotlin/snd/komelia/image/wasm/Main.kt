package snd.komelia.image.wasm

import org.w3c.dom.DedicatedWorkerGlobalScope
import snd.komelia.image.wasm.actor.CanvasImageActor

external val self: DedicatedWorkerGlobalScope

fun main() {
//    if (isCrossOriginIsolated()) {
//        self.importScripts("vips.js")
//        VipsImageActor(self).launch()
//    } else {
//        CanvasImageActor(self).launch()
//    }
    CanvasImageActor(self).launch()
}

private fun isCrossOriginIsolated(): Boolean {
    js("return self.crossOriginIsolated;")
}