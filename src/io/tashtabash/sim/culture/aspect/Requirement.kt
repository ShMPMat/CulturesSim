package io.tashtabash.sim.culture.aspect

import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler

data class Requirement(val labeler: ResourceLabeler, val isInstrumental: Boolean)