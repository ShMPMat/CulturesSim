package shmp.utils

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tanh

/**
 * A boxed value in -1..1
 */
data class SoftValue(val actualValue: Double = 0.0) : Comparable<SoftValue> {
    constructor(actualValue: Int): this(actualValue.toDouble())

    val value = actualValue / (1 + abs(actualValue))
    val norm = (value + 1) / 2
    val reverseNorm = 1 - norm
    val positive = max(0.0, value)
    val negative = min(0.0, value)

    operator fun plus(softValue: SoftValue) = SoftValue(actualValue + softValue.actualValue)
    operator fun minus(softValue: SoftValue) = SoftValue(actualValue - softValue.actualValue)
    operator fun unaryMinus() = SoftValue(-actualValue)
    operator fun times(double: Double) = SoftValue(actualValue * double)
    operator fun div(double: Double) = SoftValue(actualValue / double)

    override fun compareTo(other: SoftValue) = actualValue.compareTo(other.actualValue)
}

operator fun Double.times(softValue: SoftValue) = softValue * this
