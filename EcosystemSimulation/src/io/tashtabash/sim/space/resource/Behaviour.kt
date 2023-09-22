package io.tashtabash.sim.space.resource


data class Behaviour(
    var resistance: Double,
    var danger: Double,
    val camouflage: Double,
    val speed: Double,
    val overflowType: OverflowType
) {
    val isResisting
        get() = resistance > 0.0

    override fun toString() = "resistance ${"%.2f".format(resistance)}," +
            " danger ${"%.2f".format(danger)}" +
            " camouflage ${"%.2f".format(camouflage)}" +
            " speed ${"%.2f".format(speed)}"
}
