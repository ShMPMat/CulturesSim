package io.tashtabash.sim.space.tile


private fun getGenericTag(prefix: String, name: String) = TileTag("${prefix}_$name", prefix)

fun getRiverTag(name: String) = getGenericTag("River", name)

fun getLakeTag(name: String) = getGenericTag("Lake", name)

fun getMountainTag(name: String) = getGenericTag("Mountain", name)

fun getMountainsTag(name: String) = getGenericTag("Mountains", name)
