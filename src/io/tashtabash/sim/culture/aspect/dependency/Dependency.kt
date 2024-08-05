package io.tashtabash.sim.culture.aspect.dependency

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.AspectController
import io.tashtabash.sim.culture.aspect.AspectResult
import io.tashtabash.sim.culture.group.centers.AspectCenter

interface Dependency {
    val name: String
    fun isCycleDependency(otherAspect: Aspect): Boolean
    fun isCycleDependencyInner(otherAspect: Aspect): Boolean
    fun useDependency(controller: AspectController): AspectResult
    val isMain: Boolean
    fun copy(): Dependency
    fun swapDependencies(aspectCenter: AspectCenter)
}