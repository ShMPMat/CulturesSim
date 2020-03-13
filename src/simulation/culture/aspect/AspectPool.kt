package simulation.culture.aspect

open class AspectPool(initialAspects: MutableSet<Aspect>) {
    protected val aspectMap = initialAspects.zip(initialAspects).toMap().toMutableMap()

    val aspects: Set<Aspect>
        get() = aspectMap.keys

    fun get(name: String) = aspects.firstOrNull { it.name == name }
            ?: throw NoSuchElementException("No aspect with name $name")

    fun getWithPredicate(predicate: (Aspect) -> Boolean): List<Aspect> = aspects
            .filter(predicate)

    fun getAll(): Set<Aspect> = aspects
}