package io.tashtabash.sim.space


class Scale(val tileSizeKm: Double, val tickDurationSeconds: Double) {
    val tileAreaKm2 = tileSizeKm * tileSizeKm

    fun toTileSpeed(speedMs: Double): Double {
        val metresPerTile = tileSizeKm * 1000.0
        val distancePerTick = speedMs * tickDurationSeconds / REST_COEFFICIENT
        return distancePerTick / metresPerTile
    }

    override fun toString() = "Scale: ${tileSizeKm}km per tile, 1 tick is ${tickDurationSeconds}s"
}

private const val REST_COEFFICIENT = 25.0 // Assume that entities can't move at their usual speed all the time
