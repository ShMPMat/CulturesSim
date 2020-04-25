package simulation.culture.group.intergroup

import simulation.culture.group.centers.Group

class Relation(var owner: Group, var other: Group) {
    var positive = 0.0
        set(value) {
            field = value
            if (positive > 1) field = 1.0
            if (positive < -1) field = -1.0
        }

    var positiveInteractions = 0

    var pair: Relation = this

    override fun toString() = "${other.name} is $positive"

    val positiveNormalized: Double
        get() = positive + 1

}