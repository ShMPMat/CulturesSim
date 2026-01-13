package io.tashtabash.sim.space.resource.material

import io.tashtabash.utils.InputDatabase
import io.tashtabash.sim.DataInitializationError
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.instantiation.getResourcePaths
import io.tashtabash.sim.space.resource.tag.ResourceTag
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList


class MaterialInstantiation(
        private val allowedTags: Collection<ResourceTag>,
        private val actions: List<ResourceAction>
) {
    fun createPool(): MaterialPool {
        val materials = mutableListOf<MaterialTemplate>()
        val classLoader = Thread.currentThread().contextClassLoader
        val inputDatabase = InputDatabase(Collections.enumeration(getResourcePaths(classLoader.getResources("Materials/").toList())))
        while (true) {
            val line = inputDatabase.readLine()
                ?: break
            val tags = line.split("\\s+".toRegex()).toTypedArray()
            materials.add(createMaterial(tags))
        }
        val materialPool = MaterialPool(materials.map { it.material })
        for (it in materials)
            actualizeLinks(it, materialPool)

        return materialPool
    }

    private fun createMaterial(tags: Array<String>): MaterialTemplate {
        val aspectConversion = HashMap<ResourceAction, String>()
        val name = tags[0]
        val density = tags[1].toDouble()
        val materialTags = ArrayList<ResourceTag>()
        for (i in 2 until tags.size) {
            val key = tags[i][0]
            val tag = tags[i].substring(1)
            try {
                when (key) {
                    '+' -> aspectConversion[actions.first { a -> a.technicalName == tag.takeWhile { it != ':' } }] =
                            tag.substring(tag.indexOf(':') + 1)
                    '-' -> {
                        val resourceTag = ResourceTag(
                                tag.takeWhile { it != ':' },
                                tag.substring(tag.indexOf(':') + 1).toDouble()
                        )
                        if (!allowedTags.contains(resourceTag))
                            throw DataInitializationError("Tag $resourceTag doesn't exist")
                        materialTags.add(resourceTag)
                    }
                }
            } catch (e: NoSuchElementException) {
                println(e.message)
            }
        }
        return MaterialTemplate(
                Material(name, density, materialTags),
                aspectConversion
        )
    }

    private fun actualizeLinks(template: MaterialTemplate, materialPool: MaterialPool) {
        template.actionConversion.forEach {
            template.material.addActionConversion(it.key, materialPool.get(it.value))
        }
    }
}

data class MaterialTemplate(val material: Material, val actionConversion: Map<ResourceAction, String>)
