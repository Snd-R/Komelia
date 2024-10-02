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
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

package androidx.compose.foundation.v2

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState

// copied from https://github.com/JetBrains/compose-multiplatform-core/blob/32c8757bdfc8bcf918c76cf8f95349a4ea0a0f09/compose/foundation/foundation/src/skikoMain/kotlin/androidx/compose/foundation/v2/Scrollbar.skiko.kt#L302
// modified averageVisibleLineSize method to not return NaN if last line is unknown (e.g. when animation is in progress)
// work around for bug https://github.com/JetBrains/compose-multiplatform/issues/1980
internal class LazyGridScrollbarAdapter(
    private val scrollState: LazyGridState
) : LazyLineContentAdapter() {

    override val viewportSize: Double
        get() = with(scrollState.layoutInfo) {
            if (orientation == Orientation.Vertical)
                viewportSize.height
            else
                viewportSize.width
        }.toDouble()

    private val isVertical = scrollState.layoutInfo.orientation == Orientation.Vertical

    private val unknownLine = with(LazyGridItemInfo) {
        if (isVertical) UnknownRow else UnknownColumn
    }

    private fun LazyGridItemInfo.line() = if (isVertical) row else column

    private fun LazyGridItemInfo.mainAxisSize() = with(size) {
        if (isVertical) height else width
    }

    private fun LazyGridItemInfo.mainAxisOffset() = with(offset) {
        if (isVertical) y else x
    }

    private fun lineOfIndex(index: Int) = index / scrollState.slotsPerLine

    private fun indexOfFirstInLine(line: Int) = line * scrollState.slotsPerLine

    override fun firstVisibleLine(): VisibleLine? {
        return scrollState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.line() != unknownLine } // Skip exiting items
            ?.let { firstVisibleItem ->
                VisibleLine(
                    index = firstVisibleItem.line(),
                    offset = firstVisibleItem.mainAxisOffset()
                )
            }
    }

    override fun totalLineCount(): Int {
        val itemCount = scrollState.layoutInfo.totalItemsCount
        return if (itemCount == 0)
            0
        else
            lineOfIndex(itemCount - 1) + 1
    }

    override fun contentPadding() = with(scrollState.layoutInfo) {
        beforeContentPadding + afterContentPadding
    }

    override suspend fun snapToLine(lineIndex: Int, scrollOffset: Int) {
        scrollState.scrollToItem(
            index = indexOfFirstInLine(lineIndex),
            scrollOffset = scrollOffset
        )
    }

    override suspend fun scrollBy(value: Float) {
        scrollState.scrollBy(value)
    }

    override fun averageVisibleLineSize(): Double {
        val visibleItemsInfo = scrollState.layoutInfo.visibleItemsInfo
        val indexOfFirstKnownLineItem = visibleItemsInfo.indexOfFirst { it.line() != unknownLine }
        if (indexOfFirstKnownLineItem == -1)
            return 0.0
        val reallyVisibleItemsInfo =  // Non-exiting visible items
            visibleItemsInfo.subList(indexOfFirstKnownLineItem, visibleItemsInfo.size)

        // Compute the size of the last line
        val lastLine = reallyVisibleItemsInfo.last().line()
        if (lastLine == -1) return 0.0
        val lastLineSize = reallyVisibleItemsInfo
            .asReversed()
            .asSequence()
            .takeWhile { it.line() == lastLine }
            .maxOf { it.mainAxisSize() }

        val first = reallyVisibleItemsInfo.first()
        val last = reallyVisibleItemsInfo.last()
        val lineCount = last.line() - first.line() + 1
        val lineSpacingSum = (lineCount - 1) * lineSpacing
        return (
                last.mainAxisOffset() + lastLineSize - first.mainAxisOffset() - lineSpacingSum
                ).toDouble() / lineCount
    }

    override val lineSpacing get() = scrollState.layoutInfo.mainAxisItemSpacing

}