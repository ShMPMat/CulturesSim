package io.tashtabash.simulation.culture.aspect

import io.tashtabash.simulation.space.resource.tag.labeler.ResourceLabeler

data class Requirement(val labeler: ResourceLabeler, val isInstrumental: Boolean)