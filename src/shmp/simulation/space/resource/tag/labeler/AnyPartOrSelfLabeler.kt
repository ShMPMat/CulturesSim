package shmp.simulation.space.resource.tag.labeler

class AnyPartOrSelfLabeler(val labeler: ResourceLabeler) : DisjointLabeler(listOf(labeler, AnyPartLabeler(labeler))) {
    override fun toString() = "Any Part or the Resource itself - ($labeler)"
}
