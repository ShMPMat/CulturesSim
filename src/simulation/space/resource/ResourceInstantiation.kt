package simulation.space.resource

import extra.InputDatabase
import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectPool
import simulation.space.SpaceError
import simulation.space.tile.Tile
import simulation.space.resource.dependency.AvoidTiles
import simulation.space.resource.dependency.ConsumeDependency
import simulation.space.resource.dependency.ResourceDependency
import simulation.space.resource.dependency.ResourceNeedDependency
import simulation.space.resource.material.Material
import simulation.space.resource.material.MaterialPool
import simulation.space.resource.tag.ResourceTag

class ResourceInstantiation(
        private val path: String,
        private val aspectPool: AspectPool,
        private val materialPool: MaterialPool
) {
    private val resourceTemplates = ArrayList<ResourceTemplate>()
    var resourcePool = ResourcePool(listOf())

    fun createPool(): ResourcePool {
        val inputDatabase = InputDatabase(path)
        var line: String?
        var tags: Array<String>
        while (true) {
            line = inputDatabase.readLine() ?: break
            tags = line.split("\\s+".toRegex()).toTypedArray()
            resourceTemplates.add(createResource(tags))
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
                '+' -> aspectConversion[aspectPool.getValue(tag.substring(0, tag.indexOf(':')))] =
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
                mutableMapOf<Aspect?, MutableList<Pair<Resource?, Int?>?>?>(),
                null
        )
        return ResourceTemplate(ResourceIdeal(resourceCore), aspectConversion, parts)
    }

    private fun actualizeLinks(template: ResourceTemplate) {
        val (resource, aspectConversion, _) = template
        for (entry in aspectConversion.entries) {
            resource.resourceCore.aspectConversion[entry.key] = entry.value
                    .map {
                        readConversion(template, it)
                    }
        }
        if (resource.resourceCore.materials.isEmpty()) {
            return
        }
        for (aspect in aspectPool.getAll()) {//TODO why is it here?
            for (matcher in aspect.matchers) {
                if (matcher.match(resource.resourceCore)) {
                    resource.resourceCore.addAspectConversion(
                            aspectPool.getValue(aspect.name),
                            matcher.getResults(resource.resourceCore.copy(), resourcePool)
                    )
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
        nextTemplate = if (nextTemplate.resource.genome.hasLegacy())
            copyWithLegacyInsertion(nextTemplate, template.resource.resourceCore)
        else nextTemplate
        actualizeLinks(nextTemplate)
        return Pair(nextTemplate.resource, resourceNames[1].toInt())//TODO insert amount in Resource amount;
    }

    private fun manageLegacyConversion(
            resource: ResourceIdeal,
            amount: Int
    ): Pair<Resource?, Int> {
        if (resource.genome.legacy == null) {
            return Pair(null, amount) //TODO this is so wrong
        }
//        val legacyResource = resource.genome.legacy.copy()
        val legacyTemplate = getTemplateWithName(resource.genome.legacy.baseName)//TODO VERY DANGEROUS will break on legacy depth > 1
        return Pair(
                if (legacyTemplate.resource.genome.hasLegacy())
                    copyWithLegacyInsertion(legacyTemplate, resource.resourceCore).resource
                else
                    legacyTemplate.resource,
                amount)
    }

    private fun copyWithLegacyInsertion(
            template: ResourceTemplate,
            creator: ResourceCore
    ): ResourceTemplate {
        val (resource, aspectConversion, parts) = template
        if (template.resource.genome is GenomeTemplate) {
            return ResourceTemplate(resource, aspectConversion, parts)//TODO give instantiated resource
        }
        val legacyResource = ResourceIdeal(ResourceCore(
                resource.genome.name,
                "",
                ArrayList<Material>(resource.resourceCore.materials),
                Genome(resource.genome),
                mutableMapOf(),
                null
        ))
        val legacyTemplate = ResourceTemplate(legacyResource, aspectConversion, parts)
        actualizeLinks(legacyTemplate)
        //TODO actualize parts?
        setLegacy(legacyTemplate, creator)
        return legacyTemplate //TODO is legacy passed to parts in genome?
    }

    private fun setLegacy(template: ResourceTemplate, legacy: ResourceCore) {
        val (resource, aspectConversion, _) = template
        resource.genome.legacy = legacy
        for (entry in aspectConversion.entries) {
            if (entry.value.any { it.split(":".toRegex()).toTypedArray()[0] == "LEGACY" }) {
                resource.resourceCore.aspectConversion[entry.key] = entry.value
                        .map { readConversion(template, it) }//TODO should I do it if in upper level I call actualizeLinks?
            }
        }
        replaceLinks(resource)
    }

    private fun replaceLinks(resource: ResourceIdeal) {
        for (resources in resource.resourceCore.aspectConversion.values) {
            for (i in resources.indices) {
                val (conversionResource, conversionResourceAmount) = resources[i]
                if (conversionResource == null) {
                    resources[i] = Pair(resource.genome.legacy.copy(), conversionResourceAmount)
                } else if (conversionResource.simpleName == resource.genome.name) {
                    resources[i] = Pair(resource.resourceCore.copy(), conversionResourceAmount)
                }
            }
        }
    }

    private fun actualizeParts(template: ResourceTemplate) {
        val (resource, _, parts) = template
        for (part in parts) {
            val partResourceName = part.split(":".toRegex()).toTypedArray()[0]
            var partTemplate = getTemplateWithName(partResourceName)
            if (partTemplate.resource.resourceCore.genome.hasLegacy())//TODO seems strange
                partTemplate = copyWithLegacyInsertion(partTemplate, resource.resourceCore)

            partTemplate.resource.amount = part.split(":".toRegex()).toTypedArray()[1].toInt()
            resource.resourceCore.genome.addPart(partTemplate.resource)
        }
        addTakeApartAspect(template)
    }

    private fun addTakeApartAspect(template: ResourceTemplate) {//TODO remove it
        val (resource, aspectConversion, _) = template
        if (resource.resourceCore.genome.parts.isNotEmpty()
                && !aspectConversion.containsKey(aspectPool.getValue("TakeApart"))) {//TODO aspects shouldn't be here I recon
            val resourceList: MutableList<Pair<Resource, Int>> = ArrayList()
            for (partResource in resource.resourceCore.genome.parts) {
                resourceList.add(Pair(partResource, partResource.amount))
                resource.resourceCore.aspectConversion[aspectPool.getValue("TakeApart")] = resourceList
            }
        }
    }
}

data class ResourceTemplate(
        val resource: ResourceIdeal,
        val aspectConversion: MutableMap<Aspect, Array<String>>,
        val parts: List<String>
)