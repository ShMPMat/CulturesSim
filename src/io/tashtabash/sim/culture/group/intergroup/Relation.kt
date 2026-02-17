package io.tashtabash.sim.culture.group.intergroup

import io.tashtabash.sim.culture.group.centers.Group


class Relation(val owner: Group, val other: Group, value: Double = 0.0, var positiveInteractions: Double = 0.0) {
    var value = value
        set(value) {
            field = value.coerceIn(-1.0, 1.0)
        }

    val normalized: Double
        get() = (this.value + 1) / 2

    override fun toString() = "${other.name} is %.3f, $positiveInteractions positive interactions".format(this.value)
}

class OutgoingRelation(var other: Group, value: Double = 0.0, var positiveInteractions: Double = 0.0) {
    var value = value
        set(value) {
            field = value.coerceIn(-1.0, 1.0)
        }

    val normalized: Double
        get() = (value + 1) / 2

    override fun toString() = "${other.name} is %.3f, $positiveInteractions positive interactions".format(value)
}
