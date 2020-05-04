package simulation.culture.group.cultureaspect

import simulation.culture.group.centers.Group

open class CultureAspectPool(initialAspects: MutableSet<CultureAspect>) {
    protected val aspectMap = initialAspects.zip(initialAspects).toMap().toMutableMap()

    protected val aspects: Set<CultureAspect>
        get() = aspectMap.keys

    val ritualSystems
        get() = aspects
                .filterIsInstance<RitualSystem>()

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