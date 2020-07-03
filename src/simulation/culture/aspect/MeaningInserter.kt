package simulation.culture.aspect

import simulation.culture.aspect.dependency.AspectDependencies
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.MemeSubject
import simulation.space.resource.ExternalResourceFeature
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import java.util.*


//Inserts meaning in Resources.
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
        result.resources.addAll(targetResources.map {
            val name = makePostfix(result, controller.meaning)
            it.copyWithNewExternalFeatures(listOf(MeaningResourceFeature(controller.meaning, name)))
        })

        return result
    }

    private fun makePostfix(result: AspectResult, meaning: Meme) =
            "representing_${meaning}_with_${result.node.aspect.name}"

    override fun shouldPassMeaningNeed(isMeaningNeeded: Boolean) = false

    override fun copy(dependencies: AspectDependencies): MeaningInserter {
        val copy = MeaningInserter(aspect, resource)
        copy.initDependencies(dependencies)
        val unwantedTags: MutableCollection<ResourceTag> = ArrayList()

        for (resourceTag in dependencies.map.keys)
            if (!(resourceTag.isInstrumental || resourceTag.name == "phony"))
                unwantedTags.add(resourceTag)

        unwantedTags.forEach { dependencies.map.remove(it) }
        return copy
    }
}

data class MeaningResourceFeature(val meme: Meme, override val name: String) : ExternalResourceFeature {
    override val index: Int = 0
}

private val phonyMeaningFeature = MeaningResourceFeature(MemeSubject(""), "")

val Resource.hasMeaning: Boolean
    get() = externalFeatures.any { it is MeaningResourceFeature }
