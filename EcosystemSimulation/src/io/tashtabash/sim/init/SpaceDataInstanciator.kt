package io.tashtabash.sim.init

import io.tashtabash.sim.space.Data
import io.tashtabash.sim.space.SpaceData
import io.tashtabash.sim.space.generator.GenerationParameters
import io.tashtabash.sim.space.resource.tag.TagMatcher
import kotlin.math.*


fun instantiateSpaceData(proportionFactor: Double, resourceTagMatchers: List<TagMatcher>) {
    val defaultData = Data()
    val newCondensationFactor = max(1, proportionFactor.toInt() / 2)
    val squaredCondensation = newCondensationFactor.toDouble().pow(2)
    val startResourceAmountMin = (40 * proportionFactor.pow(2) / squaredCondensation).toInt()

    val defaultGeneration = defaultData.generation

    SpaceData.data = Data(
        generation = GenerationParameters(
            scale = defaultGeneration.scale,
            sizeX = (defaultGeneration.sizeX * proportionFactor).toInt(),
            sizeY = (defaultGeneration.sizeY * proportionFactor).toInt(),
            platesAmount = (defaultGeneration.platesAmount * proportionFactor).toInt(),
            tectonicRange = defaultGeneration.tectonicRange * (proportionFactor * 0.5).roundToInt(),
            minTectonicRise = ceil(defaultGeneration.minTectonicRise.toDouble() / proportionFactor).toInt(),
        ),
        additionalTags = resourceTagMatchers,
        startResourceAmountMin = (startResourceAmountMin * proportionFactor.pow(2) / squaredCondensation).toInt(),
        startResourceAmountMax = ((startResourceAmountMin + 30) * proportionFactor.pow(2) / squaredCondensation).toInt(),
        seabedLevel = (defaultData.seabedLevel - (proportionFactor - 1) * 10).toInt(),
        windFriction = defaultData.windFriction / proportionFactor,
        condensationFactor = newCondensationFactor,
    )
}
