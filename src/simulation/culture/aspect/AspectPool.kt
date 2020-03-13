package simulation.culture.aspect

open class AspectPool(protected open val aspects: Set<Aspect>) {
    fun get(name: String): Aspect {
        for (aspect in aspects) {
            if (aspect.name == name) {
                return aspect
            }
        }
        throw NoSuchElementException("No aspect with name $name")
    }

    fun getWithPredicate(predicate: (Aspect) -> Boolean): List<Aspect> = aspects
            .filter(predicate)

    fun getAll(): Set<Aspect> = aspects
}