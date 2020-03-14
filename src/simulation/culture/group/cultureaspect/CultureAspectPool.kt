package simulation.culture.group.cultureaspect

open class CultureAspectPool(initialAspects: MutableSet<CultureAspect>) {
    protected val aspectMap = initialAspects.zip(initialAspects).toMap().toMutableMap()

    protected val aspects: Set<CultureAspect>
        get() = aspectMap.keys

    fun isEmpty() = aspectMap.isEmpty()

    fun get(aspect: CultureAspect) = aspectMap[aspect]
            ?: throw NoSuchElementException("No culture aspect in a pool")

    fun contains(aspect: CultureAspect) = aspectMap[aspect] != null

    fun filter(predicate: (CultureAspect) -> Boolean) = aspects
            .filter(predicate)

    fun getAll(): Set<CultureAspect> = aspects

    open fun getAspectRequests() = aspects.map { it.request }
}