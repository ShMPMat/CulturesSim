package simulation.culture.group.cultureaspect

import simulation.culture.group.Group

fun takeOutSimilarRituals(aspectPool: MutableCultureAspectPool, group: Group): RitualSystem? {
    val (popularReason, popularRituals) = aspectPool
            .filter { it is Ritual }
            .map { it as Ritual }
            .groupBy { it.reason }
            .maxBy { it.value.size }
            ?: return null
    if (popularRituals.size >= 3) {
        aspectPool.removeAll(popularRituals)
        return RitualSystem(group, popularRituals, popularReason)
    }
    return null
}

fun takeOutSimilarTalesByTag(infoTag: String, aspectPool: MutableCultureAspectPool, group: Group): TaleSystem? {
    val (popularMeme, popularTales) = aspectPool
            .filter { it is Tale }
            .map { it as Tale }
            .groupBy { it.info.map[infoTag] }//TODO its hardcoded... and strange
            .maxBy { it.value.size }
            ?: return null
    popularMeme ?: return null
    if (popularTales.size >= 3) {
        aspectPool.removeAll(popularTales)
        return TaleSystem(group, popularTales, popularMeme, infoTag);
    }
    return null
}
