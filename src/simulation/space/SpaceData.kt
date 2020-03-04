package simulation.space

object SpaceData {
    private var wasCalled = false
    var data = Data()
        get() {
            wasCalled = true
            return field
        }
        set(value) {
            if (wasCalled)
                throw ExceptionInInitializerError("Tried to change Space Data after use")
            field = value
        }

}

class Data(
    val maximalWind: Double = 10.0,
    val tileScale: Double = 10.0,
    val resourceDenseCoefficient: Double = tileScale
)

