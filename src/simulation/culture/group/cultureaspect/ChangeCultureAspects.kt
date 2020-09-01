package simulation.culture.group.cultureaspect

import shmp.random.randomElement
import shmp.random.randomElementOrNull
import simulation.culture.group.centers.Group
import simulation.culture.group.cultureaspect.worship.GodWorship
import simulation.culture.group.cultureaspect.worship.MemeWorship
import simulation.culture.group.cultureaspect.worship.Worship
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.MemeSubject
import kotlin.random.Random

fun takeOutSimilarRituals(
        aspectPool: MutableCultureAspectPool,
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

fun takeOutSimilarTalesBy(
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
    val existing = aspectPool.worships
            .map { it.worshipObject.name }
            .toSet()
    val systems = aspectPool
            .filter { it is TaleSystem }
            .map { it as TaleSystem }
            .filter { it.groupingMeme is MemeSubject }
            .filter { it.groupingMeme !in existing }
    val system = randomElementOrNull(systems, random)
            ?: return null

    aspectPool.remove(system)
    val depictSystem = takeOutDepictionSystem(
            aspectPool,
            system.groupingMeme,
            bound = 0
    ) ?: DepictSystem(setOf(), system.groupingMeme)
    return Worship(
            MemeWorship(system.groupingMeme.copy()),
            system,
            depictSystem,
            PlaceSystem(mutableSetOf()),
            mutableListOf()
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

fun takeOutGod(
        aspectPool: MutableCultureAspectPool,
        group: Group,
        random: Random
): CultureAspect? {
    val worshipsAndCults = aspectPool.worships
            .filter { it.worshipObject is MemeWorship }

    if (worshipsAndCults.isEmpty())
        return null

    val chosen = randomElement(worshipsAndCults, random)
    val meme = chosen.worshipObject.name

    val sphereMemes = group.cultureCenter.memePool.all
            .filterIsInstance<MemeSubject>()
            .filter { it.predicates.isEmpty() }

    val sphere = randomElement(sphereMemes, { it.importance * 1.0 / it.toString().length }, random)
    val god = GodWorship(meme, sphere)

    aspectPool.remove(chosen)

    return chosen.swapWorship(god)
}
