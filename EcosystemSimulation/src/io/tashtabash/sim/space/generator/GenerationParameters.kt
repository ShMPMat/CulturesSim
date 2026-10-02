package io.tashtabash.sim.space.generator

import io.tashtabash.sim.space.Scale
import kotlin.math.roundToInt


// Parameters for generating a map of the Scale; some of them are measured in cells
class GenerationParameters(
    val scale: Scale,
    val sizeX: Int,
    val sizeY: Int,
    val platesAmount: Int,
    val tectonicRange: Int, // In cells
    val minTectonicRise: Int,
) {
    // The same world in the cells of the finer Scale
    fun forScale(finerScale: Scale): GenerationParameters {
        val ratio = scale.tileSizeKm / finerScale.tileSizeKm
        val factor = ratio.roundToInt()
        require(factor >= 1 && factor.toDouble() == ratio) {
            "$finerScale must divide the cells of $scale into a whole number of cells"
        }

        return GenerationParameters(
            scale = finerScale,
            sizeX = sizeX * factor,
            sizeY = sizeY * factor,
            platesAmount = platesAmount,
            tectonicRange = tectonicRange * factor,
            minTectonicRise = minTectonicRise,
        )
    }
}
