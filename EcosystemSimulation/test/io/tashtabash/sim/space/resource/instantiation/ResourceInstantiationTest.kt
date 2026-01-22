package io.tashtabash.sim.space.resource.instantiation

import io.tashtabash.sim.init.getResourcePaths
import io.tashtabash.sim.space.resource.ResourceIdeal
import io.tashtabash.sim.space.resource.Resources
import io.tashtabash.sim.space.resource.action.ActionMatcher
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.instantiation.tag.DefaultTagParser
import io.tashtabash.sim.space.resource.material.MaterialInstantiation
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.createTagMatchers
import io.tashtabash.sim.space.resource.tag.labeler.TagLabeler
import io.tashtabash.sim.space.resource.transformer.NameTransformer
import io.tashtabash.utils.InputDatabase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Collections


class ResourceInstantiationTest {
    @Test
    fun `Simulation resources load correctly`() {
        val classLoader = Thread.currentThread().contextClassLoader
        val tagMatchers = createTagMatchers(classLoader.getResources("ResourceTagLabelers"))
        val tags = InputDatabase(
            Collections.enumeration(
                getResourcePaths(
                    classLoader.getResources("ResourceTags/").toList()
                )
            )
        )
            .readLines()
            .map { ResourceTag(it) }
            .union(tagMatchers.map { it.tag })
        val materialsResources = getResourcePaths(classLoader.getResources("Materials/").toList())
        val resourceResources = getResourcePaths(classLoader.getResources("Resources/").toList())

        val instantiation = MaterialInstantiation(
            tags,
            emptyList(),
            materialsResources
        )
        val materialPool = instantiation.createPool()
        val resourceInstantiation = ResourceInstantiation(
            resourceResources,
            mapOf(
                ResourceAction("Incinerate", listOf(), listOf()) to listOf(
                    ActionMatcher(
                        TagLabeler(ResourceTag("canBeIgnited")),
                        listOf("Ash" to 2, "MATCHED" to 1),
                        "Incinerate"
                    )
                )
            ),
            materialPool,
            1,
            DefaultTagParser(tags),
            listOf(
                fun(a: ResourceAction, rs: Resources): List<Pair<ResourceAction, Resources>> {
                    val ash = rs.firstOrNull { it.simpleName == "Ash" }
                        ?: return listOf()

                    val actionName = "Ultra" + a.technicalName
                    val newAction = a.copy(actionName)
                    val newResources = rs.map {
                        if (it != ash) it.exactCopy() else NameTransformer { "HotAsh" }.transform(it)
                    }

                    return listOf(newAction to newResources)
                }
            )
        )

        val resources = resourceInstantiation.createPool()

        // Check that there are no half-baked Resources
        assertTrue {
            resources.all.all { it.genome.primaryMaterial != null }
        }
        assertTrue {
            resources.all.all { it !is ResourceIdeal }
        }
        assertTrue {
            resources.all.all { it.genome !is GenomeTemplate }
        }
        assertNull(
            resources.all.firstOrNull { it.genome.parts.any { p -> p.genome.hasLegacy && !p.fullName.contains(it.fullName) } },
            "Resource with parts w/o legacy: \n" + resources.all
                .firstOrNull { it.genome.parts.any { p -> p.genome.hasLegacy && !p.fullName.contains(it.fullName) } }
                ?.let { "$it\n" + it.genome.parts.joinToString("\n") }
        )
        // Check that the injector was applied
        assertTrue {
            resources.all.any {
                it.genome.conversionCore.actionConversions.keys
                    .any { a -> a.technicalName == "UltraIncinerate" }
            }
        }
    }
}
