package snd.komelia.color

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.math.pow
import kotlin.math.roundToInt

class Levels(initialLevels: ColorLevelsConfig = ColorLevelsConfig.DEFAULT) {
    val sampleCount: Int = 256
    val levelsConfig = MutableStateFlow(initialLevels)
    val lookupTable = levelsConfig.map { calculateLut(it) }

    fun setConfig(config: ColorLevelsConfig) {
        this.levelsConfig.value = config
    }

    fun setLowInput(value: Float) {
        levelsConfig.update { it.copy(lowInput = value) }
    }

    fun setHighInput(value: Float) {
        levelsConfig.update { it.copy(highInput = value) }
    }

    fun setGamma(value: Float) {
        levelsConfig.update { it.copy(gamma = value) }
    }
    fun setLowOutput(value: Float) {
        levelsConfig.update { it.copy(lowOutput = value) }
    }
    fun setHighOutput(value: Float) {
        levelsConfig.update { it.copy(highOutput = value) }
    }

    fun reset() {
        levelsConfig.value = ColorLevelsConfig.DEFAULT
    }

    private fun calculateLut(levels: ColorLevelsConfig): UByteArray? {
        if (levels == ColorLevelsConfig.DEFAULT) return null
        val lut = UByteArray(sampleCount)
        val invGamma = 1.0f / levels.gamma

        for (i in 0 until sampleCount) {
            val value = i / ((sampleCount - 1f))
            val outValue = mapValue(
                value = value,
                lowInput = levels.lowInput,
                highInput = levels.highInput,
                invGamma = invGamma,
                lowOutput = levels.lowOutput,
                highOutput = levels.highOutput,
            )
            lut[i] = (outValue * (sampleCount - 1)).roundToInt().toUByte()
        }
        return lut
    }

    private fun mapValue(
        value: Float,
        lowInput: Float,
        highInput: Float,
        invGamma: Float,
        lowOutput: Float,
        highOutput: Float,
    ): Float {
        var outValue: Float

        outValue = if (highInput != lowInput) {
            (value - lowInput) / (highInput - lowInput)
        } else value - lowInput

        outValue = outValue.coerceIn(0f, 1f)

        if (invGamma != 1.0f && outValue > 0f) {
            outValue = outValue.pow(invGamma)
        }

        if (highOutput >= lowOutput) {
            outValue = outValue * (highOutput - lowOutput) + lowOutput
        } else if (highOutput < lowOutput) {
            outValue = lowOutput - outValue * (lowOutput - highOutput)
        }

        return outValue.coerceIn(0f, 1f)
    }

}
