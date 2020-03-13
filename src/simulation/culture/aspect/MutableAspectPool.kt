package simulation.culture.aspect

class MutableAspectPool(initialAspects: MutableSet<Aspect>) : AspectPool(HashSet()) {
    private val aspectMap = initialAspects.zip(initialAspects).toMap().toMutableMap()

    override val aspects: Set<Aspect>
        get() = aspectMap.keys

    fun add(aspect: Aspect) = aspectMap.set(aspect, aspect)

    fun addAll(aspects: Collection<Aspect>) = aspects.forEach { add(it) }

    fun getConverseWrappers() = aspects.filterIsInstance<ConverseWrapper>()

    fun getMeaningAspects() = aspects
                .filterIsInstance<ConverseWrapper>()
                .filter { it.canReturnMeaning() }
                .toSet()
}