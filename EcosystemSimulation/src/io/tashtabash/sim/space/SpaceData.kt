package io.tashtabash.sim.space

import io.tashtabash.sim.DataInitializationError
import io.tashtabash.sim.SimulationError
import io.tashtabash.sim.space.resource.container.ResourcePool
import io.tashtabash.sim.space.resource.material.MaterialPool
import io.tashtabash.sim.space.resource.tag.TagMatcher


object SpaceData {
    private var wasCalled = false
    var data = Data()
        get() {
            wasCalled = true
            return field
        }
        set(value) {
            if (wasCalled)
                throw SimulationError("Tried to change Space Data after use")
            field = value
        }

}

class Data(
        var resourcePool: ResourcePool = ResourcePool(listOf()),
        val materialPool: MaterialPool = MaterialPool(listOf()),
        val maximalWind: Double = 10.0,
        val temperatureToWindCoefficient: Int = 1,
        val tileScale: Double = 5.0,
        val tileResourceCapacity: Double = 10000.0,
        val tectonicRange: Int = 2,
        val minTectonicRise: Int = 5,
        val resourceDenseCoefficient: Double = tileScale,
        val windPropagation: Double = 0.025,
        val coriolisEffect: Double = 0.1,
        val windFillIn: Double = 0.1,
        val temperatureBaseStart: Double = -15.0,
        val temperatureBaseFinish: Double = 29.0,
        val startResourceAmountMin: Int = 40,
        val startResourceAmountMax: Int = startResourceAmountMin + 30,
        val resourceProportionCoefficient: Int = 100,
        val mapSizeX: Int = 45,
        val mapSizeY: Int = 60,
        val platesAmount: Int = 10,
        val defaultWaterLevel: Int = 98,
        val seabedLevel: Int = 85,
        val resourceSizeEffect: Double = 1.0,
        val additionalTags: List<TagMatcher> = listOf(),
        val xMapLooping: Boolean = false,
        val yMapLooping: Boolean = true,
        val clearSpan: Double = 0.05
) {
    init {
        if (resourceSizeEffect !in 0.0..1.0)
            throw DataInitializationError("resourceSizeEffect value $resourceSizeEffect is not in 0..1 range")
    }
}
