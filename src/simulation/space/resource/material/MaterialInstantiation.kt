package simulation.space.resource.material

import extra.InputDatabase
import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectPool
import simulation.space.resource.ResourceTag
import java.util.*
import kotlin.collections.ArrayList

class MaterialInstantiation(private val aspectPool: AspectPool) {
    fun createPool(path: String): MaterialPool {
        val materials: MutableList<MaterialTemplate> = ArrayList()
        val inputDatabase = InputDatabase(path)
        while (true) {
            val line = inputDatabase.readLine() ?: break
            val tags = line.split("\\s+".toRegex()).toTypedArray()
            materials.add(createMaterial(tags))
        }
        val materialPool = MaterialPool(materials.map { it.material })
        materials.forEach { actualizeLinks(it, materialPool) }
        return materialPool
    }

    private fun createMaterial(tags: Array<String>): MaterialTemplate {
        val aspectConversion = HashMap<Aspect, String>()
        val name = tags[0]
        val density = tags[1].toDouble()
        val materialTags = ArrayList<ResourceTag>()
        for (i in 2 until tags.size) {
            val key = tags[i][0]
            val tag = tags[i].substring(1)
            when (key) {
                '+' -> aspectConversion[aspectPool.get(tag.takeWhile { it != ':' })] =
                        tag.substring(tag.indexOf(':') + 1)
                '-' -> materialTags.add(ResourceTag(
                        tag.takeWhile { it != ':' },
                        tag.substring(tag.indexOf(':') + 1).toInt()
                ))
            }
        }
        return MaterialTemplate(
                Material(name, density, materialTags),
                aspectConversion
        )
    }

    private fun actualizeLinks(template: MaterialTemplate, materialPool: MaterialPool) {
        template.aspectConversion.forEach {
            template.material.addAspectConversion(it.key, materialPool.get(it.value))
        }
    }
}

data class MaterialTemplate(val material: Material, val aspectConversion: Map<Aspect, String>)