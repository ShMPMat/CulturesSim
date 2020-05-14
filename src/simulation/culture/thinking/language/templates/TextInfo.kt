package simulation.culture.thinking.language.templates

import simulation.Controller
import simulation.culture.thinking.meaning.Meme

class TextInfo(val map: MutableMap<InfoKey, Meme>) {
    constructor(actor: Meme, verb: Meme, receiver: Meme) : this(mutableMapOf()) {
        map["!actor"] = actor
        map["@verb"] = verb
        map["!receiver"] = receiver
    }

    fun changedInfo(key: String, newMeme: Meme): TextInfo {
        val changedMap = map.toMutableMap()
        changedMap[key] = newMeme
        return TextInfo(changedMap)
    }

    fun getMainPart(key: InfoKey) = getSubstituted(key).topMemeCopy()

    fun getSubstituted(key: InfoKey) = substitute(map.getValue(key))

    fun substitute(meme: Meme): Meme {
        return meme.refactor {
            if (Controller.session.templateBase.templateChars.contains(it.observerWord[0])) {
                val substitution = map[it.observerWord] ?: throw RuntimeException()
                return@refactor substitution.copy()
            } else {
                return@refactor it.topMemeCopy()
            }
        }
    }
}

typealias InfoKey = String