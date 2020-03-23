package simulation.culture.group.cultureaspect

import shmp.random.randomElement
import simulation.culture.aspect.AspectPool
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.Group
import simulation.culture.group.reason.BetterAspectUseReason
import simulation.culture.group.reason.Reason
import simulation.culture.thinking.language.templates.TemplateBase
import simulation.culture.thinking.language.templates.constructTextInfo
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.constructAspectMemes
import simulation.space.resource.Resource
import kotlin.random.Random

fun createDepictObject(
        meaningAspects: Collection<ConverseWrapper>,
        meme: Meme?,
        group: Group,
        random: Random
): DepictObject? {
    if (meaningAspects.isNotEmpty() && meme != null)
        return DepictObject(
                group,
                meme,
                randomElement(meaningAspects, random)
        )
    return null
}

fun createAestheticallyPleasingObject(resource: Resource?, group: Group): AestheticallyPleasingObject? {
    if (resource != null) {
        return AestheticallyPleasingObject(group, resource)
    }
    return null
}

fun createTale(group: Group, templateBase: TemplateBase, random: Random): Tale? {
    val template = templateBase.randomSentenceTemplate
    val info = constructTextInfo(
            group.cultureCenter,
            templateBase,
            random
    )
    if (template != null && info != null) {
        return Tale(template, info)
    }
    return null
}

fun constructRitual(reason: Reason?, group: Group, random: Random): Ritual? {
    if (reason == null) {
        return null
    } else if (reason is BetterAspectUseReason) {
        return constructBetterAspectUseReasonRitual(
                reason,
                group.cultureCenter.aspectCenter.aspectPool,
                group,
                random
        )
    }
    return null
}

fun constructBetterAspectUseReasonRitual(
        reason: BetterAspectUseReason,
        aspectPool: AspectPool,
        group: Group,
        random: Random
): Ritual? {
    val converseWrapper = reason.converseWrapper
    val (aspectMemes, second) = constructAspectMemes(converseWrapper)
    aspectMemes.addAll(second)
    aspectMemes.shuffle(random)
    for (meme in aspectMemes) { //TODO maybe depth check
        if (meme.observerWord == converseWrapper.name) continue
        if (aspectPool.contains(meme.observerWord)) {
            val myAspect = aspectPool.get(meme.observerWord)
            if (myAspect is ConverseWrapper)
                return AspectRitual(group, myAspect, reason)
        } else {
            val options: List<ConverseWrapper> = aspectPool.producedResources
                    .filter { (r) -> r.baseName == meme.observerWord }
                    .map { it.second }
            return if (options.isNotEmpty())
                AspectRitual(group, randomElement(options, random), reason)
            else {
                val meaningAspects = aspectPool.getMeaningAspects()
                val aspect = createDepictObject(
                        meaningAspects,
                        randomElement(aspectMemes, random),
                        group,
                        random
                ) ?: continue
                CultureAspectRitual(aspect, reason) //TODO make complex tales;
            }
        }
    }
    return null
}