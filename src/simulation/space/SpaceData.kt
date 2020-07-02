package simulation.space

import simulation.space.resource.material.MaterialPool
import simulation.space.resource.tag.TagMatcher
import kotlin.random.Random

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
        val materialPool: MaterialPool = MaterialPool(listOf()),
        val maximalWind: Double = 10.0,
        val temperatureToWindCoefficient: Int = 1,
        val tileScale: Double = 5.0,
        val tectonicRange: Int = 2,
        val resourceDenseCoefficient: Double = tileScale,
        val windPropagation: Double = 0.025,
        val temperatureBaseStart: Int = -15,
        val temperatureBaseFinish: Int = 29,
        val mapSizeX: Int = 45,
        val mapSizeY: Int = 135,
        val platesAmount: Int = 10,
        val defaultWaterLevel: Int = 98,
        val seabedLevel: Int = 85,
        val resourceSizeEffect: Double = 0.0,//TODO back to 1.0
        val additionalTags: List<TagMatcher> = listOf(),
        val xMapLooping: Boolean = false,
        val yMapLooping: Boolean = true,
        val random: Random = Random(0L)
) {
    init {
        if (resourceSizeEffect !in 0.0..1.0)
            throw SpaceError("resourceSizeEffect value $resourceSizeEffect is not in 0..1 range")
    }
}
