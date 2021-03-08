package shmp.utils

import kotlin.math.max
import kotlin.math.min
import kotlin.math.tanh

/**
 * Special boxed value in -1..1
 */
data class SoftValue(val actualValue: Double = 0.0) : Comparable<SoftValue> {
    val value = tanh(actualValue)
    val norm = (value + 1) / 2
    val positive = max(0.0, value)
    val negative = min(0.0, value)

    init {
        if (value.isNaN()) {
            val k = 0
        }
    }

    operator fun plus(softValue: SoftValue) = SoftValue(actualValue + softValue.actualValue)
    operator fun minus(softValue: SoftValue) = SoftValue(actualValue - softValue.actualValue)
    operator fun unaryMinus() = SoftValue(-actualValue)
    operator fun times(double: Double) = SoftValue(actualValue * double)
    operator fun div(double: Double) = SoftValue(actualValue / double)

    override fun compareTo(other: SoftValue) = actualValue.compareTo(other.actualValue)
}

operator fun Double.times(softValue: SoftValue) = softValue * this