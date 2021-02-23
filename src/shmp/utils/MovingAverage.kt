package shmp.utils


data class MovingAverage(var value: SoftValue, val coefficient: Double) {
    constructor(double: Double, coefficient: Double): this(SoftValue(double), coefficient)
    constructor(int: Int, coefficient: Double): this(SoftValue(int.toDouble()), coefficient)

    fun change(new: SoftValue) {
        value = coefficient * value + (1 - coefficient) * new
    }
    fun change(double: Double) = change(SoftValue(double))
    fun change(int: Int) = change(SoftValue(int.toDouble()))

    override fun toString() = value.toString()
}
