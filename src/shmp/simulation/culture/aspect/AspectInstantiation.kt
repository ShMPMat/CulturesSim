package shmp.simulation.culture.aspect

import shmp.utils.InputDatabase
import shmp.simulation.DataInitializationError
import shmp.simulation.culture.aspect.complexity.ResourceComplexity
import shmp.simulation.culture.aspect.complexity.getComplexity
import shmp.simulation.culture.aspect.dependency.AspectDependencies
import shmp.simulation.culture.group.GroupError
import shmp.simulation.space.resource.action.ActionMatcher
import shmp.simulation.space.resource.action.ActionTag
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.labeler.makeResourceLabeler
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class AspectInstantiation(
        private val allowedResourceTags: Collection<ResourceTag>,
        val allowedActionTags: List<ActionTag>
) {
    private val aspects = mutableListOf<Aspect>()

    fun createPool(path: Enumeration<URL>): MutableAspectPool {
        val inputDatabase = InputDatabase(path)

        while (true) {
            val line = inputDatabase.readLine() ?: break
            val tags = line.split("\\s+".toRegex()).toTypedArray()
            aspects.add(createAspect(tags))
        }

        checkAspectsCoherency()

        return MutableAspectPool(aspects.toMutableSet())
    }

    private fun createAspect(tags: Array<String>) = Aspect(createCore(tags), AspectDependencies(mutableMapOf()))

    fun postResourceInstantiation() { //TODO all the creation must be moved after the resources

    }

    private fun createCore(tags: Array<String>): AspectCore {
        val name = tags[0]
        val aspectTags = ArrayList<ResourceTag>()
        val requirements = ArrayList<ResourceTag>()
        val matchers = ArrayList<ActionMatcher>()
        var applyMeaning = false
        var isResourceExposed = true
        var complexity = 1.0
        val sideComplexities = mutableListOf<ResourceComplexity>()
        val actionTags = mutableListOf<ActionTag>()

        for (i in 1 until tags.size) {
            val key = tags[i][0]
            val tag = tags[i].substring(1)
            when (key) {
                '/' -> requirements.add(ResourceTag(tag))
                '#' -> if (tag == "MEANING")
                    applyMeaning = true
                else
                    requirements.add(InstrumentTag(tag))
                '&' -> {
                    val matcherTags = tag.split("-+".toRegex()).toTypedArray()
                    val (resultsTags, labelerTags) = matcherTags.partition { it[0] == '#' }
                    val labeler = makeResourceLabeler(labelerTags.joinToString(","))
                    val results = resultsTags.map {
                        val temp = it.drop(1).split(":".toRegex()).toTypedArray()
                        temp[0] to temp[1].toInt()
                    }
                    matchers.add(ActionMatcher(labeler, results, name))
                }
                '$' -> allowedActionTags
                        .firstOrNull { it.name == tag }
                        ?.let { actionTags.add(it) }
                        ?: throw DataInitializationError("No such ActionTag - $tag")
                'E' -> isResourceExposed = false
                'C' -> complexity = tag.toDouble()
                'S' -> sideComplexities.addAll( tag.split(",").map {
                    if (allowedResourceTags.none { t -> t.name == it }) {
                        throw GroupError("Tag $it doesnt exist")
                    }
                    getComplexity(it)
                })
            }
        }

        return AspectCore(
                name,
                aspectTags,
                requirements,
                applyMeaning,
                isResourceExposed,
                complexity,
                sideComplexities,
                ResourceAction(name, matchers, actionTags)
        )
    }

    private fun checkAspectsCoherency() {
        val maxSimilarAspects = aspects
                .groupBy { it.name }
                .maxByOrNull { it.value.size }
                ?: return
        if (maxSimilarAspects.value.size > 1)
            throw DataInitializationError("Similar Aspects with a name ${maxSimilarAspects.key}")
    }
}
