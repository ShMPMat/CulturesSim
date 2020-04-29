package simulation.culture.aspect

import extra.InputDatabase
import simulation.culture.aspect.dependency.AspectDependencies
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.makeResourceLabeler
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class AspectInstantiation {
    fun createPool(path: String): AspectPool {
        val aspects: MutableSet<Aspect> = HashSet()
        val inputDatabase = InputDatabase(path)
        while (true) {
            val line = inputDatabase.readLine() ?: break
            val tags = line.split("\\s+".toRegex()).toTypedArray()
            aspects.add(createAspect(tags))
        }
        return AspectPool(aspects)
    }

    private fun createAspect(tags: Array<String>): Aspect {
        return Aspect(createCore(tags), AspectDependencies(mutableMapOf()))
    }

    fun postResourceInstantiation() { //TODO all the creation must be moved after the resources

    }
}

internal fun createCore(tags: Array<String>): AspectCore {
    val name = tags[0]
    val aspectTags = ArrayList<ResourceTag>()
    val requirements = ArrayList<ResourceTag>()
    val matchers = ArrayList<AspectMatcher>()
    var applyMeaning = false
    for (i in 1 until tags.size) {
        val key = tags[i][0]
        val tag = tags[i].substring(1)
        when (key) {
            '/' -> requirements.add(ResourceTag(tag,false))
            '#' -> if (tag == "MEANING") {
                applyMeaning = true
            } else {
                requirements.add(ResourceTag(tag, true))
            }
            '&' -> {
                val matcherTags = tag.split("-+".toRegex()).toTypedArray()
                val (resultsTags, labelerTags) = matcherTags.partition { it[0] == '#' }
                val labeler = makeResourceLabeler(labelerTags)
                val results = resultsTags.map {
                            val temp = it.drop(1).split(":".toRegex()).toTypedArray()
                            Pair(temp[0], temp[1].toInt())
                        }
                matchers.add(AspectMatcher(labeler, results, name))
            }
        }
    }
    return AspectCore(name, aspectTags, requirements, matchers, applyMeaning)
}