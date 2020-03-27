package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.group.AspectCenter
import simulation.space.resource.tag.ResourceTag

interface Dependency {
    val name: String
    fun isCycleDependency(otherAspect: Aspect): Boolean
    fun isCycleDependencyInner(otherAspect: Aspect): Boolean
    fun useDependency(controller: AspectController): AspectResult
    val isPhony: Boolean
    fun copy(): Dependency
    fun swapDependencies(aspectCenter: AspectCenter)
}