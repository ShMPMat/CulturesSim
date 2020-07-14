package simulation.space.resource.transformer

import simulation.space.resource.tag.labeler.makeResourceLabeler

fun makeResourceTransformer(tagString: String): ResourceTransformer {
    val tags = tagString.split("#")
    val transformers = ArrayList<ResourceTransformer>()
    for (tag in tags)
        transformers.add(getLabel(tag.take(2), tag.drop(2)))

    return ConcatTransformer(transformers)
}

private fun getLabel(key: String, value: String): ResourceTransformer = when (key) {
    "s|" -> SizeTransformer(value.toDouble())
    "a|" -> TODO()
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
