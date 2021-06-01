package shmp.simulation.culture.group.request

import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.AspectImprovementLabeler
import shmp.simulation.culture.aspect.getAspectImprovement
import shmp.simulation.culture.aspect.hasMeaning
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.labeler.BaseNameLabeler
import shmp.simulation.space.resource.tag.labeler.PassingLabeler
import shmp.simulation.space.resource.tag.labeler.SimpleNameLabeler
import shmp.simulation.space.resource.tag.labeler.TagLabeler

val passingEvaluator: ResourceEvaluator = ResourceEvaluator(PassingLabeler) { it.amount.toDouble() }

fun resourceEvaluator(resource: Resource) =
        ResourceEvaluator(BaseNameLabeler(resource.baseName)) {
            if (it.baseName == resource.baseName && (!resource.hasMeaning || it.hasMeaning))
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
