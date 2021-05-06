package shmp.simulation.space.resource.instantiation

import shmp.simulation.DataInitializationError
import shmp.simulation.space.resource.*
import shmp.simulation.space.resource.action.ConversionCore
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.dependency.*
import shmp.simulation.space.resource.material.Material
import shmp.simulation.space.resource.material.MaterialPool
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.tile.Tile
import kotlin.math.min


class ResourceTemplateCreator(
        private val actions: List<ResourceAction>,
        private val materialPool: MaterialPool,
        private val amountCoefficient: Int,
        private val tagParser: TagParser
) {
    private val dependencyParser = DefaultDependencyParser()

    fun createResource(tags: Array<String>): ResourceStringTemplate {
        val name = tags.getOrNull(0)
                ?: throw DataInitializationError("Tags for Resource are empty")
        var willResist = false
        var isTemplate = false
        var isDesirable = true
        var minTempDeprivation = 2.0
        var maxTempDeprivation = 2.0
        val resourceTags = mutableSetOf<ResourceTag>()
        val resourceDependencies: MutableList<ResourceDependency> = ArrayList()
        var primaryMaterial: Material? = null
        val secondaryMaterials: MutableList<Material> = ArrayList()
        val actionConversion = mutableMapOf<ResourceAction, List<ResourceLink>>()
        val parts = mutableListOf<String>()

        for (i in 12..tags.lastIndex) {
            val key = tags[i][0]
            val tag = tags[i].drop(1)
            try {
                when (key) {
                    '+' -> {
                        val (action, result) = parseConversion(tag, actions)
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
                        val rDependency = dependencyParser.parse(tag)
                                ?: throw DataInitializationError("Unknown dependency with tags: $tag")
                        resourceDependencies.add(rDependency)
                    }
                    '#' -> resourceDependencies.add(AvoidTiles(
                            tag.split(":".toRegex()).toTypedArray()
                                    .map { Tile.Type.valueOf(it) }
                                    .toSet()
                    ))
                    'R' -> willResist = true
                    'U' -> isDesirable = false
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

        var genome = Genome(
                name = name,
                type = ResourceType.valueOf(tags[11]),
                size = tags[2].toDouble(),
                spreadProbability = tags[1].toDouble(),
                baseDesirability = tags[7].toInt(),
                overflowType = OverflowType.valueOf(tags[10]),
                isMutable = false,
                isMovable = tags[8] == "1",
                isResisting = willResist,
                isDesirable = isDesirable,
                hasLegacy = tags[9] == "1",
                lifespan = lifespan,
                defaultAmount = min(tags[6].toInt() * amountCoefficient, 10e7.toInt()),
                legacy = null,
                dependencies = resourceDependencies,
                tags = resourceTags,
                primaryMaterial = primaryMaterial,
                secondaryMaterials = secondaryMaterials,
                conversionCore = ConversionCore(mapOf())
        )
        if (isTemplate)
            genome = GenomeTemplate(genome)
        specialActions.values.forEach {
            if (!actionConversion.containsKey(it))
                actionConversion[it] = listOf()
        }

        return ResourceStringTemplate(ResourceIdeal(genome), actionConversion, parts)
    }
}
