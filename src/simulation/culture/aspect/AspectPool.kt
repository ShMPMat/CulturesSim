package simulation.culture.aspect

class AspectPool(private val aspects: List<Aspect>) {
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

    fun getAll(): List<Aspect> = aspects
}