package simulation

import simulation.space.Data
import simulation.space.SpaceData

fun instantiateSpaceData(proportionFactor: Int) {
    val defaultData = Data()
    SpaceData.data = Data(
            mapSizeX = defaultData.mapSizeX * proportionFactor,
            mapSizeY = defaultData.mapSizeY * proportionFactor,
            platesAmount = defaultData.platesAmount * proportionFactor
    )
}