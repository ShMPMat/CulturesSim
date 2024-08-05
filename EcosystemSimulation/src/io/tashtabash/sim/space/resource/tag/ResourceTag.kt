package io.tashtabash.sim.space.resource.tag

import java.util.*


open class ResourceTag(var name: String, var level: Double = 1.0) {
    open fun copy(level: Double = this.level) = ResourceTag(name, level)

    override fun toString() = name + when {
        level == 1.0 -> ""
        level - level.toInt() == 0.0 -> ":${level.toInt()}"
        else -> ":$level"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ResourceTag) return false

        return name == other.name
    }

    override fun hashCode() = Objects.hash(name)
}

val mainDependencyName = ResourceTag("main")
