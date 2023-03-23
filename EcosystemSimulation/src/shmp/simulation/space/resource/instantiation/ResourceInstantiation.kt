package shmp.simulation.space.resource.instantiation

import shmp.utils.InputDatabase
import shmp.simulation.space.resource.*
import shmp.simulation.space.resource.action.ActionMatcher
import shmp.simulation.space.resource.action.ConversionCore
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.container.ResourcePool
import shmp.simulation.space.resource.instantiation.tag.TagParser
import shmp.simulation.space.resource.material.MaterialPool
import shmp.simulation.space.resource.transformer.ColourTransformer
import shmp.simulation.space.resource.transformer.TextureTransformer
import java.nio.file.Files
import java.nio.file.Paths


class ResourceInstantiation(
        private val folderPath: String,
        private val actions: Map<ResourceAction, List<ActionMatcher>>,
        materialPool: MaterialPool,
        amountCoefficient: Int = 1,
        tagParser: TagParser,
        private val resourceActionInjectors: List<ResourceActionInjector>
) {
    private val dependencyParser = DefaultDependencyParser()
    private val conversionParser = ConversionParser(actions.keys.toList(), dependencyParser)
    private val resourceTemplateCreator = ResourceTemplateCreator(
            materialPool,
            amountCoefficient,
            tagParser,
            dependencyParser,
            conversionParser
    )

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

            addTemplate(resourceTemplateCreator.createResource(tags))
        }
        resourceStringTemplates.forEach { actualizeLinks(it, listOf()) }
        resourceStringTemplates.forEach { actualizeParts(it) }

        val swappedLegacyResources = mutableListOf<Resource>()
        val endResources = filteredFinalResources.map {
            swapLegacies(it, swappedLegacyResources) as ResourceIdeal
        }

        return finalizePool(endResources)
    }

    private val filteredFinalResources
        get() = resourceStringTemplates
                .map { (r) -> r }
                .filter { it.genome !is GenomeTemplate && !it.genome.hasLegacy }

    private fun addTemplate(template: ResourceStringTemplate) {
        if (resourceStringTemplates.any { it.resource.baseName == template.resource.baseName })
            throw ParseException("Resource with name ${template.resource.baseName} is already being created")

        resourceStringTemplates += template
    }

    private fun getTemplateWithName(name: String): ResourceStringTemplate = resourceStringTemplates
            .firstOrNull { it.resource.baseName == name }
            ?: throw NoSuchElementException("Cannot find a Resource template with a name '$name'")

    private fun actualizeLinks(template: ResourceStringTemplate, conversionPrefix: List<Resource>) {
        val (resource, actionConversion, _) = template
        for ((a, l) in actionConversion.entries) {
            resource.genome.conversionCore.addActionConversion(
                    a,
                    l.map { readConversion(template, it, conversionPrefix + listOf(resource)) }
            )
        }
        if (resource.genome.materials.isEmpty())//TODO why is it here? What is it? (is it a GenomeTemplate check?)
            return

        for ((action, matchers) in actions)
            for (matcher in matchers)
                if (matcher.match(resource))
                    resource.genome.conversionCore.addActionConversion(
                            action,
                            matcher.getResults(template, resourceStringTemplates)
                                    .map { (t, n) -> t.resource.copy(n) }
                    )
    }

    private fun readConversion(template: ResourceStringTemplate, link: ResourceLink, conversionPrefix: List<Resource>): Resource {
        if (link.resourceName == "LEGACY")
            return manageLegacyConversion(template.resource, link.amount)

        val conversionResource = conversionPrefix.dropLast(1).firstOrNull { link.resourceName == it.baseName }
                ?: run {
                    val foundTemplate = getTemplateWithName(link.resourceName)
                    actualizeLinks(foundTemplate, conversionPrefix)
                    foundTemplate.resource
                }
        val resource = link.transform(conversionResource)
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
            val link = parseLink(part, conversionParser)
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
            swappedLegacyResources: MutableList<Resource>,
            legacyResource: Resource? = null,
            treeStart: List<Resource> = listOf()
    ): Resource {
        if (!resource.genome.hasLegacy) //TODO there are resources with same name but different sizes
            swappedLegacyResources.firstOrNull { it.fullName == resource.fullName && it.genome.size == resource.genome.size }
                    ?.let { return ResourceIdeal(it.genome, resource.amount) }

        val newGenome = resource.genome.let { oldGenome ->
            if (oldGenome is GenomeTemplate)
                oldGenome.getInstantiatedGenome(legacyResource?.genome!!)//TODO make it safe
            else oldGenome
        }
        val newResource = ResourceIdeal(newGenome.copy(
                legacy = legacyResource?.baseName?.takeIf { newGenome.hasLegacy },
                parts = listOf(),
                primaryMaterial = newGenome.primaryMaterial ?: legacyResource?.genome?.primaryMaterial!!
        ), resource.amount)

        swappedLegacyResources += newResource

        val newParts = resource.genome.parts.map {
            swapLegacies(
                    it.injectAppearance(resource),
                    swappedLegacyResources,
                    newResource,
                    treeStart + listOf(newResource)
            )
                    .copy(it.amount)
                    .let { r -> ResourceIdeal(r.genome, it.amount) }
        }.toMutableList()

        newParts.forEach {
            newResource.genome.addPart(it)
        }

        val newConversionCore = ConversionCore(mutableMapOf())

        resource.genome.conversionCore.actionConversion.map { (action, results) ->
            action to results.map { r ->
                when (r) {
                    resource -> newResource
                    phonyResource -> treeStart[0]
                    else -> swapLegacies(
                            r.injectAppearance(resource),
                            swappedLegacyResources,
                            newResource,
                            treeStart + listOf(newResource)
                    )
                }.let{ ResourceIdeal(it.genome, r.amount) }
            }
        }.forEach { (a, r) -> newConversionCore.addActionConversion(a, r) }
        injectBuildings(newConversionCore)

        newResource.genome.conversionCore = newConversionCore
        return newResource
    }

    private fun injectBuildings(conversionCore: ConversionCore) {
        conversionCore.actionConversion.flatMap { (a, rs) ->
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
