package simulation.culture.thinking.meaning

import shmp.random.testProbability
import simulation.culture.aspect.Aspect
import simulation.culture.aspect.ConverseWrapper
import simulation.space.resource.Resource
import simulation.space.resource.dependency.ConsumeDependency
import kotlin.random.Random

fun constructAndAddSimpleMeme(
        groupMemes: GroupMemes,
        random: Random,
        complicateProbability: Double = 0.5
): Meme? {
    var meme: Meme = groupMemes.memeWithComplexityBias.copy()
    if (testProbability(complicateProbability, random)) {
        var second: Meme
        do second = groupMemes.memeWithComplexityBias.copy()
        while (second.hasPart(meme, setOf("and")))
        meme = meme.copy().addPredicate(
                groupMemes.getMemeCopy("and").addPredicate(second)
        )
        groupMemes.addMemeCombination(meme)
    }
    return meme
}

fun constructMeme(resource: Resource) = MemeSubject(resource.fullName)

fun constructMeme(aspect: Aspect) = MemePredicate(aspect.name)

fun constructAspectMemes(aspect: Aspect): Pair<MutableList<Meme>, List<Meme>> {
    val aspectMemes = Pair<MutableList<Meme>, MutableList<Meme>>(ArrayList(), ArrayList())
    aspectMemes.first.add(constructMeme(aspect))
    if (aspect is ConverseWrapper) {
        val (first, second) = constructResourceMemes(aspect.resource)
        aspectMemes.first.addAll(first)
        aspectMemes.second.addAll(second)
        aspect.producedResources
                .map { constructResourceMemes(it) }
                .forEach { (first1, second1) ->
                    aspectMemes.first.addAll(first1)
                    aspectMemes.second.addAll(second1)
                }
    }
    return aspectMemes
}

fun constructResourceMemes(resource: Resource): Pair<List<Meme>, List<Meme>> {
    val resourceMemes: Pair<MutableList<Meme>, List<Meme>> = constructResourceInfoMemes(resource)
    resourceMemes.first.add(constructMeme(resource))
    return resourceMemes
}

fun constructResourceInfoMemes(resource: Resource): Pair<MutableList<Meme>, MutableList<Meme>> {
    val infoMemes = Pair<MutableList<Meme>, MutableList<Meme>>(ArrayList(), ArrayList())
    for (resourceDependency in resource.genome.dependencies)
        if (resourceDependency is ConsumeDependency)
            for (res in resourceDependency.lastConsumed) {
                val subject: Meme = MemeSubject(res.toLowerCase())
                infoMemes.first.add(subject)
                val element = constructMeme(resource).addPredicate(MemePredicate("consume"))
                element.predicates[0].addPredicate(subject)
                infoMemes.second.add(element)
            }
    return infoMemes
}