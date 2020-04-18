package simulation.space.resource.tag.labeler

class AnyPartOrSelfLabeler(labeler: ResourceLabeler) : DisjointLabeler(listOf(labeler, AnyPartLabeler(labeler)))