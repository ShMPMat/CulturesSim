package io.tashtabash.simulation.culture.aspect.dependency

import io.tashtabash.simulation.culture.aspect.Aspect
import io.tashtabash.simulation.culture.aspect.AspectController
import io.tashtabash.simulation.culture.aspect.AspectResult
import io.tashtabash.simulation.culture.group.centers.AspectCenter

interface Dependency {
    val name: String
    fun isCycleDependency(otherAspect: Aspect): Boolean
    fun isCycleDependencyInner(otherAspect: Aspect): Boolean
    fun useDependency(controller: AspectController): AspectResult
    val isPhony: Boolean
    fun copy(): Dependency
    fun swapDependencies(aspectCenter: AspectCenter)
}