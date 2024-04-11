package io.github.snd_r.komelia.image

import kotlin.js.Promise

@JsModule("wasm-vips")
//@Suppress("UPPER_BOUND_VIOLATED")
external fun Vips(config: JsAny): Promise<JsAny>
