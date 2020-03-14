package simulation.culture.aspect

open class AspectPool(initialAspects: MutableSet<Aspect>) {
    protected val aspectMap = initialAspects.zip(initialAspects).toMap().toMutableMap()

    protected val aspects: Set<Aspect>
        get() = aspectMap.keys

    fun getConverseWrappers() = aspects.filterIsInstance<ConverseWrapper>()

    fun getMeaningAspects() = aspects
            .filterIsInstance<ConverseWrapper>()
            .filter { it.canReturnMeaning() }
            .toSet()

    fun get(name: String) = aspects.firstOrNull { it.name == name }
            ?: throw NoSuchElementException("No aspect with name $name")

    fun get(aspect: Aspect) = aspectMap[aspect]
            ?: throw NoSuchElementException("No aspect with name ${aspect.name}")

    fun contains(aspect: Aspect) = aspectMap[aspect] != null

    fun filter(predicate: (Aspect) -> Boolean): List<Aspect> = aspects
            .filter(predicate)

    fun getAll(): Set<Aspect> = aspects

    fun getResourceRequirements() = getConverseWrappers()
            .map { it.resource } .distinct()
}