package simulation.space.resource

import extra.InputDatabase
import extra.ShnyPair
import simulation.Controller
import simulation.culture.aspect.Aspect
import simulation.space.SpaceError
import simulation.space.Tile
import simulation.space.resource.dependency.AvoidTiles
import simulation.space.resource.dependency.ConsumeDependency
import simulation.space.resource.dependency.ResourceDependency
import simulation.space.resource.dependency.ResourceNeedDependency
import java.util.*
import java.util.stream.Collectors

fun createPool(): ResourcePool {
    val resourceIdeals = ArrayList<ResourceTemplate>()
    val inputDatabase = InputDatabase("SupplementFiles/Resources")
    var line: String?
    var tags: Array<String>
    while (true) {
        line = inputDatabase.readLine() ?: break
        tags = line.split("\\s+".toRegex()).toTypedArray()
        resourceIdeals.add(createResource(tags))
    }
    val pool = ResourcePool(resourceIdeals.map { it.resourceIdeal })
    resourceIdeals.forEach { actualizeLinks(it, pool, Controller.session.world.aspectPool) }//TODO remove all Controller calls
    resourceIdeals.forEach { actualizeParts(it, pool) }
    return pool
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

    for (i in 12 until tags.size) {
        val key = tags[i][0]
        val tag = tags[i].substring(1)
        when (key) {
            '+' -> aspectConversion[Controller.session.world.getPoolAspect(tag.substring(0, tag.indexOf(':')))] =
                    tag.substring(tag.indexOf(':') + 1).split(",".toRegex()).toTypedArray()
            '@' -> {
                if (tag == "TEMPLATE") {
                    isTemplate = true
                } else {
                    val material = Controller.session.world.getPoolMaterial(tag)
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
                    resourceDependencies.add(ConsumeDependency(elements[2].toDouble(), elements[3] == "1", elements[1].toDouble(),
                            listOf(*elements[0].split(",".toRegex()).toTypedArray())
                    ))
                } else {
                    resourceDependencies.add(ResourceNeedDependency(
                            ResourceNeedDependency.Type.valueOf(elements[4]), elements[1].toDouble(), elements[2].toDouble(), elements[3] == "1",
                            listOf(*elements[0].split(",".toRegex()).toTypedArray())
                    ))
                }
            }
            '#' -> resourceDependencies.add(AvoidTiles(
                    Arrays.stream(tag.split(":".toRegex()).toTypedArray())
                            .map { name: String? -> Tile.Type.valueOf(name!!) }
                            .collect(Collectors.toSet())
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
        var genome = GenomeTemplate(genome)
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

fun actualizeLinks(template: ResourceTemplate, resourcePool: ResourcePool, aspectPool: List<Aspect>) {
    val (resource, aspectConversion, _) = template
    for (aspect in aspectConversion.keys) {
        resource.resourceCore.aspectConversion.put(
                aspect,
                aspectConversion[aspect]
                        ?.map {
                            resource.resourceCore.readConversion(it, resourcePool)
                                    ?: throw SpaceError("Impossible error")
                        }//TODO better iteration
        )
    }
    if (resource.resourceCore.materials.isEmpty()) {
        return
    }
    val material: Material = resource.resourceCore.mainMaterial
    for (aspect in aspectPool) {//TODO why is it here?
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

fun actualizeParts(template: ResourceTemplate, resourcePool: ResourcePool) {
    val (resource, aspectConversion, parts) = template
    for (part in parts) {
        var partResource = resourcePool.getResource(part.split(":".toRegex()).toTypedArray()[0])
        partResource = if (partResource.resourceCore.genome.hasLegacy())//TODO seems strange
            partResource.resourceCore.copyWithLegacyInsertion(resource.resourceCore, resourcePool)
        else
            partResource
        partResource.amount = part.split(":".toRegex()).toTypedArray()[1].toInt()
        resource.resourceCore.genome.addPart(partResource)
    }
    if (resource.resourceCore.genome.parts.isNotEmpty()
            && !aspectConversion.containsKey(Controller.session.world.getPoolAspect("TakeApart"))) {//TODO aspects shouldn't be here I recon
        val resourceList: MutableList<ShnyPair<Resource, Int>> = ArrayList()
        for (partResource in resource.resourceCore.genome.getParts()) {
            resourceList.add(ShnyPair(partResource, partResource.amount))
            resource.resourceCore.aspectConversion[Controller.session.world.getPoolAspect("TakeApart")] = resourceList
        }
    }
}

data class ResourceTemplate(
        val resourceIdeal: ResourceIdeal,
        val aspectConversion: Map<Aspect, Array<String>>,
        val parts: List<String>
)