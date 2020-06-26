package simulation.space.resource.instantiation

import extra.InputDatabase
import simulation.SimulationException
import simulation.space.SpaceError
import simulation.space.resource.*
import simulation.space.resource.dependency.AvoidTiles
import simulation.space.resource.dependency.LevelRestrictions
import simulation.space.resource.dependency.ResourceDependency
import simulation.space.resource.material.Material
import simulation.space.resource.material.MaterialPool
import simulation.space.resource.tag.ResourceTag
import simulation.space.tile.Tile
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.min
import kotlin.streams.toList

class ResourceInstantiation(
        private val folderPath: String,
        private val actions: List<ResourceAction>,
        private val materialPool: MaterialPool,
        private val amountCoefficient: Int = 1,
        private val tagParser: TagParser
) {
    private val resourceTemplates = ArrayList<ResourceTemplate>()
    private val dependencyParser = DefaultDependencyParser()
    var resourcePool = ResourcePool(listOf())

    fun createPool(): ResourcePool {
        val resourceFolders = Files.walk(Paths.get(folderPath)).toList().drop(1)
        var line: String?
        var tags: Array<String>
        for (path in resourceFolders) {
            val inputDatabase = InputDatabase(path.toString())
            while (true) {
                line = inputDatabase.readLine() ?: break
                tags = line.split("\\s+".toRegex()).toTypedArray()
                resourceTemplates.add(createResource(tags))
            }
        }
        resourcePool = ResourcePool(resourceTemplates.map { it.resource })
        resourceTemplates.forEach { actualizeLinks(it) }
        resourceTemplates.forEach { actualizeParts(it) }//TODO Maybe legacy resources dont have parts!
        return resourcePool
    }

    private fun getTemplateWithName(name: String): ResourceTemplate =
            resourceTemplates.first { it.resource.baseName == name }

    private fun createResource(tags: Array<String>): ResourceTemplate {
        val name = tags.getOrNull(0) ?: throw SpaceError("Tags for Resource are empty")
        var willResist = false
        var isTemplate = false
        var isDesirable = true
        val resourceTags: MutableList<ResourceTag> = ArrayList()
        val resourceDependencies: MutableList<ResourceDependency> = ArrayList()
        var primaryMaterial: Material? = null
        val secondaryMaterials: MutableList<Material> = ArrayList()
        val actionConversion = mutableMapOf<ResourceAction, Array<String>>()
        val parts = mutableListOf<String>()

        for (i in 12..tags.lastIndex) {
            val key = tags[i][0]
            val tag = tags[i].substring(1)
            when (key) {
                '+' -> {
                    val actionName = tag.substring(0, tag.indexOf(':'))
                    val action = when (actionName) {
                        DEATH_ACTION.name -> DEATH_ACTION
                        EACH_TURN_ACTION.name -> EACH_TURN_ACTION
                        else -> actions.first { it.name == actionName }
                    }
                    actionConversion[action] =
                            tag.substring(tag.indexOf(':') + 1).split(",".toRegex()).toTypedArray()
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
                            ?: throw SimulationException("Unknown dependency with tags: $tag")
                    resourceDependencies.add(rDependency)
                }
                '#' -> resourceDependencies.add(AvoidTiles(
                        tag.split(":".toRegex()).toTypedArray()
                                .map { Tile.Type.valueOf(it) }
                                .toSet()
                ))
                'R' -> willResist = true
                'U' -> isDesirable = false
                else -> {
                    val rTag = tagParser.parse(key, tag)
                            ?: throw ExceptionInInitializerError("Unknown resource description command - $key")
                    resourceTags.add(rTag)
                }
            }
        }
        var genome = Genome(
                name = name,
                type = Genome.Type.valueOf(tags[11]),
                size = tags[2].toDouble(),
                spreadProbability = tags[1].toDouble(),
                temperatureMin = tags[4].toInt(),
                temperatureMax = tags[5].toInt(),
                baseDesirability = tags[7].toInt(),
                canMove = tags[9] == "1",
                isMutable = false,
                isMovable = tags[8] == "1",
                isResisting = willResist,
                isDesirable = isDesirable,
                hasLegacy = tags[9] == "1",
                deathTime = tags[3].toInt(),
                defaultAmount = min(tags[6].toInt() * amountCoefficient, 10e7.toInt()),
                legacy = null,
                templateLegacy = null,
                dependencies = resourceDependencies,
                tags = resourceTags,
                primaryMaterial = primaryMaterial,
                secondaryMaterials = secondaryMaterials
        )
        if (isTemplate)
            genome = GenomeTemplate(genome)
        val resourceCore = ResourceCore(
                genome.name,
                genome.materials,
                genome,
                mapOf()
        )
        if (!actionConversion.containsKey(DEATH_ACTION))
            actionConversion[DEATH_ACTION] = arrayOf()
        if (!actionConversion.containsKey(EACH_TURN_ACTION))
            actionConversion[EACH_TURN_ACTION] = arrayOf()

        return ResourceTemplate(ResourceIdeal(resourceCore), actionConversion, parts)
    }

    private fun actualizeLinks(template: ResourceTemplate) {
        val (resource, actionConversion, _) = template
        for (entry in actionConversion.entries) {
            resource.core.actionConversion[entry.key] = entry.value
                    .map { readConversion(template, it) }
                    .toMutableList()
        }
        if (resource.core.materials.isEmpty())
            return

        for (action in actions) {
            for (matcher in action.matchers) {
                if (matcher.match(resource)) {
                    resource.core.addActionConversion(action, matcher.getResults(resource.core.copy(), resourcePool))
                }
            }
        }
    }

    private fun readConversion(
            template: ResourceTemplate,
            conversionString: String
    ): Pair<Resource?, Int> {
        val resourceNames = conversionString.split(":".toRegex()).toTypedArray()
        if (resourceNames[0] == "LEGACY") {
            return manageLegacyConversion(template.resource, resourceNames[1].toInt())
        }
        var nextTemplate: ResourceTemplate = getTemplateWithName(resourceNames[0])
        nextTemplate = if (nextTemplate.resource.genome.hasLegacy)
            copyWithLegacyInsertion(nextTemplate, template.resource.core)
        else nextTemplate
        actualizeLinks(nextTemplate)
        return Pair(nextTemplate.resource, resourceNames[1].toInt())//TODO insert amount in Resource amount;
    }

    private fun manageLegacyConversion(
            resource: ResourceIdeal,
            amount: Int
    ): Pair<Resource?, Int> {
        val legacy = resource.genome.legacy //TODO this is so wrong
        if (legacy == null) return Pair(null, amount)
        //        val legacyResource = resource.genome.legacy.copy()
        val legacyTemplate = getTemplateWithName(legacy.genome.baseName)//TODO VERY DANGEROUS will break on legacy depth > 1
        return Pair(
                if (legacyTemplate.resource.genome.hasLegacy)
                    copyWithLegacyInsertion(legacyTemplate, resource.core).resource
                else
                    legacyTemplate.resource,
                amount)
    }

    private fun copyWithLegacyInsertion(
            template: ResourceTemplate,
            creator: ResourceCore
    ): ResourceTemplate {
        val (resource, actionConversion, parts) = template
        if (template.resource.genome is GenomeTemplate) {
            return ResourceTemplate(resource, actionConversion, parts)//TODO give instantiated resource
        }
        val legacyResource = ResourceIdeal(ResourceCore(
                resource.genome.name,
                ArrayList<Material>(resource.core.materials),
                resource.genome.copy(),
                mutableMapOf()
        ))
        val legacyTemplate = ResourceTemplate(legacyResource, actionConversion, parts)
        actualizeLinks(legacyTemplate)
        //TODO actualize parts?
        setLegacy(legacyTemplate, creator)
        return legacyTemplate //TODO is legacy passed to parts in genome?
    }

    private fun setLegacy(template: ResourceTemplate, legacy: ResourceCore) {
        val (resource, actionConversion, _) = template
        resource.genome.legacy = legacy
        for (entry in actionConversion.entries) {
            if (entry.value.any { it.split(":".toRegex()).toTypedArray()[0] == "LEGACY" }) {
                resource.core.actionConversion[entry.key] = entry.value
                        .map { readConversion(template, it) }
                        .toMutableList()//TODO should I do it if in upper level I call actualizeLinks?
            }
        }
        replaceLinks(resource)
    }

    private fun replaceLinks(resource: ResourceIdeal) {
        for (resources in resource.core.actionConversion.values) {
            for (i in resources.indices) {
                val (conversionResource, conversionResourceAmount) = resources[i]
                if (conversionResource == null) {
                    resources[i] = Pair(resource.genome.legacy!!.copy(), conversionResourceAmount)//FIXME
                } else if (conversionResource.simpleName == resource.genome.name) {
                    resources[i] = Pair(resource.core.copy(), conversionResourceAmount)
                }
            }
        }
    }

    private fun actualizeParts(template: ResourceTemplate) {
        val (resource, _, parts) = template
        for (part in parts) {
            val partResourceName = part.split(":".toRegex()).toTypedArray()[0]
            var partTemplate = getTemplateWithName(partResourceName)
            if (partTemplate.resource.core.genome.hasLegacy)//TODO seems strange
                partTemplate = copyWithLegacyInsertion(partTemplate, resource.core)

            partTemplate.resource.amount = part.split(":".toRegex()).toTypedArray()[1].toInt()
            resource.core.genome.addPart(partTemplate.resource)
        }
        addTakeApartAction(template)
    }

    private fun addTakeApartAction(template: ResourceTemplate) {
        val (resource, actionConversion, _) = template
        if (resource.core.genome.parts.isNotEmpty()
                && !actionConversion.containsKey(actions.first { it.name == "TakeApart" })) {//TODO TakeApart shouldn't be here I recon
            val resourceList = mutableListOf<Pair<Resource?, Int>>()
            for (partResource in resource.core.genome.parts) {
                resourceList.add(Pair(partResource, partResource.amount))
                resource.core.actionConversion[actions.first { it.name == "TakeApart" }] = resourceList
            }
        }
    }
}

data class ResourceTemplate(
        val resource: ResourceIdeal,
        val actionConversion: MutableMap<ResourceAction, Array<String>>,
        val parts: List<String>
)