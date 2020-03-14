package simulation.culture.aspect

class MutableAspectPool(initialAspects: MutableSet<Aspect>) : AspectPool(initialAspects) {
    fun add(aspect: Aspect) = aspectMap.set(aspect, aspect)

    fun addAll(aspects: Collection<Aspect>) = aspects.forEach { add(it) }
}