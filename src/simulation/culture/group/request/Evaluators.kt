package simulation.culture.group.request

import simulation.culture.aspect.Aspect
import simulation.space.resource.Resource
import simulation.culture.aspect.AspectImprovementTag
import simulation.culture.aspect.getAspectImprovement
import simulation.space.resource.tag.ResourceTag

val passingEvaluator: ResourceEvaluator
    get() = ResourceEvaluator { it.amount.toDouble() }

fun resourceEvaluator(resource: Resource) =
        ResourceEvaluator { if (it.baseName == resource.baseName) it.amount.toDouble() else 0.toDouble() }

fun simpleResourceEvaluator(resource: Resource) =
        ResourceEvaluator { if (it.simpleName == resource.simpleName) it.amount.toDouble() else 0.toDouble() }

fun tagEvaluator(tag: ResourceTag) = ResourceEvaluator { it.getTagPresence(tag) }

fun aspectEvaluator(aspect: Aspect) = ResourceEvaluator { it.getAspectImprovement(aspect) }