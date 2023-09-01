package io.tashtabash.sim.culture.group.cultureaspect

import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.cultureaspect.worship.MultigroupWorshipWrapper
import io.tashtabash.sim.culture.group.cultureaspect.worship.Worship


open class CultureAspectPool(initialAspects: MutableSet<CultureAspect>) {
    protected val aspectMap = initialAspects.zip(initialAspects).toMap().toMutableMap()

    protected val aspects: Set<CultureAspect>
        get() = aspectMap.keys

    val ritualSystems
        get() = aspects.filterIsInstance<RitualSystem>()

    val depictSystems
        get() = aspects
                .filterIsInstance<DepictSystem>()
                .union(worships.map { it.depictSystem })

    val taleSystems
        get() = aspects
                .filterIsInstance<TaleSystem>()
                .union(worships.map { it.taleSystem })

    val cwDependencies: Set<ConverseWrapper>
        get() = ritualSystems
                .flatMap { it.rituals.filterIsInstance<AspectRitual>() }
                .union(aspects.filterIsInstance<AspectRitual>())
                .map { it.converseWrapper }
                .toSet()

    val cherishedResources
        get() = aspects
                .filterIsInstance<CherishedResource>()

    val worships: Set<Worship>
        get() = aspects.filterIsInstance<Worship>().toSet() +
                aspects.filterIsInstance<MultigroupWorshipWrapper>().map { it.worship }.toSet()

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
