package simulation.culture.thinking.meaning

import shmp.random.testProbability
import simulation.culture.aspect.Aspect
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.centers.Group
import simulation.culture.group.stratum.Stratum
import simulation.space.resource.Resource
import simulation.space.resource.container.ResourcePack
import simulation.space.resource.dependency.ConsumeDependency
import kotlin.random.Random


fun constructAndAddSimpleMeme(
        groupMemes: GroupMemes,
        random: Random,
        complicateProbability: Double = 0.5,
        maxTests: Int = 10
): Meme? {
    var meme: Meme = groupMemes.memeWithComplexityBias.copy()
    if (testProbability(complicateProbability, random)) {
        var second: Meme
        var tests = 0
        do {
            second = groupMemes.memeWithComplexityBias.copy()
            tests++
        } while (second.hasPart(meme, setOf("and")) && tests <= maxTests)
        meme = meme.copy().addPredicate(
                groupMemes.getMemeCopy("and")?.addPredicate(second)
        )
        groupMemes.addMemeCombination(meme)
    }
    return meme
}


fun makeMeme(stratum: Stratum) = MemeSubject(stratum.name)

fun makeMeme(group: Group) = MemeSubject(group.name)

fun makeMeme(resource: Resource) = MemeSubject(resource.fullName)

fun makeMeme(aspect: Aspect) = MemePredicate(aspect.name)


fun makeResourcePackMemes(pack: ResourcePack) = pack.resources
        .map { makeResourceMemes(it).flattenMemePair() }
        .flatten()

fun makeStratumMemes(stratum: Stratum): List<Meme> =
    stratum.places.flatMap { makeResourcePackMemes(it.owned) } + listOf(makeMeme(stratum))


fun makeAspectMemes(aspect: Aspect): Pair<MutableList<Meme>, List<Meme>> {
    val memes = mutableListOf<Meme>() to mutableListOf<Meme>()

    memes.first.add(makeMeme(aspect))

    if (aspect is ConverseWrapper) {
        val (first, second) = makeResourceMemes(aspect.resource)
        memes.first.addAll(first)
        memes.second.addAll(second)
        aspect.producedResources
                .map { makeResourceMemes(it) }
                .forEach { (first1, second1) ->
                    memes.first.addAll(first1)
                    memes.second.addAll(second1)
                }
    }

    return memes
}

fun makeResourceMemes(resource: Resource): Pair<List<Meme>, List<Meme>> {
    val memes = makeResourceInfoMemes(resource)

    memes.first.add(makeMeme(resource))

    return memes
}

private fun makeResourceInfoMemes(resource: Resource): Pair<MutableList<Meme>, MutableList<Meme>> {
    val memes = mutableListOf<Meme>() to mutableListOf<Meme>()

    for (resourceDependency in resource.genome.dependencies)
        if (resourceDependency is ConsumeDependency)
            for (res in resourceDependency.lastConsumed) {
                val subject: Meme = MemeSubject(res.toLowerCase())
                memes.first.add(subject)
                val element = makeMeme(resource).addPredicate(MemePredicate("consume"))
                element.predicates[0].addPredicate(subject)
                memes.second.add(element)
            }
    return memes
}

fun Pair<List<Meme>, List<Meme>>.flattenMemePair() = first + second