package simulation.culture.aspect

import extra.InputDatabase
import simulation.SimulationException
import simulation.culture.aspect.complexity.ResourceComplexity
import simulation.culture.aspect.complexity.getComplexity
import simulation.culture.aspect.dependency.AspectDependencies
import simulation.culture.group.GroupError
import simulation.space.resource.action.ActionMatcher
import simulation.space.resource.action.ActionTag
import simulation.space.resource.action.ResourceAction
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.makeResourceLabeler
import kotlin.collections.ArrayList


class AspectInstantiation(
        private val allowedResourceTags: Collection<ResourceTag>,
        val allowedActionTags: List<ActionTag>
) {
    private val aspects = mutableListOf<Aspect>()

    fun createPool(path: String): AspectPool {
        val inputDatabase = InputDatabase(path)

        while (true) {
            val line = inputDatabase.readLine() ?: break
            val tags = line.split("\\s+".toRegex()).toTypedArray()
            aspects.add(createAspect(tags))
        }

        checkAspectsCoherency()

        return AspectPool(aspects.toMutableSet())
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
        var standardComplexity = 1.0
        val sideComplexities = mutableListOf<ResourceComplexity>()
        val actionTags = mutableListOf<ActionTag>()

        for (i in 1 until tags.size) {
            val key = tags[i][0]
            val tag = tags[i].substring(1)
            when (key) {
                '/' -> requirements.add(ResourceTag(tag, false))
                '#' -> if (tag == "MEANING") {
                    applyMeaning = true
                } else {
                    requirements.add(ResourceTag(tag, true))
                }
                '&' -> {
                    val matcherTags = tag.split("-+".toRegex()).toTypedArray()
                    val (resultsTags, labelerTags) = matcherTags.partition { it[0] == '#' }
                    val labeler = makeResourceLabeler(labelerTags.joinToString(","))
                    val results = resultsTags.map {
                        val temp = it.drop(1).split(":".toRegex()).toTypedArray()
                        Pair(temp[0], temp[1].toInt())
                    }
                    matchers.add(ActionMatcher(labeler, results, name))
                }
                '$' -> allowedActionTags
                        .firstOrNull { it.name == tag }
                        ?.let { actionTags.add(it) }
                        ?: throw SimulationException("No such ActionTag - $tag")
                'E' -> isResourceExposed = false
                'C' -> standardComplexity = tag.toDouble()
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
                standardComplexity,
                sideComplexities,
                ResourceAction(name, matchers, actionTags)
        )
    }

    private fun checkAspectsCoherency() {
        val maxSimilarAspects = aspects
                .groupBy { it.name }
                .maxBy { it.value.size }
                ?: return
        if (maxSimilarAspects.value.size > 1)
            throw SimulationException("Similar Aspects with a name ${maxSimilarAspects.key}")
    }
}
