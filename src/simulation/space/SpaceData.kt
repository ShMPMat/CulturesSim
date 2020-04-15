package simulation.space

import simulation.space.resource.tag.TagMatcher

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
    val temperatureToWindCoefficient: Int = 1,
    val tileScale: Double = 10.0,
    val resourceDenseCoefficient: Double = tileScale,
    val windPropagation: Double = 0.025,
    val temperatureBaseStart: Int = -15,
    val temperatureBaseFinish: Int = 29,
    val mapSizeX: Int = 45,
    val mapSizeY: Int = 135,
    val platesAmount: Int = 10,
    val defaultWaterLevel: Int = 98,
    val additionalTags: List<TagMatcher> = listOf()
)

