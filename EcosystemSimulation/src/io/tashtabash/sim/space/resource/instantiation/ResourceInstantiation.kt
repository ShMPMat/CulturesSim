package io.tashtabash.sim.space.resource.instantiation

import io.tashtabash.utils.InputDatabase
import io.tashtabash.sim.space.resource.*
import io.tashtabash.sim.space.resource.action.ActionMatcher
import io.tashtabash.sim.space.resource.action.ConversionCore
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.container.ResourcePool
import io.tashtabash.sim.space.resource.instantiation.tag.TagParser
import io.tashtabash.sim.space.resource.material.Material
import io.tashtabash.sim.space.resource.material.MaterialPool
import io.tashtabash.sim.space.resource.transformer.ColourTransformer
import io.tashtabash.sim.space.resource.transformer.TextureTransformer
import java.net.URL
import java.util.*


class ResourceInstantiation(
    private val resourceResources: List<URL>,
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
        val inputDatabase = InputDatabase(Collections.enumeration(resourceResources))
        for (line in inputDatabase.readLines()) {
            val tags = line.split("\\s+".toRegex())
            addTemplate(resourceTemplateCreator.createResource(tags))
        }
        for (it in resourceStringTemplates)
            initConversions(it, listOf())
        for (it in resourceStringTemplates)
            initParts(it)

        val filteredFinalResources = resourceStringTemplates.map { (r) -> r }
            .filterIsInstance<ResourceTemplate.Ready>()
            .filter { !it.genome.hasLegacy }
        val swappedLegacyResources = mutableListOf<Pair<Resource, Resource?>>()
        val endResources = filteredFinalResources.map {
            swapLegacies(ResourceIdeal(it.genome), swappedLegacyResources) as ResourceIdeal
        }

        val allResources = listAllResources(endResources)
        return finalizePool(allResources)
    }

    private fun addTemplate(template: ResourceStringTemplate) {
        if (resourceStringTemplates.any { it.genomeTemplate.baseName == template.genomeTemplate.baseName })
            throw ParseException("Resource with name ${template.genomeTemplate.baseName} is already being created")

        resourceStringTemplates += template
    }

    private fun getTemplateWithName(name: String): ResourceStringTemplate = resourceStringTemplates
        .firstOrNull { it.genomeTemplate.baseName == name }
        ?: throw NoSuchElementException("Cannot find a Resource template with a name '$name'")

    private fun matchActions(resource: Resource, resourceMapper: (Resource, Int) -> Resource = { r, n -> r.copy(n) }) {
        for ((action, matchers) in actions)
            for (matcher in matchers)
                if (matcher.match(resource))
                    resource.genome.conversionCore.addActionConversion(
                        action,
                        matcher.getResults(resource, resourceStringTemplates)
                            .map { (r, n) -> resourceMapper(r, n) }
                    )
    }

    private fun initConversions(template: ResourceStringTemplate, conversionPrefix: List<Resource>) {
        val (genome, actionConversion, _) = template
        for ((a, l) in actionConversion.entries)
            genome.conversionCore.addActionConversion(
                a,
                l.map { readConversion(it, conversionPrefix + listOf(createResource(template.genomeTemplate))) }
            )
    }

    private fun readConversion(link: ResourceTemplateLink, conversionPrefix: List<Resource>): Resource {
        if (link.resourceName == "LEGACY")
            return phonyResource.copy(link.amount) // The legacy isn't computed yet, put a mock resource fot now

        val conversionResource = conversionPrefix.dropLast(1).firstOrNull { link.resourceName == it.baseName }
            ?: run {
                val template = getTemplateWithName(link.resourceName)
                initConversions(template, conversionPrefix)
                instantiateTemplate(template.genomeTemplate, conversionPrefix.last().genome)
            }
        val resource = link.transform(conversionResource)
        return resource.copy(link.amount)
    }

    private fun initParts(template: ResourceStringTemplate) {
        val (genome, _, parts) = template
        for (part in parts) {
            val link = parseLink(part, conversionParser)
            val partTemplate = getTemplateWithName(link.resourceName)
            val instantiatedResource = createResource(partTemplate.genomeTemplate)
            val partResource = link.transform(instantiatedResource)

            genome.addPart(partResource)
        }
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

    private fun addTakeApartAction(genome: Genome) {
        val takeApart = specialActions.getValue("TakeApart")
        val killing = specialActions.getValue("Killing")

        if (genome.parts.isNotEmpty()) {
            val result = genome.parts.toList()

            if (!genome.conversionCore.actionConversions.containsKey(takeApart) && !genome.behaviour.isResisting)
                genome.conversionCore.addActionConversion(takeApart, result)
            else if (!genome.conversionCore.actionConversions.containsKey(killing) && genome.behaviour.isResisting)
                genome.conversionCore.addActionConversion(killing, result)
        }
    }

    private fun swapLegacies(
        resource: Resource,
        swappedLegacyResources: MutableList<Pair<Resource, Resource?>>,
        legacyResource: Resource? = null,
        treeStart: List<Resource> = listOf()
    ): Resource {
        val legacy = legacyResource?.takeIf { resource.genome.hasLegacy }
        val newResource = ResourceIdeal(
            resource.genome.copy(
                legacy = legacy?.baseName,
                parts = listOf()
            ),
            resource.amount
        )

        swappedLegacyResources.firstOrNull { it.first == newResource && it.second == legacy }
            ?.let { return ResourceIdeal(it.first.genome, resource.amount) } // Has has already been handled
        swappedLegacyResources += newResource to legacy

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
                }.let { ResourceIdeal(it.genome, r.amount) }
            }
        }.forEach { (a, r) -> newConversionCore.addActionConversion(a, r) }

        newResource.genome.conversionCore = newConversionCore
        // Match actions for legacy Resources, since they were not matched at initConversions(..)
        matchActions(newResource) { r, n ->
            when (r) {
                newResource -> newResource
                else -> swapLegacies(
                    r.injectAppearance(resource),
                    swappedLegacyResources,
                    newResource,
                    treeStart + listOf(newResource)
                )
            }.let { ResourceIdeal(it.genome, n) }
        }
        applyActionInjectors(newConversionCore)
        addTakeApartAction(newResource.genome)

        return newResource
    }

    private fun applyActionInjectors(conversionCore: ConversionCore) {
        conversionCore.actionConversions.flatMap { (a, rs) ->
            resourceActionInjectors.flatMap { i -> i(a, rs) }
        }.forEach { (a, rs) ->
            conversionCore.addActionConversion(a, rs)
        }
    }
}

data class ResourceStringTemplate(
//    val resource: ResourceIdeal,
    val genomeTemplate: ResourceTemplate,
    val actionConversion: TemplateConversions,
    val parts: MutableList<String>
)

sealed class ResourceTemplate {
    abstract val baseName: String
    abstract val conversionCore: ConversionCore
    abstract fun addPart(part: Resource)

    class Ready(val genome: Genome): ResourceTemplate() {
        override val baseName = genome.baseName
        override val conversionCore = genome.conversionCore
        override fun addPart(part: Resource) = genome.addPart(part)
    }

    class Template(val genome: GenomeTemplate): ResourceTemplate() {
        override val baseName = genome.name
        override val conversionCore = genome.conversionCore
        override fun addPart(part: Resource) {
            genome.parts += part
        }
    }
}

typealias TemplateConversions = Map<ResourceAction, List<ResourceTemplateLink>>


private val phonyResource = Resource(
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
            Material("Phony", .1, listOf()),
            emptyList(),
            ConversionCore(mapOf())
        )
    )
)

fun createResource(template: ResourceTemplate) =
    if (template is ResourceTemplate.Ready)
        ResourceIdeal(template.genome)
    else throw ParseException("No templates allowed")

fun instantiateTemplate(resourceTemplate: ResourceTemplate, legacy: Genome) =
    ResourceIdeal(
        when (resourceTemplate) {
            is ResourceTemplate.Template ->
                resourceTemplate.genome.getInstantiatedGenome(legacy)
            is ResourceTemplate.Ready ->
                resourceTemplate.genome
        }
    )
