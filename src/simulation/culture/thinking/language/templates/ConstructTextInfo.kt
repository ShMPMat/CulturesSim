package simulation.culture.thinking.language.templates

import shmp.random.randomElement
import shmp.random.randomElementWithProbability
import simulation.culture.group.CultureCenter
import simulation.culture.thinking.meaning.GroupMemes
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.MemePredicate
import java.util.*
import kotlin.random.Random

fun constructTextInfo(cultureCenter: CultureCenter, templateBase: TemplateBase, random: Random): TextInfo? { //TODO too slow
    val textInfos: List<TextInfo> = cultureCenter.aspectCenter.aspectPool.getConverseWrappers()
            .flatMap { cultureCenter.memePool.getAspectTextInfo(it) }
    return if (textInfos.isEmpty()) null else complicateInfo(
            randomElement(textInfos, random),
            templateBase,
            cultureCenter.memePool,
            random
    )
}

fun complicateInfo(
        info: TextInfo,
        templateBase: TemplateBase,
        groupMemes: GroupMemes,
        random: Random
): TextInfo {
    val substitutions: MutableMap<String, Meme> = HashMap()
    for ((key, value) in info.getMap()) {
        if (key[0] == '!') {
            substitutions[key] = randomElement(templateBase.nounClauseBase, random).refactor { m: Meme ->
                when {
                    m.observerWord == "!n!" -> value.topCopy()
                    templateBase.templateChars.contains(m.observerWord[0]) -> {
                        substitutions[key.toString() + m.observerWord] = randomElementWithProbability(
                                templateBase.wordBase[m.observerWord]!!,
                                { groupMemes.getMeme(it.observerWord).importance.toDouble() },
                                random
                        )
                        MemePredicate(key + m.observerWord)
                    }
                    else -> m.topCopy()
                }
            }
        }
    }
    substitutions.forEach { (key, value) -> info.getMap()[key] = value }
    return info
}