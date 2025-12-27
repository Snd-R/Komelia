package snd.komelia.ui.reader.image.continuous

import snd.komelia.settings.model.ContinuousReadingDirection
import snd.komelia.settings.model.ContinuousReadingDirection.LEFT_TO_RIGHT
import snd.komelia.settings.model.ContinuousReadingDirection.RIGHT_TO_LEFT
import snd.komelia.settings.model.ContinuousReadingDirection.TOP_TO_BOTTOM


class KeyMapState(
    readingDirection: ContinuousReadingDirection,
    private val volumeKeysNavigation: Boolean,
    private val scrollBy: (Float) -> Unit,
    private val scrollForward: () -> Unit,
    private val scrollBackward: () -> Unit,
    private val scrollToFirstPage: () -> Unit,
    private val scrollToLastPage: () -> Unit,
    private val changeReadingDirection: (ContinuousReadingDirection) -> Unit,
) {
    private var upKeyPressed: Boolean = false
    private var downKeyPressed: Boolean = false
    private var leftKeyPressed: Boolean = false
    private var rightKeyPressed: Boolean = false
    private var volumeKeyUpPressed: Boolean = false
    private var volumeKeyDownPressed: Boolean = false

    private val upKeyAction: () -> Unit
    private val downKeyAction: () -> Unit
    private val leftKeyAction: () -> Unit
    private val rightKeyAction: () -> Unit

    init {
        when (readingDirection) {
            TOP_TO_BOTTOM -> {
                upKeyAction = { scrollBy(100f) }
                downKeyAction = { scrollBy(-100f) }
                leftKeyAction = { if (!leftKeyPressed) scrollBackward() }
                rightKeyAction = { if (!rightKeyPressed) scrollForward() }
            }

            LEFT_TO_RIGHT -> {
                upKeyAction = { if (!upKeyPressed) scrollBackward() }
                downKeyAction = { if (!downKeyPressed) scrollForward() }
                leftKeyAction = { scrollBy(100f) }
                rightKeyAction = { scrollBy(-100f) }
            }

            RIGHT_TO_LEFT -> {
                upKeyAction = { if (!upKeyPressed) scrollBackward() }
                downKeyAction = { if (!downKeyPressed) scrollForward() }
                leftKeyAction = { scrollBy(100f) }
                rightKeyAction = { scrollBy(-100f) }
            }
        }
    }

    fun onUpKeyUp(): Boolean {
        upKeyPressed = false
        return true
    }

    fun onUpKeyDown(): Boolean {
        upKeyAction()
        upKeyPressed = true
        return true
    }

    fun onDownKeyUp(): Boolean {
        downKeyPressed = false
        return true
    }

    fun onDownKeyDown(): Boolean {
        downKeyAction()
        downKeyPressed = true
        return true
    }

    fun onLeftKeyUp(altPressed: Boolean): Boolean {
        leftKeyPressed = false
        return !altPressed
    }

    fun onLeftKeyDown(): Boolean {
        leftKeyAction()
        leftKeyPressed = true
        return true
    }

    fun onRightKeyUp(): Boolean {
        rightKeyPressed = false
        return true

    }

    fun onRightKeyDown(): Boolean {
        rightKeyAction()
        rightKeyPressed = true
        return true
    }

    fun onVolumeUpKeyUp(): Boolean {
        if (!volumeKeysNavigation) return false
        volumeKeyUpPressed = false
        return true
    }

    fun onVolumeUpKeyDown(): Boolean {
        if (!volumeKeysNavigation) return false
        if (!volumeKeyUpPressed) scrollBackward()
        volumeKeyUpPressed = true
        return true
    }

    fun onVolumeDownKeyUp(): Boolean {
        if (!volumeKeysNavigation) return false
        volumeKeyDownPressed = false
        return true
    }

    fun onVolumeDownKeyDown(): Boolean {
        if (!volumeKeysNavigation) return false
        if (!volumeKeyDownPressed) scrollForward()
        volumeKeyDownPressed = true
        return true
    }

    fun onReadingDirectionChange(direction: ContinuousReadingDirection): Boolean {
        changeReadingDirection(direction)
        return true
    }

    fun onScrollToFirstPage(): Boolean {
        scrollToFirstPage()
        return true
    }

    fun onScrollToLastPage(): Boolean {
        scrollToLastPage()
        return true
    }
}
