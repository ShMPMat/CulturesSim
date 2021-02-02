package shmp.simulation.culture.aspect.dependency

import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.AspectController
import shmp.simulation.culture.aspect.AspectResult
import shmp.simulation.culture.group.centers.AspectCenter

interface Dependency {
    val name: String
    fun isCycleDependency(otherAspect: Aspect): Boolean
    fun isCycleDependencyInner(otherAspect: Aspect): Boolean
    fun useDependency(controller: AspectController): AspectResult
    val isPhony: Boolean
    fun copy(): Dependency
    fun swapDependencies(aspectCenter: AspectCenter)
}