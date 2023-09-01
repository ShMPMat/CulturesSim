package io.tashtabash.sim.culture.aspect.complexity

import io.tashtabash.sim.space.resource.tag.ResourceTag

fun getComplexity(tagName: String): ResourceComplexity = ResourceTagComplexity(ResourceTag(tagName))