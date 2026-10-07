package io.tashtabash.sim.space

import io.tashtabash.sim.DataInitializationError
import io.tashtabash.sim.SimulationError
import io.tashtabash.sim.space.generator.GenerationParameters
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
    var materialPool: MaterialPool = MaterialPool(listOf()),
    val resourceCapacityPerKm2: Double = 4.0, // Volume of Resources expected on 1 km^2
    val temperatureToWind: Int = 2,
    val maxWind: Double = 10.0,
    val windPropagationDrag: Double = .05, // How much wind is lost when moving to the next tile as an absolute value
    val windFriction: Double = .1,         // How much wind is lost when moving to the next tile as a fraction
    val coriolisEffect: Double = 0.1,
    val temperatureBaseStart: Double = -15.0,
    val temperatureBaseFinish: Double = 29.0,
    val startResourceAmountMin: Int = 40,
    val startResourceAmountMax: Int = startResourceAmountMin + 30,
    val resourceProportionCoefficient: Int = 100,
    val defaultWaterLevel: Int = 98,
    val seabedLevel: Int = 85,
    val resourceSizeEffect: Double = 1.0,
    val additionalTags: List<TagMatcher> = listOf(),
    val xMapLooping: Boolean = false,
    val yMapLooping: Boolean = true,
    val clearSpan: Double = .05,
    val yearDurationDays: Int = 300,
    val dayDurationSeconds: Int = 24 * 60 * 60,
    val tickDurationDays: Int = 10,
    val condensationFactor: Int = 1, // The generated map is condensed into cells this many times larger
    val generation: GenerationParameters = GenerationParameters(
        scale = Scale(50.0, tickDurationDays.toDouble() * dayDurationSeconds),
        sizeX = 60,
        sizeY = 60,
        platesAmount = 10,
        tectonicRange = 2,
    ),
) {
    init {
        if (resourceSizeEffect !in 0.0..1.0)
            throw DataInitializationError("resourceSizeEffect value $resourceSizeEffect is not in 0..1 range")
    }

    val yearDurationTicks = yearDurationDays.toDouble() / tickDurationDays

    val worldSizeXKm = generation.sizeX * generation.scale.tileSizeKm
}
