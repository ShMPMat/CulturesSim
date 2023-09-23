package io.tashtabash.sim.space.tile

import kotlin.math.abs


class Flow(x: Double = 0.0, y: Double = 0.0) {
    var strength = 0.0
        private set

    var x = x
        set(value) {
            field = value
            updateStrength()
        }
    var y = y
        set(value) {
            field = value
            updateStrength()
        }

    private fun updateStrength() {
        strength = abs(x) + abs(y)
    }
}
