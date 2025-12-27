/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// adapted from
// https://github.com/JetBrains/compose-multiplatform-core/blob/0274827b8341e2faaa22f80f6cb9e6aa97fc031d/compose/foundation/foundation/src/jsWasmMain/kotlin/androidx/compose/foundation/gestures/JsScrollable.js.kt

package snd.komelia.ui.reader.image.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastFold

internal actual fun platformScrollConfig(): ScrollConfig = JsConfig

private object JsConfig : ScrollConfig {
    override fun Density.calculateMouseWheelScroll(event: PointerEvent, bounds: IntSize): Offset {
        // Note: The returned offset value here is not strictly accurate.
        // However, it serves two primary purposes:
        // 1. Ensures all related tests pass successfully.
        // 2. Provides satisfactory UI behavior
        // In future iterations, this value could be refined to enhance UI behavior.
        // However, keep in mind that any modifications would also necessitate adjustments to the corresponding tests.
        return event.totalScrollDelta * -1f
    }
}

private val PointerEvent.totalScrollDelta
    get() = this.changes.fastFold(Offset.Zero) { acc, c -> acc + c.scrollDelta }

