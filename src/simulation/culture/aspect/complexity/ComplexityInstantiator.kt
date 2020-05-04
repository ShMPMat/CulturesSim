package simulation.culture.aspect.complexity

import simulation.space.resource.tag.ResourceTag

fun getComplexity(tagName: String): ResourceComplexity = ResourceTagComplexity(ResourceTag(tagName))