package simulation.culture.aspect

import extra.InputDatabase
import simulation.space.resource.tag.ResourceTag
import java.util.*
import kotlin.collections.ArrayList

class AspectInstantiation {
    fun createPool(path: String): AspectPool {
        val aspects: MutableList<Aspect> = ArrayList()
        val inputDatabase = InputDatabase(path)
        while (true) {
            val line = inputDatabase.readLine() ?: break
            val tags = line.split("\\s+".toRegex()).toTypedArray()
            aspects.add(createAspect(tags))
        }
        return AspectPool(aspects)
    }

    private fun createAspect(tags: Array<String>): Aspect {
        return Aspect(createCore(tags), HashMap(), null)
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
            '+' -> aspectTags.add(ResourceTag(tag, true, false, false))
            '/' -> requirements.add(ResourceTag(tag, false, false, false))
            '*' -> requirements.add(ResourceTag(tag, false, false, true))
            '#' -> if (tag == "MEANING") {
                applyMeaning = true
            } else {
                requirements.add(ResourceTag(tag, false, true, false))
            }
            '&' -> matchers.add(AspectMatcher(tag.split("-+".toRegex()).toTypedArray(), name))
        }
    }
    return AspectCore(name, aspectTags, requirements, matchers, applyMeaning)
}