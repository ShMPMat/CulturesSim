package shmp.simulation.space.resource.instantiation

import shmp.utils.InputDatabase
import shmp.simulation.space.SpaceData
import shmp.simulation.space.resource.*
import shmp.simulation.space.resource.action.ConversionCore
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.container.ResourcePool
import shmp.simulation.space.resource.material.MaterialPool
import shmp.simulation.space.resource.transformer.ColourTransformer
import shmp.simulation.space.resource.transformer.TextureTransformer
import java.nio.file.Files
import java.nio.file.Paths


class ResourceInstantiation(
        private val folderPath: String,
        private val actions: List<ResourceAction>,
        materialPool: MaterialPool,
        amountCoefficient: Int = 1,
        tagParser: TagParser,
        private val resourceActionInjectors: List<ResourceActionInjector>
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
                                    .map { (t, n) -> t.resource.copy(n) }
                    )
    }

    private fun readConversion(template: ResourceStringTemplate, link: ResourceLink): Resource {
        if (link.resourceName == "LEGACY")
            return manageLegacyConversion(template.resource, link.amount)

        val nextTemplate = getTemplateWithName(link.resourceName)
        actualizeLinks(nextTemplate)
        val resource = link.transform(nextTemplate.resource)
        return resource.copy(link.amount)
    }

    private fun manageLegacyConversion(resource: ResourceIdeal, amount: Int): Resource {
        val legacy = resource.genome.legacy
                ?: return phonyResource.copy(amount)
        val legacyTemplate = getTemplateWithName(legacy)
        return legacyTemplate.resource.copy(amount)
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

    private fun Resource.injectAppearance(referenceResource: Resource): Resource {
        var result = this

        if (genome.appearance.colour == null)
            referenceResource.genome.appearance.colour?.let {
                result = ColourTransformer(it).transform(result).exactCopy()
            }
        if (genome.appearance.texture == null)
            referenceResource.genome.appearance.texture?.let {
                result = TextureTransformer(it).transform(result).exactCopy()
            }

        return result
    }

    private fun addTakeApartAction(template: ResourceStringTemplate) {
        val takeApart = specialActions.getValue("TakeApart")
        val killing = specialActions.getValue("Killing")
        val (resource, actionConversion, _) = template

        if (resource.genome.parts.isNotEmpty()) {
            val result = resource.genome.parts.toList()

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
        val newResource = ResourceIdeal(newGenome.copy(
                legacy = legacyResource?.baseName,
                parts = listOf(),
                primaryMaterial = newGenome.primaryMaterial ?: legacyResource?.genome?.primaryMaterial
        ), resource.amount)

        val newParts = resource.genome.parts.map {
            swapLegacies(it.injectAppearance(resource), newResource, treeStart + listOf(newResource)).copy(it.amount)
        }.toMutableList()

        newParts.forEach {
            newResource.genome.addPart(it)
        }

        val newConversionCore = ConversionCore(mutableMapOf())

        resource.genome.conversionCore.actionConversion.map { (action, results) ->
            action to results.map { r ->
                when (r) {
                    resource -> newResource.copy(r.amount)
                    phonyResource -> treeStart[0].copy(r.amount)
                    else -> swapLegacies(r.injectAppearance(resource), newResource, treeStart + listOf(newResource))
                }
            }
        }.forEach { (a, r) -> newConversionCore.addActionConversion(a, r) }
        injectBuildings(newConversionCore)

        newResource.genome.conversionCore = newConversionCore
        return newResource
    }

    private fun injectBuildings(conversionCore: ConversionCore) {
        conversionCore.actionConversion.flatMap{ (a, rs) ->
            resourceActionInjectors.flatMap { i -> i(a, rs) }
        }.forEach { (a, rs) ->
            conversionCore.addActionConversion(a, rs)
        }
    }
}

data class ResourceStringTemplate(
        val resource: ResourceIdeal,
        val actionConversion: TemplateConversions,
        val parts: MutableList<String>
)

typealias TemplateConversions = Map<ResourceAction, List<ResourceLink>>


val phonyResource = Resource(
        ResourceCore(
                Genome(
                        "Phony",
                        ResourceType.Animal,
                        1.6 to 1.6,
                        0.0,
                        0,
                        false,
                        true,
                        Behaviour(0.0, 0.00, 0.0, OverflowType.Ignore),
                        Appearance(null, null, null),
                        false,
                        false,
                        0.0,
                        1,
                        null,
                        emptyList(),
                        emptySet(),
                        null,
                        emptyList(),
                        ConversionCore(mapOf())
                )
        )
)
