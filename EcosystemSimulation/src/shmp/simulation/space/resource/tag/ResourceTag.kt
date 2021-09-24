package shmp.simulation.space.resource.tag

import java.util.*


open class ResourceTag constructor(
        var name: String,
        var level: Double = 1.0,
        var isInstrumental: Boolean = false // Whether Resource doesn't waste on use.
) {
    open fun copy(level: Double = this.level) = ResourceTag(name, level, isInstrumental)

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ResourceTag) return false

        return name == other.name
    }

    override fun hashCode() = Objects.hash(name)
}

val phony = ResourceTag("phony")
