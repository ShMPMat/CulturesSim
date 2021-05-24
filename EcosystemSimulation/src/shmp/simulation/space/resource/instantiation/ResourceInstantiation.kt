package shmp.simulation.space.resource.instantiation

import shmp.utils.InputDatabase
import shmp.simulation.space.SpaceData
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.ResourceIdeal
import shmp.simulation.space.resource.action.ConversionCore
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.container.ResourcePool
import shmp.simulation.space.resource.material.MaterialPool
import shmp.simulation.space.resource.specialActions
import shmp.simulation.space.resource.transformer.ColourTransformer
import java.nio.file.Files
import java.nio.file.Paths


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
        val urls = this::class.java.classLoader.getResources(folderPath).toList()

        val resourceFilePaths = urls.flatMap {
            Files.walk(Paths.get(it.toURI()))
                    .toArray()
                    .toList()
                    .drop(1)
                    .map(Any::toString)
        }

        var line: String?
        var tags: Array<String>
        val inputDatabase = InputDatabase(resourceFilePaths)
        while (true) {
            line = inputDatabase.readLine()
                    ?: break
            tags = line.split("\\s+".toRegex()).toTypedArray()
            resourceStringTemplates.add(resourceTemplateCreator.createResource(tags))
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
                            .map { it.first?.let { r -> injectParentColour(r, resource) } to it.second }
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
        val (resource, actionConversion, parts) = template
        for (part in parts) {
            val link = parseLink(part, actions)
            val partTemplate = getTemplateWithName(link.resourceName)
            var partResource = link.transform(partTemplate.resource)

            partResource = injectParentColour(partResource, resource)

            resource.genome.addPart(partResource)
        }

        addTakeApartAction(template)
    }

    private fun injectParentColour(partResource: Resource, resource: Resource): Resource {
        if (partResource.genome.appearance.colour == null)
            resource.genome.appearance.colour?.let {
                return ColourTransformer(it).transform(partResource)
            }
        return partResource
    }

    private fun addTakeApartAction(template: ResourceStringTemplate) {
        val takeApart = specialActions.getValue("TakeApart")
        val killing = specialActions.getValue("Killing")
        val (resource, actionConversion, _) = template

        if (resource.genome.parts.isNotEmpty()) {
            val result = resource.genome.parts.map { it to it.amount }

            if (!actionConversion.containsKey(takeApart) && !resource.genome.behaviour.isResisting)
                resource.genome.conversionCore.addActionConversion(takeApart, result)
            else if (!actionConversion.containsKey(killing) && resource.genome.behaviour.isResisting)
                resource.genome.conversionCore.addActionConversion(killing, result)
        }
    }

    private fun swapLegacies(
            resource: Resource,
            legacyResource: Resource? = null,
            treeStart: List<Resource> = listOf()
    ): Resource {
        if (!resource.genome.hasLegacy && legacyResource != null)
            return treeStart.firstOrNull { it.fullName == resource.fullName }
                    ?: resource //TODO damn, there should be a new Resource

        val newGenome = resource.genome.let { oldGenome ->
            if (oldGenome is GenomeTemplate)
                oldGenome.getInstantiatedGenome(legacyResource?.genome!!)//TODO make it safe
            else oldGenome
        }
        val newResource = ResourceIdeal(newGenome.copy(legacy = legacyResource?.baseName, parts = listOf()))

        val newParts = resource.genome.parts.map {
            swapLegacies(it, newResource, treeStart + listOf(newResource)).copy()
        }.toMutableList()

        newParts.forEach {
            newResource.genome.addPart(it)
        }

        val newConversionCore = ConversionCore(mutableMapOf())

        resource.genome.conversionCore.actionConversion.map { (action, results) ->
            action to results.map { (r, n) ->
                if (r == resource)
                    newResource to n
                else if (r == null)
                    treeStart[0] to n
                else
                    swapLegacies(r, newResource, treeStart + listOf(newResource)) to n
            }
        }.forEach { (a, r) -> newConversionCore.addActionConversion(a, r) }

        newResource.genome.conversionCore = newConversionCore
        return newResource
    }
}

data class ResourceStringTemplate(
        val resource: ResourceIdeal,
        val actionConversion: TemplateConversions,
        val parts: MutableList<String>
)

typealias TemplateConversions = Map<ResourceAction, List<ResourceLink>>
