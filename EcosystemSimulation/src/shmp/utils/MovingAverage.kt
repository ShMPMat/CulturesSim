package shmp.utils


data class MovingAverage(var value: SoftValue, val coefficient: Double) : Comparable<MovingAverage> {
    constructor(double: Double, coefficient: Double): this(SoftValue(double), coefficient)
    constructor(int: Int, coefficient: Double): this(SoftValue(int), coefficient)

    fun change(new: SoftValue) {
        value = coefficient * value + (1 - coefficient) * new
    }
    fun change(double: Double) = change(SoftValue(double))
    fun change(int: Int) = change(SoftValue(int))

    override fun toString() = value.toString()

    override fun compareTo(other: MovingAverage) = value.compareTo(other.value)
}
