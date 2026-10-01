package io.tashtabash.sim.space.resource

import io.tashtabash.sim.space.Scale


data class Behaviour(
    var resistance: Double,
    var danger: Double,
    val camouflage: Double,
    val speedMs: Double,
    val overflowType: OverflowType
) {
    val isResisting
        get() = resistance > 0.0

    // Speed in tiles per tick
    fun tileSpeed(scale: Scale): Double = scale.toTileSpeed(speedMs)

    override fun toString() = "resistance ${"%.2f".format(resistance)}," +
            " danger ${"%.2f".format(danger)}" +
            " camouflage ${"%.2f".format(camouflage)}" +
            " speed ${"%.2f".format(speedMs)}m/s"
}
