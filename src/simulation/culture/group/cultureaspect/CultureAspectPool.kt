package simulation.culture.group.cultureaspect

import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.centers.Group
import simulation.culture.group.cultureaspect.worship.Worship

open class CultureAspectPool(initialAspects: MutableSet<CultureAspect>) {
    protected val aspectMap = initialAspects.zip(initialAspects).toMap().toMutableMap()

    protected val aspects: Set<CultureAspect>
        get() = aspectMap.keys

    val ritualSystems
        get() = aspects.filterIsInstance<RitualSystem>()

    val depictSystems
        get() = aspects
                .filterIsInstance<DepictSystem>()
                .union(aspects
                        .filterIsInstance<Worship>()
                        .map { it.depictSystem }
                )

    val taleSystems
        get() = aspects
                .filterIsInstance<TaleSystem>()
                .union(aspects
                        .filterIsInstance<Worship>()
                        .map { it.taleSystem }
                )

    val cwDependencies: Set<ConverseWrapper>
        get() = ritualSystems
                .flatMap { it.rituals.filterIsInstance<AspectRitual>() }
                .union(aspects.filterIsInstance<AspectRitual>())
                .map { it.converseWrapper }
                .toSet()

    val worships: Set<Worship>
        get() = aspects.filterIsInstance<Worship>().toSet()

    fun isEmpty() = aspectMap.isEmpty()

    fun get(aspect: CultureAspect) = aspectMap[aspect]
            ?: throw NoSuchElementException("No culture aspect in a pool")

    fun contains(aspect: CultureAspect) = aspectMap[aspect] != null

    fun filter(predicate: (CultureAspect) -> Boolean) = aspects
            .filter(predicate)

    val all: Set<CultureAspect>
        get() = aspects

    open fun getAspectRequests(group: Group) = aspects.mapNotNull { it.getRequest(group) }
}