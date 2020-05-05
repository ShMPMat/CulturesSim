package simulation.culture.group.cultureaspect

import shmp.random.randomElement
import simulation.culture.group.centers.Group
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.MemeSubject
import kotlin.random.Random

fun takeOutSimilarRituals(
        aspectPool: MutableCultureAspectPool,
        group: Group,
        bound: Int = 3
): RitualSystem? {
    val (popularReason, popularRituals) = aspectPool
            .filter { it is Ritual }
            .map { it as Ritual }
            .groupBy { it.reason }
            .maxBy { it.value.size }
            ?: return null
    if (popularRituals.size >= bound) {
        aspectPool.removeAll(popularRituals)
        return RitualSystem(popularRituals, popularReason)
    }
    return null
}

fun takeOutSimilarTalesByTag(
        infoTag: String,
        aspectPool: MutableCultureAspectPool,
        bound: Int = 3
): TaleSystem? {
    val (popularMeme, popularTales) = aspectPool
            .filter { it is Tale }
            .map { it as Tale }
            .groupBy { it.info.getMainPart(infoTag) }
            .maxBy { it.value.size }
            ?: return null
    popularMeme ?: return null
    if (popularTales.size >= bound) {
        aspectPool.removeAll(popularTales)
        return TaleSystem(popularTales, popularMeme, infoTag);
    }
    return null
}

fun takeOutWorship(
        aspectPool: MutableCultureAspectPool,
        random: Random
): Worship? {
    val systems = aspectPool
            .filter { it is TaleSystem }
            .map { it as TaleSystem }
            .filter { it.groupingMeme is MemeSubject }
    if (systems.isEmpty()) return null
    val system = randomElement(systems, random)
    aspectPool.remove(system)
    val depictSystem = takeOutDepictionSystem(
            aspectPool,
            system.groupingMeme,
            bound = 0
    ) ?: DepictSystem(setOf(), system.groupingMeme)
    return Worship(
            system,
            depictSystem,
            PlaceSystem(mutableSetOf())
    )
}

fun takeOutDepictionSystem(
        aspectPool: MutableCultureAspectPool,
        groupingMeme: Meme,
        bound: Int = 3
): DepictSystem? {
    val depictions = aspectPool
            .filter { it is DepictObject }
            .map { it as DepictObject }
            .filter { d -> d.meme.anyMatch { it.topMemeCopy() == groupingMeme } }
    if (depictions.size >= bound) {
        aspectPool.removeAll(depictions)
        return DepictSystem(depictions, groupingMeme)
    }
    return null
}

fun takeOutCultWorship(
        aspectPool: MutableCultureAspectPool,
        random: Random
) : CultWorship? {
    val worships = aspectPool.all.filterIsInstance<Worship>()
    if (worships.isEmpty()) return null
    val worship = randomElement(worships, random)
    aspectPool.remove(worship)
    return CultWorship(worship)
}