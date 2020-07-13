package simulation.space.resource.instantiation

import extra.InputDatabase
import simulation.space.resource.Genome
import simulation.space.resource.Resource
import simulation.space.resource.ResourceCore
import simulation.space.resource.ResourceIdeal
import simulation.space.resource.action.ConversionCore
import simulation.space.resource.action.ResourceAction
import simulation.space.resource.container.ResourcePool
import simulation.space.resource.material.MaterialPool
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

class ResourceInstantiation(
        private val folderPath: String,
        private val actions: List<ResourceAction>,
        materialPool: MaterialPool,
        amountCoefficient: Int = 1,
        tagParser: TagParser
) {
    private val resourceTemplateCreator = ResourceTemplateCreator(actions, materialPool, amountCoefficient, tagParser)

    private val resourceTemplates = ArrayList<ResourceStringTemplate>()

    fun createPool(): ResourcePool {
        val resourceFolders = Files.walk(Paths.get(folderPath))
                .toList()
                .drop(1)
        var line: String?
        var tags: Array<String>
        for (path in resourceFolders) {
            val inputDatabase = InputDatabase(path.toString())
            while (true) {
                line = inputDatabase.readLine()
                        ?: break
                tags = line.split("\\s+".toRegex()).toTypedArray()
                resourceTemplates.add(resourceTemplateCreator.createResource(tags))
            }
        }
        resourceTemplates.forEach { actualizeLinks(it) }
        resourceTemplates.forEach { actualizeParts(it) }

        return finalizePool()
    }

    private fun getTemplateWithName(name: String): ResourceStringTemplate = resourceTemplates
            .first { it.resource.baseName == name }

    private fun actualizeLinks(template: ResourceStringTemplate) {
        val (resource, actionConversion, _) = template
        for ((a, l) in actionConversion.entries) {
            resource.core.genome.conversionCore.addActionConversion(a, l
                    .map { readConversion(template, it) }
                    .toMutableList()
            )
        }
        if (resource.genome.materials.isEmpty())//TODO why is it here? What is it?
            return

        for (action in actions)
            for (matcher in action.matchers)
                if (matcher.match(resource))
                    resource.core.genome.conversionCore.addActionConversion(
                            action,
                            matcher.getResults(template, resourceTemplates)
                                    .map { (r, n) -> copyWithLegacyInsertion(r, resource.genome).resource to n }
                    )
    }

    private fun readConversion(
            template: ResourceStringTemplate,
            conversionString: String
    ): Pair<Resource?, Int> {
        val link = parseLink(conversionString)
        if (link.resourceName == "LEGACY")
            return manageLegacyConversion(template.resource, link.amount)

        var nextTemplate = getTemplateWithName(link.resourceName)
        nextTemplate = copyWithLegacyInsertion(nextTemplate, template.resource.genome)
        actualizeLinks(nextTemplate)

        val resource = link.transform(nextTemplate.resource)
        return Pair(resource, link.amount)//TODO insert amount in Resource amount;
    }

    private fun manageLegacyConversion(
            resource: ResourceIdeal,
            amount: Int
    ): Pair<Resource?, Int> {
        val legacy = resource.genome.legacy
                ?: return null to amount //TODO this is so wrong
        //        val legacyResource = resource.genome.legacy.copy()
        val legacyTemplate = getTemplateWithName(legacy.baseName)//TODO VERY DANGEROUS will break on legacy depth > 1
        return copyWithLegacyInsertion(legacyTemplate, resource.genome).resource to amount
    }

    private fun copyWithLegacyInsertion(
            template: ResourceStringTemplate,
            creator: Genome
    ): ResourceStringTemplate {
        if (!template.resource.genome.hasLegacy || template.resource.genome == creator)
            return template

        val (resource, actionConversion, parts) = template

        val legacyResource = ResourceIdeal(
                template.resource.core.genome.let { g ->
                    if (g is GenomeTemplate)
                        instantiateTemplateCopy(g, creator)
                    else resource.genome.copy()
                }
        )
        var legacyTemplate = ResourceStringTemplate(legacyResource, actionConversion, parts)
        actualizeLinks(legacyTemplate)
        //TODO actualize parts?
        if (resource.genome !is GenomeTemplate)
            legacyTemplate = setLegacy(legacyTemplate, creator)

        legacyTemplate = legacyTemplate.copy(
                resource = swapDependentResourcesLegacy(legacyTemplate.resource, template.resource.genome)
        )

        return legacyTemplate
    }

    private fun swapDependentResourcesLegacy(resource: ResourceIdeal, oldLegacy: Genome): ResourceIdeal {
        if (resource.baseName.contains("Nut_of_ConiferCone_of_Spruce")) {
            val k = 0
        }
        var swappedResource = resource

        val newParts = resource.genome.parts.map {
            val newGenome = it.genome.copy(legacy = resource.genome)
            swapDependentResourcesLegacy(
                    ResourceIdeal(newGenome),
                    it.genome
            ).copy()
        }.toMutableList()
        swappedResource = ResourceIdeal(swappedResource.genome.copy(parts = newParts))
                newParts.forEach { resource.genome.addPart(it) }

        val newConversionCore = ConversionCore(mutableMapOf())

        resource.genome.conversionCore.actionConversion.forEach { (action, resources) ->
            newConversionCore.addActionConversion(
                    action,
                    resources.map { (r, n) ->
                        val newResource =
                                if (r?.genome?.legacy == oldLegacy)
                                    swapDependentResourcesLegacy(
                                            ResourceIdeal(r.genome.copy(legacy = resource.genome)),
                                            r.genome
                                    )
                                else r?.copy()
                        newResource to n
                    }
            )
        }

        return ResourceIdeal(swappedResource.genome.copy(conversionCore = newConversionCore))
    }

    private fun instantiateTemplateCopy(genome: GenomeTemplate, legacy: Genome) =
            genome.getInstantiatedGenome(legacy)

    private fun setLegacy(template: ResourceStringTemplate, legacy: Genome): ResourceStringTemplate {
        var (resource, actionConversion, parts) = template
        val newGenome = resource.genome.copy(legacy = legacy)
        resource = ResourceIdeal(newGenome)
        for (entry in actionConversion.entries) {
            if (entry.value.any { it.split(":".toRegex()).toTypedArray()[0] == "LEGACY" }) {
                resource.core.genome.conversionCore.addActionConversion(entry.key, entry.value
                        .map { readConversion(template, it) }
                        .toMutableList())//TODO should I do it if in upper level I call actualizeLinks?
            }
        }
        replaceLinks(resource)
        return ResourceStringTemplate(resource, mutableMapOf(), parts)
    }

    private fun replaceLinks(resource: ResourceIdeal) {
        for (resources in resource.core.genome.conversionCore.actionConversion.values) {
            for (i in resources.indices) {
                val (conversionResource, conversionResourceAmount) = resources[i]
                if (conversionResource == null) {
                    resources[i] = Pair(ResourceCore(resource.genome.legacy!!).copy(), conversionResourceAmount)//FIXME
                } else if (conversionResource.simpleName == resource.genome.name) {
                    resources[i] = Pair(resource.copy(), conversionResourceAmount)
                }
            }
        }
    }

    private fun actualizeParts(template: ResourceStringTemplate) {
        val (resource, _, parts) = template
        for (part in parts) {
            val link = parseLink(part)
            var partTemplate = getTemplateWithName(link.resourceName)

            partTemplate = copyWithLegacyInsertion(partTemplate, resource.genome)

            val partResource = link.transform(partTemplate.resource)
            resource.core.genome.addPart(partResource)
        }
        addTakeApartAction(template)
    }

    private fun addTakeApartAction(template: ResourceStringTemplate) {
        val (resource, actionConversion, _) = template
        if (resource.core.genome.parts.isNotEmpty()
                && !actionConversion.containsKey(actions.first { it.name == "TakeApart" })) {//TODO TakeApart shouldn't be here I recon
            val resourceList = mutableListOf<Pair<Resource?, Int>>()
            for (partResource in resource.core.genome.parts) {
                resourceList.add(Pair(partResource, partResource.amount))
                resource.core.genome.conversionCore.addActionConversion(
                        actions.first { it.name == "TakeApart" },
                        resourceList
                )
            }
        }
    }

    private fun finalizePool(): ResourcePool {
        val finalizedResources = mutableListOf<ResourceIdeal>()
        val resourcesToAdd = mutableListOf<ResourceIdeal>()
        resourcesToAdd.addAll(resourceTemplates.map { it.resource }.filter { it.genome !is GenomeTemplate })

        while (resourcesToAdd.isNotEmpty()) {
            finalizedResources.addAll(resourcesToAdd)
            val lastResources = resourcesToAdd.toList()
            resourcesToAdd.clear()

            resourcesToAdd.addAll(
                    lastResources
                            .flatMap { it.genome.parts }
                            .map { ResourceIdeal(it.genome) }
            )
            resourcesToAdd.addAll(
                    lastResources
                            .flatMap { it.genome.conversionCore.actionConversion.values }
                            .flatten()
                            .mapNotNull { (r) -> r?.genome } //TODO why are there nulls?
                            .map { ResourceIdeal(it) }
            )

            resourcesToAdd.removeIf { it in finalizedResources }
        }

        return ResourcePool(finalizedResources.sortedBy { it.baseName })
    }
}

data class ResourceStringTemplate(
        val resource: ResourceIdeal,
        val actionConversion: MutableMap<ResourceAction, List<String>>,
        val parts: List<String>
)

typealias TemplateConversions = Map<ResourceAction, MutableList<Pair<Resource?, Int>>>

data class ResourceConversionTemplate(
        val resource: ResourceIdeal,
        val actionConversion: TemplateConversions,
        val parts: List<String>
)

