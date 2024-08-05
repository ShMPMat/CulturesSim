package io.tashtabash.sim.culture.aspect.dependency

import java.util.*

abstract class AbstractDependency(override val isMain: Boolean) : Dependency {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AbstractDependency
        return isMain == that.isMain
    }

    override fun hashCode(): Int {
        return Objects.hash(isMain)
    }
}