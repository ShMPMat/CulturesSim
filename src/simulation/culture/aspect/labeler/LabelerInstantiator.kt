package simulation.culture.aspect.labeler

import simulation.space.resource.tag.labeler.makeResourceLabeler

fun makeAspectLabeler(tags: Collection<String>): AspectLabeler {
    val labelers = ArrayList<AspectLabeler>()
    for (tag in tags) {
        labelers.add(getLabel(tag.take(2), tag.drop(2)))
    }
    return ConcatLabeler(labelers)
}

private fun getLabel(key: String, value: String): AspectLabeler = when (key) {
    "P(" -> ProducedLabeler(makeResourceLabeler(value.dropLast(1).split(",")))
    "R(" -> RequiredResourceLabeler(makeResourceLabeler(value.dropLast(1).split(",")))
    "a:" -> ConverseAspectNameLabeler(value)
    else -> throw RuntimeException("Wrong tag for a labeler")
}