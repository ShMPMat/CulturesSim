package shmp.simulation

import shmp.simulation.space.Data
import shmp.simulation.space.SpaceData
import shmp.simulation.space.resource.material.MaterialPool
import shmp.simulation.space.resource.tag.TagMatcher
import kotlin.math.ceil
import kotlin.math.roundToInt


fun instantiateSpaceData(proportionFactor: Int, resourceTagMatchers: List<TagMatcher>, materialPool: MaterialPool) {
    val startResourceAmountMin = 40 * proportionFactor * proportionFactor
    val defaultData = Data()

    SpaceData.data = Data(
            materialPool = materialPool,
            mapSizeX = defaultData.mapSizeX * proportionFactor,
            mapSizeY = defaultData.mapSizeY * proportionFactor,
            platesAmount = defaultData.platesAmount * proportionFactor,
            additionalTags = resourceTagMatchers,
            tectonicRange = defaultData.tectonicRange * (proportionFactor.toDouble() * 0.75).roundToInt(),
            minTectonicRise = ceil(defaultData.minTectonicRise.toDouble() / proportionFactor).toInt(),
            startResourceAmountMin = startResourceAmountMin,
            startResourceAmountMax = startResourceAmountMin + 30 * proportionFactor * proportionFactor,
            seabedLevel = defaultData.seabedLevel - (proportionFactor - 1) * 10
    )
}
