package shmp.simulation.culture.thinking.language.templates

import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.ConverseWrapper
import shmp.simulation.culture.group.centers.CultureCenter
import shmp.simulation.culture.group.centers.util.ArbitraryResource
import shmp.simulation.culture.thinking.meaning.*
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.dependency.ConsumeDependency
import java.util.*


fun constructTextInfo(cultureCenter: CultureCenter, templateBase: TemplateBase): TextInfo? {
    val textInfos: List<TextInfo> = cultureCenter.aspectCenter.aspectPool.converseWrappers
            .flatMap { getAspectTextInfo(it) }

    return textInfos.randomElementOrNull()?.let {
        complicateInfo(
                it,
                templateBase,
                cultureCenter.memePool
        )
    }
}

fun complicateInfo(info: TextInfo, templateBase: TemplateBase, groupMemes: GroupMemes): TextInfo {
    val substitutions: MutableMap<String, Meme> = HashMap()
    for ((key, value) in info.map) {
        if (key[0] == '!') {
            substitutions[key] = templateBase.nounClauseBase.randomElement().refactor { m: Meme ->
                when {
                    m.observerWord == "!n!" -> value.topMemeCopy()
                    templateBase.templateChars.contains(m.observerWord[0]) -> {
                        substitutions[key + m.observerWord] = templateBase.wordBase[m.observerWord]!!.randomElement {
                            groupMemes.getMeme(it.observerWord)?.importance?.toDouble() ?: 0.0
                        }
                        Meme(key + m.observerWord)
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
    val infos = mutableListOf<TextInfo>()
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
                        ArbitraryResource(resource),
                        Meme("consume"),
                        Meme(res.toLowerCase()))
                )
            }
    return infos
}
