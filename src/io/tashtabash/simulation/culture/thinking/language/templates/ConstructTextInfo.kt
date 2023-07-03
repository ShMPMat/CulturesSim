package io.tashtabash.simulation.culture.thinking.language.templates

import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.simulation.culture.aspect.Aspect
import io.tashtabash.simulation.culture.aspect.ConverseWrapper
import io.tashtabash.simulation.culture.group.centers.CultureCenter
import io.tashtabash.simulation.culture.group.centers.util.ArbitraryResource
import io.tashtabash.simulation.culture.thinking.meaning.*
import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.dependency.ConsumeDependency
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
            for (res in resourceDependency.lastConsumed(resource.baseName)) {
                infos.add(TextInfo(
                        ArbitraryResource(resource),
                        Meme("consume"),
                        Meme(res))
                )
            }
    return infos
}
