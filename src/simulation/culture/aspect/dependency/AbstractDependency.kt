package simulation.culture.aspect.dependency

import simulation.space.resource.tag.ResourceTag
import java.util.*

abstract class AbstractDependency(override var type: ResourceTag) : Dependency {
    override val isPhony: Boolean
        get() = type.name == "phony"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AbstractDependency
        return type == that.type
    }

    override fun hashCode(): Int {
        return Objects.hash(type)
    }
}