package simulation.culture.group.request

import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack
import simulation.space.resource.tag.ResourceTag

val passingEvaluator: ResourceEvaluator
    get() = ResourceEvaluator(
            { it },
            ResourcePack::amount
    )

fun resourceEvaluator(resource: Resource) = ResourceEvaluator(
        { it.getResource(resource) },
        { it.getAmount(resource) }
)

fun tagEvaluator(tag: ResourceTag) = ResourceEvaluator(
        { it.getResources(tag) },
        { it.getTagPresenceSum(tag) }
)
