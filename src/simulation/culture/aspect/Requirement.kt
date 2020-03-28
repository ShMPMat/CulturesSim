package simulation.culture.aspect

import simulation.space.resource.tag.labeler.ResourceTagLabeler

data class Requirement(val labeler: ResourceTagLabeler, val isInstrumental: Boolean)