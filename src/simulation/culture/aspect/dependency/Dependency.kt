package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.group.AspectCenter
import simulation.space.resource.tag.ResourceTag

interface Dependency {
    val name: String
    fun isCycleDependency(aspect: Aspect): Boolean
    fun isCycleDependencyInner(aspect: Aspect): Boolean
    fun useDependency(controller: AspectController): AspectResult
    val isPhony: Boolean
    val type: ResourceTag
    fun copy(): Dependency
    fun swapDependencies(aspectCenter: AspectCenter)
}