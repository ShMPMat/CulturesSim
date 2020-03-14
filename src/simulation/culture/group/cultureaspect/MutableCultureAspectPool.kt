package simulation.culture.group.cultureaspect

class MutableCultureAspectPool(initialAspects: MutableSet<CultureAspect>) : CultureAspectPool(initialAspects) {
    fun add(aspect: CultureAspect) = aspectMap.set(aspect, aspect)

    fun addAll(aspects: Collection<CultureAspect>) = aspects.forEach { add(it) }

    fun removeAll(cultureAspects: List<CultureAspect>) = cultureAspects.forEach {
        aspectMap.remove(it)
    }
}