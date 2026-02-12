package io.tashtabash.sim.culture.group.intergroup

import io.tashtabash.sim.culture.group.centers.Group

class Relation(var owner: Group, var other: Group) {
    var positive = 0.0
        set(value) {
            field = value.coerceIn(-1.0, 1.0)
        }

    var positiveInteractions = 0.0

    var pair: Relation = this

    val normalized: Double
        get() = (positive + 1) / 2

    override fun toString() = "${other.name} is %.3f, $positiveInteractions positive interactions".format(positive)
}
