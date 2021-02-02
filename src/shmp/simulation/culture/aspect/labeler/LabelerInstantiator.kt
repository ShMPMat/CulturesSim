package shmp.simulation.culture.aspect.labeler


import shmp.simulation.space.resource.tag.labeler.makeResourceLabeler

fun makeAspectLabeler(tags: Collection<String>): AspectLabeler {
    val labelers = ArrayList<AspectLabeler>()
    for (tag in tags) {
        labelers.add(getLabel(tag.take(2), tag.drop(2)))
    }
    return ConcatLabeler(labelers)
}

private fun getLabel(key: String, value: String): AspectLabeler = when (key) {
    "P(" -> ProducedLabeler(makeResourceLabeler(value.dropLast(1)))
    "R(" -> RequiredResourceLabeler(makeResourceLabeler(value.dropLast(1)))
    "a(" -> ConverseAspectNameLabeler(value.dropLast(1))
    "D(" -> DisjointLabeler(value.dropLast(1).split(":").map { getLabel(it.take(2), it.drop(2)) })
    else -> throw RuntimeException("Wrong tag for a labeler")
}