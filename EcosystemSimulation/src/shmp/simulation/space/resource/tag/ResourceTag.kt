package shmp.simulation.space.resource.tag

import java.util.*


open class ResourceTag(
        var name: String,
        var isInstrumental: Boolean = false, // Whether Resource doesn't waste on use.
        var level: Int = 1
) {
    constructor(name: String, level: Int) : this(name, false, level)

    fun copy() = ResourceTag(name, isInstrumental, level)

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ResourceTag) return false

        return name == other.name
    }

    override fun hashCode() = Objects.hash(name)
}

val phony = ResourceTag("phony")
