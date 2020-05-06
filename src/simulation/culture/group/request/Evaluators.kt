package simulation.culture.group.request

import simulation.culture.aspect.Aspect
import simulation.space.resource.Resource
import simulation.space.resource.tag.AspectImprovementTag
import simulation.space.resource.tag.ResourceTag

val passingEvaluator: ResourceEvaluator
    get() = ResourceEvaluator { it.amount.toDouble() }

fun resourceEvaluator(resource: Resource) =
        ResourceEvaluator { if (it.baseName == resource.baseName) it.amount.toDouble() else 0.toDouble() }

fun simpleResourceEvaluator(resource: Resource) =
        ResourceEvaluator { if (it.simpleName == resource.simpleName) it.amount.toDouble() else 0.toDouble() }

fun tagEvaluator(tag: ResourceTag) = ResourceEvaluator { it.getTagPresence(tag) }

fun aspectEvaluator(aspect: Aspect) = ResourceEvaluator { r ->
    r.tags.filterIsInstance<AspectImprovementTag>()
            .count { it.labeler.isSuitable(aspect) }
            .toDouble()
}