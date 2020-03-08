package simulation.space.resource.material

import extra.InputDatabase
import simulation.culture.aspect.AspectPool
import java.util.*
import java.util.function.Consumer

class MaterialInstantiation(val aspectPool: AspectPool) {
    fun createPool(path: String): MaterialPool {
        val materials: MutableList<Material> = ArrayList()
        val inputDatabase = InputDatabase(path)
        while (true) {
            val line = inputDatabase.readLine() ?: break
            val tags = line.split("\\s+".toRegex()).toTypedArray()
            materials.add(Material(tags, aspectPool))
        }
        var materialPool = MaterialPool(materials)
        materials.forEach(Consumer { material: Material -> material.actualizeLinks(materialPool) })
        return materialPool
    }
}