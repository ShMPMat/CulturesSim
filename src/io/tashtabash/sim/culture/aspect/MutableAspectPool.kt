package io.tashtabash.sim.culture.aspect


class MutableAspectPool(initialAspects: MutableSet<Aspect>) : AspectPool(initialAspects) {
    fun add(aspect: Aspect) = innerAdd(aspect)

    fun remove(aspect: Aspect) : Boolean = innerRemove(aspect)

    fun addAll(aspects: Collection<Aspect>) = aspects.forEach { add(it) }

    fun clear() = aspectMap.clear()
}
