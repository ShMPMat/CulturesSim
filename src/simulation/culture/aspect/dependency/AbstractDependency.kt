package simulation.culture.aspect.dependency

import java.util.*

abstract class AbstractDependency(override val isPhony: Boolean) : Dependency {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AbstractDependency
        return isPhony == that.isPhony
    }

    override fun hashCode(): Int {
        return Objects.hash(isPhony)
    }
}