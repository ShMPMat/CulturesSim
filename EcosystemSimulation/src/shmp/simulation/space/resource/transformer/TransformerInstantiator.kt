package shmp.simulation.space.resource.transformer

import shmp.simulation.space.SpaceData
import shmp.simulation.space.resource.ResourceColour
import shmp.simulation.space.resource.ResourceTexture
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.instantiation.parseConversion
import shmp.simulation.space.resource.tag.labeler.makeResourceLabeler


class TransformerInstantiator(private val actions: List<ResourceAction>) {
    fun makeResourceTransformer(tagString: String): ResourceTransformer {
        val tags = tagString.split("#")
        val transformers = ArrayList<ResourceTransformer>()
        for (tag in tags)
            transformers.add(getLabel(tag.take(2), tag.drop(2)))

        return ConcatTransformer(transformers)
    }

    private fun getLabel(key: String, value: String): ResourceTransformer = when (key) {
        "s|" -> SizeTransformer(value.toDouble())
        "c|" -> ColourTransformer(ResourceColour.valueOf(value))
        "t|" -> TextureTransformer(ResourceTexture.valueOf(value))
        "n|" -> NameTransformer { value.split("$").joinToString(it) }
        "a|" -> {
            val (action, resources) = parseConversion(value, actions)

            val instantiatedResources = resources.map {
                it.transform(SpaceData.data.resourcePool.getSimpleName(it.resourceName)).copy(it.amount)
            }

            AddActionTransformer(action, instantiatedResources)
        }
        "P(" -> PartsPipe(makeResourceTransformer(value.dropLast(1)))
        "L(" -> {//L(labeler)(transformer)
            val (labelerTags, transformerTags) = value.dropLast(1).split(")(")
            LabelerPipe(
                    makeResourceTransformer(transformerTags),
                    makeResourceLabeler(labelerTags)
            )
        }
        else -> throw RuntimeException("Wrong tag for a transformer")
    }
}
