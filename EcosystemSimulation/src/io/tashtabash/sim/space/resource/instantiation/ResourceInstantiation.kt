package io.tashtabash.sim.space.resource.instantiation

import io.tashtabash.sim.init.getResourcePaths
import io.tashtabash.utils.InputDatabase
import io.tashtabash.sim.space.resource.*
import io.tashtabash.sim.space.resource.action.ActionMatcher
import io.tashtabash.sim.space.resource.action.ConversionCore
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.container.ResourcePool
import io.tashtabash.sim.space.resource.instantiation.tag.TagParser
import io.tashtabash.sim.space.resource.material.MaterialPool
import io.tashtabash.sim.space.resource.transformer.ColourTransformer
import io.tashtabash.sim.space.resource.transformer.TextureTransformer
import java.util.*


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
    private val resourceStringTemplateResources by lazy {
        resourceStringTemplates.map { it.resource }
    }

    fun createPool(): ResourcePool {
        val classLoader = Thread.currentThread().contextClassLoader
        val folderUrls = classLoader.getResources(folderPath).toList()

        val resourceUrls = getResourcePaths(folderUrls)

        var line: String?
        var tags: Array<String>
        val urlEnumeration = Collections.enumeration(resourceUrls)
        val inputDatabase = InputDatabase(urlEnumeration)
        while (true) {
            line = inputDatabase.readLine()
                    ?: break
            tags = line.split("\\s+".toRegex()).toTypedArray()

            addTemplate(resourceTemplateCreator.createResource(tags))
        }
        resourceStringTemplates.forEach { initConversions(it, listOf()) }
        resourceStringTemplates.forEach { initParts(it) }

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

    private fun initConversions(template: ResourceStringTemplate, conversionPrefix: List<Resource>) {
        val (resource, actionConversion, _) = template
        for ((a, l) in actionConversion.entries) {
            resource.genome.conversionCore.addActionConversion(
                    a,
                    l.map { readConversion(template, it, conversionPrefix + listOf(resource)) }
            )
        }
        if (resource.genome is GenomeTemplate)
            return

        matchActions(resource)
    }

    private fun matchActions(resource: Resource, resourceMapper: (Resource, Int) -> Resource = { r, n -> r.copy(n) }) {
        for ((action, matchers) in actions)
            for (matcher in matchers)
                if (matcher.match(resource))
                    resource.genome.conversionCore.addActionConversion(
                        action,
                        matcher.getResults(resource, resourceStringTemplateResources)
                            .map { (r, n) -> resourceMapper(r, n) }
                    )
    }

    private fun readConversion(template: ResourceStringTemplate, link: ResourceLink, conversionPrefix: List<Resource>): Resource {
        if (link.resourceName == "LEGACY")
            return manageLegacyConversion(template.resource, link.amount)

        val conversionResource = conversionPrefix.dropLast(1).firstOrNull { link.resourceName == it.baseName }
                ?: run {
                    val foundTemplate = getTemplateWithName(link.resourceName)
                    initConversions(foundTemplate, conversionPrefix)
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

    private fun initParts(template: ResourceStringTemplate) {
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

        resource.genome.conversionCore.actionConversions.map { (action, results) ->
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
        // Match actions for legacy Resources, since they were not matched at initConversions(..)
        matchActions(newResource) { res, n ->
            swapLegacies(
                res.injectAppearance(resource),
                swappedLegacyResources,
                newResource,
                treeStart + listOf(newResource)
            ).copy(n)
                .let { r -> ResourceIdeal(r.genome, n) }
        }

        return newResource
    }

    private fun injectBuildings(conversionCore: ConversionCore) {
        conversionCore.actionConversions.flatMap { (a, rs) ->
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
                        Behaviour(0.0, 0.00, 0.0, 0.0, OverflowType.Ignore),
                        Appearance(null, null, null),
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
