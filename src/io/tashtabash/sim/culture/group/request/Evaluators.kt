package io.tashtabash.sim.culture.group.request

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.AspectImprovementLabeler
import io.tashtabash.sim.culture.aspect.getAspectImprovement
import io.tashtabash.sim.culture.aspect.hasMeaning
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.labeler.BaseNameLabeler
import io.tashtabash.sim.space.resource.tag.labeler.PassingLabeler
import io.tashtabash.sim.space.resource.tag.labeler.SimpleNameLabeler
import io.tashtabash.sim.space.resource.tag.labeler.TagLabeler

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
