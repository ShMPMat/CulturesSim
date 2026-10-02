package io.tashtabash.sim.init

import io.tashtabash.sim.space.Data
import io.tashtabash.sim.space.SpaceData
import io.tashtabash.sim.space.generator.GenerationParameters
import io.tashtabash.sim.space.resource.tag.TagMatcher
import kotlin.math.ceil
import kotlin.math.roundToInt


fun instantiateSpaceData(proportionFactor: Double, resourceTagMatchers: List<TagMatcher>) {
    val startResourceAmountMin = (40 * proportionFactor * proportionFactor).toInt()
    val defaultData = Data()

    val defaultGeneration = defaultData.generation

    SpaceData.data = Data(
        generation = GenerationParameters(
            scale = defaultGeneration.scale,
            sizeX = (defaultGeneration.sizeX * proportionFactor).toInt(),
            sizeY = (defaultGeneration.sizeY * proportionFactor).toInt(),
            platesAmount = (defaultGeneration.platesAmount * proportionFactor).toInt(),
            tectonicRange = defaultGeneration.tectonicRange * (proportionFactor * 0.75).roundToInt(),
            minTectonicRise = ceil(defaultGeneration.minTectonicRise.toDouble() / proportionFactor).toInt(),
        ),
        additionalTags = resourceTagMatchers,
        startResourceAmountMin = (startResourceAmountMin * proportionFactor * proportionFactor).toInt(),
        startResourceAmountMax = ((startResourceAmountMin + 30) * proportionFactor * proportionFactor).toInt(),
        seabedLevel = (defaultData.seabedLevel - (proportionFactor - 1) * 10).toInt(),
        windFriction = defaultData.windFriction / proportionFactor,
    )
}
