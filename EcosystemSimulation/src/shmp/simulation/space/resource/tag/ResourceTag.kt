package shmp.simulation.space.resource.tag

import java.util.*


open class ResourceTag constructor(
        var name: String,
        var level: Int = 1,
        var isInstrumental: Boolean = false // Whether Resource doesn't waste on use.
) {
    fun copy(level: Int = this.level) = ResourceTag(name, level, isInstrumental)

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ResourceTag) return false

        return name == other.name
    }

    override fun hashCode() = Objects.hash(name)
}

val phony = ResourceTag("phony")
