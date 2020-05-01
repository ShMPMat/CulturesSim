package simulation.space.resource

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectResult
import simulation.culture.group.GroupError
import simulation.culture.thinking.meaning.Meme
import simulation.space.SpaceError
import simulation.space.resource.material.Material
import simulation.space.resource.tag.ResourceTag
import java.util.*
import java.util.stream.Collectors

/**
 * Class which contains all general information about all Resources with the same name.
 */
class ResourceCore(
        name: String,
        meaningPostfix: String,
        val materials: List<Material>,
        val genome: Genome,
        aspectConversion: Map<Aspect, MutableList<Pair<Resource?, Int>>>,
        private val meaning: Meme?
) {
    var meaningPostfix = ""
        private set
    var hasMeaning = false
    val aspectConversion: MutableMap<Aspect, MutableList<Pair<Resource?, Int>>>

    init {
        this.aspectConversion = HashMap(aspectConversion)
        setName(name + meaningPostfix)
    }

    internal fun addAspectConversion(aspect: Aspect, resourceList: List<Pair<Resource?, Int>>) {
        aspectConversion[aspect] = resourceList.toMutableList()
    }

    private fun setName(fullName: String) {
        if (fullName.contains("_representing_")) {
            genome.name = fullName.substring(0, fullName.indexOf("_representing_"))
            meaningPostfix = fullName.substring(fullName.indexOf("_representing_"))
        } else
            genome.name = fullName
    }

    fun copy() = Resource(this)

    fun copy(amount: Int) = Resource(this, amount)

    fun fullCopy(): Resource {
        if (genome is GenomeTemplate)
            throw SpaceError("Cant make a full copy of a template")
        return Resource(ResourceCore(
                genome.name,
                meaningPostfix,
                ArrayList(materials),
                Genome(genome),
                aspectConversion,
                meaning
        ))
    }

    private fun instantiateTemplateCopy(legacy: ResourceCore): ResourceCore {
        if (genome !is GenomeTemplate)
            throw SpaceError("Cant make a instantiated copy not from a template")
        return ResourceCore(
                genome.name,
                meaningPostfix,
                ArrayList(legacy.materials),
                genome.getInstantiatedGenome(legacy),
                aspectConversion,
                meaning
        )
    }

    fun insertMeaning(meaning: Meme, result: AspectResult): ResourceCore {
        val genome = Genome(genome)
        genome.spreadProbability = 0.0
        var meaningPostfix = StringBuilder("_representing_" + meaning + "_with_" + result.node.aspect.name)
        if (result.node.resourceUsed.size > 1) {
            val names = result.node.resourceUsed.entries
                    .filter { it.key.name != ResourceTag.phony().name }
                    .flatMap { p -> p.value.resources.map { it.fullName } }
                    .distinct()
            meaningPostfix.append("(")
            for (name in names)
                meaningPostfix.append(name).append(", ")
            meaningPostfix = StringBuilder(meaningPostfix.substring(0, meaningPostfix.length - 2) + ")")
        }
        val core = ResourceCore(
                genome.name,
                meaningPostfix.toString(),
                ArrayList(materials),
                genome,
                aspectConversion,
                meaning
        )
        core.hasMeaning = true
        return core
    }

    fun applyAspect(aspect: Aspect): List<Resource> { //TODO throw an exception on any attempt to copy template
        return if (aspectConversion.containsKey(aspect)) {
            aspectConversion[aspect]!!.stream()
                    .map { pair: Pair<Resource?, Int> ->
                        var resource = pair.first ?: throw GroupError("Empty conversion")
                        if (resource.resourceCore.genome is GenomeTemplate) {
                            resource = resource.copy(pair.second)
                            resource.resourceCore = resource.resourceCore.instantiateTemplateCopy(this)
                            resource.computeHash()
                            return@map resource
                        } else {
                            return@map resource.copy(pair.second)
                        }
                    }.collect(Collectors.toList())
        } else listOf(applyAspectToMaterials(aspect).copy(1))
    }

    private fun applyAspectToMaterials(aspect: Aspect): ResourceCore {
        val newMaterials = materials.map { it.applyAspect(aspect) }
        val genome = Genome(genome)
        genome.spreadProbability = 0.0
        return ResourceCore(genome.name + if (newMaterials == materials) "" else "_" + aspect.name,
                meaningPostfix, newMaterials, genome, aspectConversion, meaning) //TODO dangerous stuff for genome
    }

    fun hasApplicationForAspect(aspect: Aspect) = aspectConversion.containsKey(aspect)
            || materials.any { it.hasApplicationForAspect(aspect) }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null) return false
        val resourceCore = o as ResourceCore
        return genome.baseName == resourceCore.genome.baseName
    }

    override fun hashCode(): Int {
        return Objects.hash(genome.baseName)
    }

    override fun toString() = genome.baseName
}