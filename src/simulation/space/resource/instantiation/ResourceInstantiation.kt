package simulation.space.resource.instantiation

import extra.InputDatabase
import simulation.space.SpaceData
import simulation.space.resource.Resource
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

    private val resourceStringTemplates = ArrayList<ResourceStringTemplate>()

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
                resourceStringTemplates.add(resourceTemplateCreator.createResource(tags))
            }
        }
        SpaceData.data.resourcePool = ResourcePool(filteredFinalResources)
        resourceStringTemplates.forEach { actualizeLinks(it) }
        resourceStringTemplates.forEach { actualizeParts(it) }

        val endResources = filteredFinalResources
                .map { swapLegacies(it) as ResourceIdeal }

        SpaceData.data.resourcePool = ResourcePool(listOf())
        return finalizePool(endResources)
    }

    private val filteredFinalResources
        get() = resourceStringTemplates
                .map { (r) -> r }
                .filter { it.genome !is GenomeTemplate && !it.genome.hasLegacy }

    private fun getTemplateWithName(name: String): ResourceStringTemplate = resourceStringTemplates
            .first { it.resource.baseName == name }

    private fun actualizeLinks(template: ResourceStringTemplate) {
        val (resource, actionConversion, _) = template
        for ((a, l) in actionConversion.entries) {
            resource.genome.conversionCore.addActionConversion(
                    a,
                    l.map { readConversion(template, it) }
            )
        }
        if (resource.genome.materials.isEmpty())//TODO why is it here? What is it? (is it a GenomeTemplate check?)
            return

        for (action in actions)
            for (matcher in action.matchers)
                if (matcher.match(resource))
                    resource.genome.conversionCore.addActionConversion(
                            action,
                            matcher.getResults(template, resourceStringTemplates)
                                    .map { (t, n) -> t.resource to n }
                    )
    }

    private fun readConversion(
            template: ResourceStringTemplate,
            link: ResourceLink
    ): Pair<Resource?, Int> {
        if (link.resourceName == "LEGACY")
            return manageLegacyConversion(template.resource, link.amount)

        val nextTemplate = getTemplateWithName(link.resourceName)
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
        val legacyTemplate = getTemplateWithName(legacy)
        return legacyTemplate.resource to amount
    }

    private fun replaceLinks(resource: ResourceIdeal) {
        for (resources in resource.genome.conversionCore.actionConversion.values) {
            for (i in resources.indices) {
                val (conversionResource, conversionResourceAmount) = resources[i]
                if (conversionResource == null) {
                    resources[i] = Pair(getTemplateWithName(resource.genome.legacy!!).resource, conversionResourceAmount)//FIXME
                } else if (conversionResource.simpleName == resource.genome.name) {
                    resources[i] = Pair(resource.copy(), conversionResourceAmount)
                }
            }
        }
    }

    private fun actualizeParts(template: ResourceStringTemplate) {
        val (resource, _, parts) = template
        for (part in parts) {
            val link = parseLink(part, actions)
            val partTemplate = getTemplateWithName(link.resourceName)
            val partResource = link.transform(partTemplate.resource)
            resource.genome.addPart(partResource)
        }
        addTakeApartAction(template)
    }

    private fun addTakeApartAction(template: ResourceStringTemplate) {
        val (resource, actionConversion, _) = template
        if (resource.genome.parts.isNotEmpty()
                && !actionConversion.containsKey(actions.first { it.name == "TakeApart" })) {//TODO TakeApart shouldn't be here I recon
            val resourceList = mutableListOf<Pair<Resource?, Int>>()
            for (partResource in resource.genome.parts) {
                resourceList.add(Pair(partResource, partResource.amount))
                resource.genome.conversionCore.addActionConversion(
                        actions.first { it.name == "TakeApart" },
                        resourceList
                )
            }
        }
    }

    private fun swapLegacies(resource: Resource, legacyResource: Resource? = null): Resource {
        if (!resource.genome.hasLegacy && legacyResource != null)
            return resource

        val newGenome = resource.genome.let { oldGenome ->
            if (oldGenome is GenomeTemplate)
                oldGenome.getInstantiatedGenome(legacyResource?.genome!!)//TODO make it safe
            else oldGenome
        }
        var newResource = ResourceIdeal(newGenome.copy(legacy = legacyResource?.baseName))

        val newParts = resource.genome.parts.map {
            swapLegacies(
                    it,
                    newResource
            ).copy()
        }.toMutableList()

        newResource = ResourceIdeal(newResource.genome.copy(parts = newParts))

        val newConversionCore = ConversionCore(mutableMapOf())

        resource.genome.conversionCore.actionConversion.map { (action, results) ->
            action to results.map { (r, n) ->
                if (r == resource)
                    newResource to n
                else if (r == null)
                    legacyResource to n
                else
                    swapLegacies(r, newResource) to n
            }
        }.forEach { (a, r) -> newConversionCore.addActionConversion(a, r) }

        newResource.genome.conversionCore = newConversionCore
        return newResource
    }
}

data class ResourceStringTemplate(
        val resource: ResourceIdeal,
        val actionConversion: TemplateConversions,
        val parts: List<String>
)

typealias TemplateConversions = Map<ResourceAction, List<ResourceLink>>
