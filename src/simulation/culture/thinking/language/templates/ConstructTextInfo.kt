package simulation.culture.thinking.language.templates

import shmp.random.randomElement
import shmp.random.randomElementWithProbability
import simulation.culture.aspect.Aspect
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.CultureCenter
import simulation.culture.thinking.meaning.*
import simulation.space.resource.Resource
import simulation.space.resource.dependency.ConsumeDependency
import java.util.*
import kotlin.random.Random

fun constructTextInfo(cultureCenter: CultureCenter, templateBase: TemplateBase, random: Random): TextInfo? { //TODO too slow
    val textInfos: List<TextInfo> = cultureCenter.aspectCenter.aspectPool.converseWrappers
            .flatMap { getAspectTextInfo(it) }
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
                    m.observerWord == "!n!" -> value.topMemeCopy()
                    templateBase.templateChars.contains(m.observerWord[0]) -> {
                        substitutions[key.toString() + m.observerWord] = randomElementWithProbability(
                                templateBase.wordBase[m.observerWord]!!,
                                { groupMemes.getMeme(it.observerWord).importance.toDouble() },
                                random
                        )
                        MemePredicate(key + m.observerWord)
                    }
                    else -> m.topMemeCopy()
                }
            }
        }
    }
    substitutions.forEach { (key, value) -> info.map[key] = value }
    return info
}


fun getAspectTextInfo(aspect: Aspect): List<TextInfo> {
    val infos: MutableList<TextInfo> = ArrayList()
    if (aspect is ConverseWrapper) {
        infos.addAll(getResourceTextInfo(aspect.resource))
        aspect.producedResources.forEach {
            infos.addAll(getResourceTextInfo(it))
        }
    }
    return infos
}

fun getResourceTextInfo(resource: Resource) = getResourceInformationTextInfo(resource)

fun getResourceInformationTextInfo(resource: Resource): List<TextInfo> {
    val infos: MutableList<TextInfo> = ArrayList()
    for (resourceDependency in resource.genome.dependencies)
        if (resourceDependency is ConsumeDependency)
            for (res in resourceDependency.lastConsumed) {
                infos.add(TextInfo(
                        constructMeme(resource),
                        MemePredicate("consume"),
                        MemeSubject(res.toLowerCase()))
                )
            }
    return infos
}