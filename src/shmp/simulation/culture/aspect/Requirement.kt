package shmp.simulation.culture.aspect

import shmp.simulation.space.resource.tag.labeler.ResourceLabeler

data class Requirement(val labeler: ResourceLabeler, val isInstrumental: Boolean)