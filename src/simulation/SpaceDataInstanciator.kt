package simulation

import simulation.space.Data
import simulation.space.SpaceData
import simulation.space.resource.material.MaterialPool
import simulation.space.resource.tag.TagMatcher
import kotlin.math.roundToInt
import kotlin.random.Random

fun instantiateSpaceData(
        proportionFactor: Int,
        resourceTagMatchers: List<TagMatcher>,
        materialPool: MaterialPool,
        random: Random
) {
    val defaultData = Data()
    SpaceData.data = Data(
            materialPool = materialPool,
            mapSizeX = defaultData.mapSizeX * proportionFactor,
            mapSizeY = defaultData.mapSizeY * proportionFactor,
            platesAmount = defaultData.platesAmount * proportionFactor,
            additionalTags = resourceTagMatchers,
            tectonicRange = defaultData.tectonicRange * (proportionFactor.toDouble() * 0.75 ).roundToInt(),
            random = random
    )
}