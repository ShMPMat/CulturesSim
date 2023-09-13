package io.tashtabash.sim.culture.aspect

import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.sim.culture.aspect.dependency.AspectDependencies
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.sim.space.resource.ExternalResourceFeature
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.ResourceTag
import java.util.*


class MeaningInserter(aspect: Aspect, resource: Resource) : ConverseWrapper(aspect, resource.fullCopy().copy(1)) {
    override val producedResources = listOf(this.resource.copyWithExternalFeatures(listOf(phonyMeaningFeature)))

    init {
        canInsertMeaning = true
    }

    override fun use(controller: AspectController): AspectResult {
        val result = super.use(controller)

        val targetResources = result.resources
                .getResourceAndRemove(resource)
                .resources.toMutableList()
        targetResources.removeIf { it.isEmpty }

        val meaning = controller.meaning ?: controller.group.cultureCenter.meaning

        val resultResources = targetResources.map { r ->
            val name = makePostfix(result, meaning)

            r.copyWithNewExternalFeatures(listOf(
                    MeaningResourceFeature(meaning, name),
                    MadeByResourceFeature(controller.group)
            ))
        }
        result.resources.addAll(resultResources)

        return result
    }

    private fun makePostfix(result: AspectResult, meaning: Meme) =
            "representing_${meaning}_with_${result.node?.aspect?.name}"

    override fun shouldPassMeaningNeed(isMeaningNeeded: Boolean) = false

    override fun copy(dependencies: AspectDependencies): MeaningInserter {
        val copy = MeaningInserter(aspect, resource)
        copy.initDependencies(dependencies)
        val unwantedTags: MutableCollection<ResourceTag> = ArrayList()

        for (resourceTag in dependencies.map.keys)
            if (!(resourceTag.isInstrumental || resourceTag.name == "phony"))//TODO why?
                unwantedTags.add(resourceTag)

//        unwantedTags.forEach { copy.dependencies.map.remove(it) }
        return copy
    }
}


data class MeaningResourceFeature(
        val meme: Meme,
        override val name: String = meme.observerWord
) : ExternalResourceFeature {
    override val index = 0
}

class MadeByResourceFeature(group: Group) : ExternalResourceFeature {
    override val name = "MadeBy_${group.name}"

    override val index = 1
}


private val phonyMeaningFeature = MeaningResourceFeature(Meme(""))

val Resource.hasMeaning
    get() = externalFeatures.any { it is MeaningResourceFeature }


val Resource.getMeaning
    get() = externalFeatures.filterIsInstance<MeaningResourceFeature>().randomElementOrNull()
