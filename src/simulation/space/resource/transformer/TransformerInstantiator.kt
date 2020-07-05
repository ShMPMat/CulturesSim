package simulation.space.resource.transformer

fun makeResourceTransformer(tags: Collection<String>): ResourceTransformer {
    val transformers = ArrayList<ResourceTransformer>()
    for (tag in tags)
        transformers.add(getLabel(tag.take(2), tag.drop(2)))

    return ConcatTransformer(transformers)
}

private fun getLabel(key: String, value: String): ResourceTransformer = when (key) {
    "s|" -> SizeTransformer(value.toDouble())
    else -> throw RuntimeException("Wrong tag for a labeler")
}
