package simulation.culture.group.intergroup

import simulation.culture.group.centers.Group

class Relation(var owner: Group, var other: Group, var status: Status = Status.None) {
    var positive = 0.0
        set(value) {
            field = value
            if (positive > 1) field = 1.0
            if (positive < -1) field = -1.0
            if (field <= 0.00001) status = Status.War
        }

    var positiveInteractions = 0.0

    var pair: Relation = this

    override fun toString() = "${other.name} is $positive"

    val normalized: Double
        get() = (positive + 1) / 2
}

enum class Status {
    None,
    War
}
