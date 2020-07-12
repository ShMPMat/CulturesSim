package simulation.space.resource.instantiation

import extra.InputDatabase
import simulation.SimulationException
import simulation.space.SpaceError
import simulation.space.resource.*
import simulation.space.resource.action.ConversionCore
import simulation.space.resource.action.ResourceAction
import simulation.space.resource.container.ResourcePool
import simulation.space.resource.dependency.*
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
        materialPool: MaterialPool,
        amountCoefficient: Int = 1,
        tagParser: TagParser
) {
    private val resourceTemplateCreator = ResourceTemplateCreator(actions, materialPool, amountCoefficient, tagParser)

    private val resourceTemplates = ArrayList<ResourceTemplate>()
    var resourcePool = ResourcePool(listOf())

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
        resourcePool = ResourcePool(resourceTemplates.map { it.resource })
        resourceTemplates.forEach { actualizeLinks(it) }
        resourceTemplates.forEach { actualizeParts(it) }//TODO Maybe legacy resources dont have parts!
        return resourcePool
    }

    private fun getTemplateWithName(name: String): ResourceTemplate = resourceTemplates
            .first { it.resource.baseName == name }

    private fun actualizeLinks(template: ResourceTemplate) {
        val (resource, actionConversion, _) = template
        for ((a, l) in actionConversion.entries) {
            resource.core.genome.conversionCore.addActionConversion(a, l
                    .map { readConversion(template, it) }
                    .toMutableList()
            )
        }
        if (resource.core.materials.isEmpty())
            return

        for (action in actions)
            for (matcher in action.matchers)
                if (matcher.match(resource))
                    resource.core.genome.conversionCore.addActionConversion(
                            action,
                            matcher.getResults(template, resourceTemplates)
                                    .map { (r, n) -> copyWithLegacyInsertion(r, resource.core).resource to n }
                    )
    }

    private fun readConversion(
            template: ResourceTemplate,
            conversionString: String
    ): Pair<Resource?, Int> {
        val link = parseLink(conversionString)
        if (link.resourceName == "LEGACY")
            return manageLegacyConversion(template.resource, link.amount)

        var nextTemplate = getTemplateWithName(link.resourceName)
        nextTemplate = copyWithLegacyInsertion(nextTemplate, template.resource.core)

        actualizeLinks(nextTemplate)

        val resource = link.transform(nextTemplate.resource)
        return Pair(resource, link.amount)//TODO insert amount in Resource amount;
    }

    private fun manageLegacyConversion(
            resource: ResourceIdeal,
            amount: Int
    ): Pair<Resource?, Int> {
        val legacy = resource.genome.legacy
                ?: return Pair(null, amount) //TODO this is so wrong
        //        val legacyResource = resource.genome.legacy.copy()
        val legacyTemplate = getTemplateWithName(legacy.genome.baseName)//TODO VERY DANGEROUS will break on legacy depth > 1
        return Pair(copyWithLegacyInsertion(legacyTemplate, resource.core).resource, amount)
    }

    private fun copyWithLegacyInsertion(
            template: ResourceTemplate,
            creator: ResourceCore
    ): ResourceTemplate {
        if (!template.resource.genome.hasLegacy || template.resource.core == creator)
            return template

        val (resource, actionConversion, parts) = template

        val legacyResource = ResourceIdeal(
                template.resource.core.genome.let { g ->
                    if (g is GenomeTemplate)
                        instantiateTemplateCopy(g, creator)
                    else ResourceCore(
                            resource.genome.name,
                            ArrayList<Material>(resource.core.materials),
                            resource.genome.copy()
                    )
                }
        )
        val legacyTemplate = ResourceTemplate(legacyResource, actionConversion, parts)
        actualizeLinks(legacyTemplate)
        //TODO actualize parts?
        if (resource.genome !is GenomeTemplate)
            setLegacy(legacyTemplate, creator)

        return legacyTemplate //TODO is legacy passed to parts in genome?
    }

    private fun instantiateTemplateCopy(genome: GenomeTemplate, legacy: ResourceCore): ResourceCore {
        return ResourceCore(
                genome.name,
                java.util.ArrayList(legacy.materials),
                genome.getInstantiatedGenome(legacy)
        )
    }

    private fun setLegacy(template: ResourceTemplate, legacy: ResourceCore) {
        val (resource, actionConversion, _) = template
        resource.genome.legacy = legacy
        for (entry in actionConversion.entries) {
            if (entry.value.any { it.split(":".toRegex()).toTypedArray()[0] == "LEGACY" }) {
                resource.core.genome.conversionCore.addActionConversion(entry.key, entry.value
                        .map { readConversion(template, it) }
                        .toMutableList())//TODO should I do it if in upper level I call actualizeLinks?
            }
        }
        replaceLinks(resource)
    }

    private fun replaceLinks(resource: ResourceIdeal) {
        for (resources in resource.core.genome.conversionCore.actionConversion.values) {
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
            val link = parseLink(part)
            var partTemplate = getTemplateWithName(link.resourceName)

            partTemplate = copyWithLegacyInsertion(partTemplate, resource.core)

            val partResource = link.transform(partTemplate.resource)
            resource.core.genome.addPart(partResource)
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
                resource.core.genome.conversionCore.addActionConversion(
                        actions.first { it.name == "TakeApart" },
                        resourceList
                )
            }
        }
    }
}

data class ResourceTemplate(
        val resource: ResourceIdeal,
        val actionConversion: MutableMap<ResourceAction, List<String>>,
        val parts: List<String>
)
