package simulation.space.resource.material

class MaterialPool(private val materials: List<Material>) {
    fun get(name: String): Material {
        for (material in materials) {
            if (material.name == name) {
                return material
            }
        }
        throw NoSuchElementException("No material with name $name")
    }

    fun getWithPredicate(predicate: (Material) -> Boolean): List<Material> = materials
            .filter(predicate)
            .map { it }

    fun getAll(): List<Material> = materials
}