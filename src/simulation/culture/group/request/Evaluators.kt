package simulation.culture.group.request

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectImprovementLabeler
import simulation.culture.aspect.getAspectImprovement
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.BaseNameLabeler
import simulation.space.resource.tag.labeler.PassingLabeler
import simulation.space.resource.tag.labeler.SimpleNameLabeler
import simulation.space.resource.tag.labeler.TagLabeler

val passingEvaluator: ResourceEvaluator = ResourceEvaluator(PassingLabeler) { it.amount.toDouble() }

fun resourceEvaluator(resource: Resource) =
        ResourceEvaluator(BaseNameLabeler(resource.baseName)) {
            if (it.baseName == resource.baseName)
                it.amount.toDouble()
            else 0.0
        }

fun simpleResourceEvaluator(resource: Resource) =
        ResourceEvaluator(SimpleNameLabeler(resource.simpleName)) {
            if (it.simpleName == resource.simpleName)
                it.amount.toDouble()
            else 0.0
        }

fun tagEvaluator(tag: ResourceTag) =
        ResourceEvaluator(TagLabeler(tag)) { it.getTagPresence(tag) }

fun aspectEvaluator(aspect: Aspect) =
        ResourceEvaluator(AspectImprovementLabeler(aspect)) { it.getAspectImprovement(aspect) }
