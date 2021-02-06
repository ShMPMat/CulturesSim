package shmp.utils

import kotlin.math.tanh

/**
 * Special boxed value in -1..1
 */
data class SoftValue(val actualValue: Double = 0.0) {
    val value = tanh(actualValue)
    val norm = (value + 1) / 2

    operator fun plus(softValue: SoftValue) = SoftValue(actualValue + softValue.actualValue)
    operator fun unaryMinus() = SoftValue(-actualValue)
}
