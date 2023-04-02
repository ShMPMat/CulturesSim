package io.tashtabash.simulation.culture.aspect.complexity

import io.tashtabash.simulation.space.resource.tag.ResourceTag

fun getComplexity(tagName: String): ResourceComplexity = ResourceTagComplexity(ResourceTag(tagName))