package simulation.culture.group.cultureaspect

import extra.ShnyPair
import shmp.random.randomElement
import simulation.Controller
import simulation.culture.aspect.Aspect
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.CultureCenter
import simulation.culture.group.Group
import simulation.culture.group.reason.BetterAspectUseReason
import simulation.culture.group.reason.Reason
import simulation.culture.thinking.meaning.Meme
import simulation.space.resource.Resource
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors

fun createDepictObject(meaningAspects: Collection<ConverseWrapper>, meme: Meme?, group: Group): DepictObject? {
    if (meaningAspects.isNotEmpty() && meme != null)
        return DepictObject(
                group,
                meme,
                randomElement(meaningAspects, Controller.session.random)
        )
    return null
}

fun createAestheticallyPleasingObject(resource: Resource?, group: Group): AestheticallyPleasingObject? {
    if (resource != null) {
        return AestheticallyPleasingObject(group, resource)
    }
    return null
}

