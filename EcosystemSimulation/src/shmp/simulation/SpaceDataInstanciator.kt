package shmp.simulation

import shmp.simulation.space.Data
import shmp.simulation.space.SpaceData
import shmp.simulation.space.resource.material.MaterialPool
import shmp.simulation.space.resource.tag.TagMatcher
import kotlin.math.ceil
import kotlin.math.roundToInt


fun instantiateSpaceData(proportionFactor: Double, resourceTagMatchers: List<TagMatcher>, materialPool: MaterialPool) {
    val startResourceAmountMin = (40 * proportionFactor * proportionFactor).toInt()
    val defaultData = Data()

    SpaceData.data = Data(
            materialPool = materialPool,
            mapSizeX = (defaultData.mapSizeX * proportionFactor).toInt(),
            mapSizeY = (defaultData.mapSizeY * proportionFactor).toInt(),
            platesAmount = (defaultData.platesAmount * proportionFactor).toInt(),
            additionalTags = resourceTagMatchers,
            tectonicRange = defaultData.tectonicRange * (proportionFactor * 0.75).roundToInt(),
            minTectonicRise = ceil(defaultData.minTectonicRise.toDouble() / proportionFactor).toInt(),
            startResourceAmountMin = startResourceAmountMin,
            startResourceAmountMax = (startResourceAmountMin + 30 * proportionFactor * proportionFactor).toInt(),
            seabedLevel = (defaultData.seabedLevel - (proportionFactor - 1) * 10).toInt()
    )
}
