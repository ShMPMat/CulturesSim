package simulation.culture.group.cultureaspect

import shmp.random.randomElement
import simulation.culture.group.Group
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.MemeSubject
import kotlin.random.Random

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
            .groupBy { it.info.getMainPart(infoTag) }
            .maxBy { it.value.size }
            ?: return null
    popularMeme ?: return null
    if (popularTales.size >= 3) {
        aspectPool.removeAll(popularTales)
        return TaleSystem(group, popularTales, popularMeme, infoTag);
    }
    return null
}

fun takeOutDeity(aspectPool: MutableCultureAspectPool, group: Group, random: Random): Deity? {
    val systems = aspectPool
            .filter { it is TaleSystem }
            .map { it as TaleSystem }
            .filter { it.groupingMeme is MemeSubject }
    if (systems.isEmpty()) return null
    val system = randomElement(systems, random)
    aspectPool.remove(system)
    return Deity(
            group,
            system
    )
}

fun takeOutDepictionSystem(
        aspectPool: MutableCultureAspectPool,
        groupingMeme: Meme,
        group: Group,
        random: Random
): DepictSystem? {
    val depictions = aspectPool
            .filter { it is DepictObject }
            .map { it as DepictObject }
            .filter { d -> d.meme.anyMatch { it == groupingMeme } }
    if (depictions.isEmpty()) return null
    return DepictSystem(group, depictions, groupingMeme)
}//TODO rewrite with minimums
