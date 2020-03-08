package simulation.space.resource

import extra.InputDatabase
import extra.ShnyPair
import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectPool
import simulation.space.SpaceError
import simulation.space.Tile
import simulation.space.resource.dependency.AvoidTiles
import simulation.space.resource.dependency.ConsumeDependency
import simulation.space.resource.dependency.ResourceDependency
import simulation.space.resource.dependency.ResourceNeedDependency
import simulation.space.resource.material.Material
import simulation.space.resource.material.MaterialPool
import java.util.*

class ResourceInstantiation(
        private val path: String,
        private val aspectPool: AspectPool,
        private val materialPool: MaterialPool
) {
    private val resourceTemplates = ArrayList<ResourceTemplate>()

    fun createPool(): ResourcePool {
        val inputDatabase = InputDatabase(path)
        var line: String?
        var tags: Array<String>
        while (true) {
            line = inputDatabase.readLine() ?: break
            tags = line.split("\\s+".toRegex()).toTypedArray()
            resourceTemplates.add(createResource(tags))
        }
        val resourcePool = ResourcePool(resourceTemplates.map { it.resourceIdeal })
        resourceTemplates.forEach { actualizeLinks(it, resourcePool) }//TODO remove all Controller calls
        resourceTemplates.forEach { actualizeParts(it, resourcePool) }//TODO Maybe legacy resources dont have parts!
        return resourcePool
    }

    private fun createResource(tags: Array<String>): ResourceTemplate {
        val name = tags.getOrNull(0) ?: throw SpaceError("Tags for Resource are empty")
        var willResist = false
        var isTemplate = false
        val resourceTags: MutableList<ResourceTag> = ArrayList()
        val resourceDependencies: MutableList<ResourceDependency> = ArrayList()
        var primaryMaterial: Material? = null
        val secondaryMaterials: MutableList<Material> = ArrayList()
        var elements: Array<String>
        val aspectConversion = mutableMapOf<Aspect, Array<String>>()
        val parts = mutableListOf<String>()

        for (i in 12..tags.lastIndex) {
            val key = tags[i][0]
            val tag = tags[i].substring(1)
            when (key) {
                '+' -> aspectConversion[aspectPool.get(tag.substring(0, tag.indexOf(':')))] =
                        tag.substring(tag.indexOf(':') + 1).split(",".toRegex()).toTypedArray()
                '@' -> {
                    if (tag == "TEMPLATE") {
                        isTemplate = true
                    } else {
                        val material = materialPool.get(tag)
                        if (primaryMaterial == null) {
                            primaryMaterial = material
                        } else {
                            secondaryMaterials.add(material)
                        }
                    }
                }
                '^' -> parts.add(tag)
                '~' -> {
                    elements = tag.split(":".toRegex()).toTypedArray()
                    if (elements[4] == "CONSUME") {
                        resourceDependencies.add(ConsumeDependency(
                                elements[2].toDouble(),
                                elements[3] == "1",
                                elements[1].toDouble(),
                                listOf(*elements[0].split(",".toRegex()).toTypedArray())
                        ))
                    } else {
                        resourceDependencies.add(ResourceNeedDependency(
                                ResourceNeedDependency.Type.valueOf(elements[4]),
                                elements[1].toDouble(),
                                elements[2].toDouble(),
                                elements[3] == "1",
                                listOf(*elements[0].split(",".toRegex()).toTypedArray())
                        ))
                    }
                }
                '#' -> resourceDependencies.add(AvoidTiles(
                        tag.split(":".toRegex()).toTypedArray()
                                .map { Tile.Type.valueOf(it) }
                                .toSet()
                ))
                '$' -> {
                    elements = tag.split(":".toRegex()).toTypedArray()
                    resourceTags.add(ResourceTag(elements[0], elements[1].toInt()))
                }
                'R' -> willResist = true
            }
        }
        var genome = Genome(
                name,
                Genome.Type.valueOf(tags[11]),
                tags[2].toDouble(),
                tags[1].toDouble(),
                tags[4].toInt(),
                tags[5].toInt(),
                tags[7].toInt(),
                tags[9] == "1",
                false,
                tags[8] == "1",
                willResist,
                tags[9] == "1",
                tags[3].toInt(),
                tags[6].toInt(),
                null,
                null,
                resourceDependencies,
                resourceTags,
                primaryMaterial,
                secondaryMaterials)
        if (isTemplate) {
            genome = GenomeTemplate(genome)
        }
        val resourceCore = ResourceCore(
                genome.name,
                "",
                genome.materials,
                genome,
                mutableMapOf<Aspect?, MutableList<ShnyPair<Resource?, Int?>?>?>(),
                null
        )
        return ResourceTemplate(ResourceIdeal(resourceCore), mutableMapOf(), mutableListOf())
    }

    private fun actualizeLinks(template: ResourceTemplate, resourcePool: ResourcePool) {
        val (resource, aspectConversion, _) = template
        for (entry in aspectConversion.entries) {
            resource.resourceCore.aspectConversion[entry.key] = entry.value
                    .map {
                        readConversion(resource, it, resourcePool)
                                ?: throw SpaceError("Impossible error")
                    }
        }
        if (resource.resourceCore.materials.isEmpty()) {
            return
        }
        for (aspect in aspectPool.getAll()) {//TODO why is it here?
            for (matcher in aspect.matchers) {
                if (matcher.match(resource.resourceCore)) {
                    resource.resourceCore.addAspectConversion(
                            aspect.name,
                            matcher.getResults(resource.resourceCore.copy(), resourcePool)
                    )
                }
            }
        }
    }

    private fun readConversion(
            resource: ResourceIdeal,
            s: String,
            resourcePool: ResourcePool
    ): ShnyPair<Resource?, Int>? {//TODO ordinary Pair pls
        val resourceNames = s.split(":".toRegex()).toTypedArray()
        if (resourceNames[0] == "LEGACY") {
            if (resource.genome.legacy == null) { //System.err.println("No legacy for LEGACY conversion in genome " + genome.getName());
                return ShnyPair(null, resourceNames[1].toInt()) //TODO insert legacy in another place
            }
            val legacyResource: Resource = resource.genome.legacy.copy()
            return ShnyPair(
                    if (legacyResource.genome.hasLegacy())
                        legacyResource.resourceCore.copyWithLegacyInsertion(resource.resourceCore, resourcePool)
                    else
                        legacyResource, resourceNames[1].toInt())
        }
        var nextTemplate: ResourceTemplate = resourceTemplates.first { it.resourceIdeal.baseName == resourceNames[0] }
        val templateResource = if (nextTemplate.resourceIdeal.genome.hasLegacy())
            nextTemplate.resourceIdeal.resourceCore.copyWithLegacyInsertion(resource.resourceCore, resourcePool)
        else nextTemplate.resourceIdeal
        actualizeLinks(ResourceTemplate(templateResource, nextTemplate.aspectConversion, listOf()), resourcePool)
        return ShnyPair(nextTemplate.resourceIdeal, resourceNames[1].toInt())//TODO insert amount in Resource amount;
    }

    fun actualizeParts(template: ResourceTemplate, resourcePool: ResourcePool) {
        val (resource, aspectConversion, parts) = template
        for (part in parts) {
            var partResource = resourcePool.get(part.split(":".toRegex()).toTypedArray()[0])
            partResource = if (partResource.resourceCore.genome.hasLegacy())//TODO seems strange
                partResource.resourceCore.copyWithLegacyInsertion(resource.resourceCore, resourcePool)
            else
                partResource
            partResource.amount = part.split(":".toRegex()).toTypedArray()[1].toInt()
            resource.resourceCore.genome.addPart(partResource)
        }
        if (resource.resourceCore.genome.parts.isNotEmpty()
                && !aspectConversion.containsKey(aspectPool.get("TakeApart"))) {//TODO aspects shouldn't be here I recon
            val resourceList: MutableList<ShnyPair<Resource, Int>> = ArrayList()
            for (partResource in resource.resourceCore.genome.getParts()) {
                resourceList.add(ShnyPair(partResource, partResource.amount))
                resource.resourceCore.aspectConversion[aspectPool.get("TakeApart")] = resourceList
            }
        }
    }
}

data class ResourceTemplate(
        val resourceIdeal: ResourceIdeal,
        val aspectConversion: Map<Aspect, Array<String>>,
        val parts: List<String>
)