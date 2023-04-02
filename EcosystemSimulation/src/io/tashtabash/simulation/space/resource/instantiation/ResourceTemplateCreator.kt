package io.tashtabash.simulation.space.resource.instantiation

import io.tashtabash.simulation.DataInitializationError
import io.tashtabash.simulation.space.resource.*
import io.tashtabash.simulation.space.resource.action.ConversionCore
import io.tashtabash.simulation.space.resource.action.ResourceAction
import io.tashtabash.simulation.space.resource.dependency.*
import io.tashtabash.simulation.space.resource.instantiation.tag.TagParser
import io.tashtabash.simulation.space.resource.instantiation.tag.TagTemplate
import io.tashtabash.simulation.space.resource.material.Material
import io.tashtabash.simulation.space.resource.material.MaterialPool
import io.tashtabash.simulation.space.tile.Tile
import kotlin.math.min


class ResourceTemplateCreator(
        private val materialPool: MaterialPool,
        private val amountCoefficient: Int,
        private val tagParser: TagParser,
        private val dependencyParser: DependencyParser,
        private val conversionParser: ConversionParser
) {
    fun createResource(tags: Array<String>): ResourceStringTemplate {
        val name = tags.getOrNull(0)
                ?: throw DataInitializationError("Tags for Resource are empty")
        var resistance: Double? = null
        var danger = 0.0
        var isTemplate = false
        var isDesirable = true
        var desirability = 0
        var minTempDeprivation = 2.0
        var maxTempDeprivation = 2.0
        val resourceTags = mutableListOf<TagTemplate>()
        val resourceDependencies: MutableList<ResourceDependency> = ArrayList()
        var primaryMaterial: Material? = null
        val secondaryMaterials: MutableList<Material> = ArrayList()
        val actionConversion = mutableMapOf<ResourceAction, List<ResourceLink>>()
        val parts = mutableListOf<String>()
        var colour: ResourceColour? = null
        var texture: ResourceTexture? = null
        var shape: ResourceShape? = null
        var camouflage = 0.0
        var hasLegasy = false
        var isMovable = true

        for (i in 9..tags.lastIndex) {
            val key = tags[i][0]
            val tag = tags[i].drop(1)
            try {
                when (key) {
                    '+' -> {
                        val (action, result) = conversionParser.parse(tag)
                        actionConversion[action] = result
                    }
                    '@' -> if (tag == "TEMPLATE")
                        isTemplate = true
                    else {
                        val material = materialPool.get(tag)
                        if (primaryMaterial == null)
                            primaryMaterial = material
                        else
                            secondaryMaterials.add(material)
                    }
                    '^' -> parts.add(tag)
                    'l' -> resourceDependencies.add(LevelRestrictions(
                            tag.split(";".toRegex())[0].toInt(), tag.split(";".toRegex())[1].toInt()
                    ))
                    '~' -> {
                        try {
                            val rDependency = dependencyParser.parseUnsafe(tag)
                            resourceDependencies.add(rDependency)
                        } catch (e: ParseException) {
                            throw DataInitializationError("Failed initializing resource '$name': ${e.message}")
                        }
                    }
                    '#' -> resourceDependencies.add(AvoidTiles(
                            tag.split(":".toRegex())
                                    .map { Tile.Type.valueOf(it) }
                                    .toSet()
                    ))
                    'R' -> resistance = tag.toDouble()
                    'D' -> danger = tag.toDouble()
                    'U' -> isDesirable = false
                    's' -> shape = ResourceShape.valueOf(tag)
                    'c' -> colour = ResourceColour.valueOf(tag)
                    'C' -> camouflage = tag.toDouble()
                    't' -> texture = ResourceTexture.valueOf(tag)
                    'L' -> hasLegasy = true
                    'I' -> isMovable = false
                    'd' -> desirability = tag.toInt()
                    'T' -> {
                        val tempBound = tag.take(3)
                        val coefficient = tag.drop(3).toDouble()
                        when (tempBound) {
                            "min" -> minTempDeprivation = coefficient
                            "max" -> maxTempDeprivation = coefficient
                            else -> throw DataInitializationError("Unknown temperature command - $tag")
                        }
                    }
                    else -> {
                        val rTag = tagParser.parse(key, tag)
                                ?: throw DataInitializationError("Unknown resource description command - $key")
                        resourceTags.add(rTag)
                    }
                }
            } catch (e: NoSuchElementException) {
                println(e.message)
            } catch (e: ParseException) {//TODO remove
                println(e.message)
            }
        }

        if (tags[4] != "None")
            resourceDependencies.add(TemperatureMin(tags[4].toInt(), minTempDeprivation))
        if (tags[5] != "None")
            resourceDependencies.add(TemperatureMax(tags[5].toInt(), maxTempDeprivation))

        val lifespan =
                if (tags[3] == "inf")
                    Double.POSITIVE_INFINITY
                else
                    tags[3].toDouble()

        val sizeRange = if (tags[2].contains('~')) {
            val (l, r) = tags[2].split('~')

            l.toDouble() to r.toDouble()
        } else tags[2].toDouble().let { it to it }

        var genome = Genome(
                name = name,
                type = ResourceType.valueOf(tags[8]),
                sizeRange = sizeRange,
                spreadProbability = tags[1].toDouble(),
                baseDesirability = desirability,
                isMutable = false,
                isMovable = isMovable,
                behaviour = Behaviour(resistance ?: danger, danger, camouflage, OverflowType.valueOf(tags[7])),
                appearance = Appearance(colour, texture, shape),
                isDesirable = isDesirable,
                hasLegacy = hasLegasy,
                lifespan = lifespan,
                defaultAmount = min(tags[6].toInt() * amountCoefficient, 10e7.toInt()),
                legacy = null,
                dependencies = resourceDependencies,
                tags = setOf(),
                primaryMaterial = primaryMaterial,
                secondaryMaterials = secondaryMaterials,
                conversionCore = ConversionCore(mapOf())
        )
        if (isTemplate)
            genome = GenomeTemplate(genome, resourceTags)
        else
            genome = genome.copy(tags = resourceTags.map { it.initialize(genome) }.toSet())
        specialActions.values
                .filter { it.name[0] == '_' }
                .forEach {
                    if (!actionConversion.containsKey(it))
                        actionConversion[it] = listOf()
                }

        return ResourceStringTemplate(ResourceIdeal(genome), actionConversion, parts)
    }
}
