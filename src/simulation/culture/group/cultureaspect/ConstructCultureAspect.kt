package simulation.culture.group.cultureaspect

import shmp.random.randomElement
import simulation.Controller
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.Group
import simulation.culture.thinking.meaning.Meme
import simulation.space.resource.Resource

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