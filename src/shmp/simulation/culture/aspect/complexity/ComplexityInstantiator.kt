package shmp.simulation.culture.aspect.complexity

import shmp.simulation.space.resource.tag.ResourceTag

fun getComplexity(tagName: String): ResourceComplexity = ResourceTagComplexity(ResourceTag(tagName))