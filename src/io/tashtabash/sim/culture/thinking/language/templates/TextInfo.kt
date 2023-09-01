package io.tashtabash.sim.culture.thinking.language.templates

import io.tashtabash.sim.CulturesController
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept
import io.tashtabash.generator.culture.worldview.Meme


class TextInfo(val map: MutableMap<InfoKey, Meme>, val actorConcept: ObjectConcept) {
    constructor(actorConcept: ObjectConcept, verb: Meme, receiver: Meme) : this(mutableMapOf(), actorConcept) {
        map["!actor"] = actorConcept.meme
        map["@verb"] = verb
        map["!receiver"] = receiver
    }

    fun changedInfo(newActorConcept: ObjectConcept): TextInfo {
        val changedMap = map.toMutableMap()
        changedMap["!actor"] = newActorConcept.meme
        return TextInfo(changedMap, actorConcept)
    }

    fun getMainPart(key: InfoKey) = getSubstituted(key).topMemeCopy()

    fun getSubstituted(key: InfoKey) = substitute(map.getValue(key))

    fun substitute(meme: Meme): Meme {
        return meme.refactor {
            if (CulturesController.session.templateBase.templateChars.contains(it.observerWord[0])) {
                val substitution = map[it.observerWord] ?: throw RuntimeException()
                return@refactor substitution.copy()
            } else
                return@refactor it.topMemeCopy()
        }
    }
}

typealias InfoKey = String