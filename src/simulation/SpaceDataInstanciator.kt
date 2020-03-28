package simulation

import simulation.space.Data
import simulation.space.SpaceData
import simulation.space.resource.tag.TagMatcher

fun instantiateSpaceData(proportionFactor: Int, resourceTagMatchers: List<TagMatcher>) {
    val defaultData = Data()
    SpaceData.data = Data(
            mapSizeX = defaultData.mapSizeX * proportionFactor,
            mapSizeY = defaultData.mapSizeY * proportionFactor,
            platesAmount = defaultData.platesAmount * proportionFactor,
            additionalTags = resourceTagMatchers
    )
}