package shmp.simulation.culture.thinking.meaning

import shmp.generator.culture.worldview.Meme
import shmp.random.singleton.chanceOf
import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.ConverseWrapper
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.stratum.Stratum
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.ResourcePack
import shmp.simulation.space.resource.dependency.ConsumeDependency


fun constructAndAddSimpleMeme(groupMemes: GroupMemes, complicateProbability: Double = 0.1, maxTests: Int = 10): Meme? {
    var meme: Meme = groupMemes.memeWithComplexityBias.copy()
    complicateProbability.chanceOf {
        var second: Meme
        var tests = 0
        do {
            second = groupMemes.memeWithComplexityBias.copy()
            tests++
        } while (second.contains(" and ") && meme.contains(second.toString()) && tests <= maxTests)
        meme = meme.copy().apply {
            groupMemes.getMemeCopy("and")
                    ?.addPredicate(second)
                    ?.let { addPredicate(it) }
        }
        groupMemes.addMemeCombination(meme)
    }
    return meme
}


fun makeMeme(stratum: Stratum) = Meme(stratum.name)

fun makeMeme(group: Group) = Meme(group.name)

fun makeMeme(resource: Resource) = Meme(resource.fullName)

fun makeMeme(aspect: Aspect) = Meme(aspect.name)


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
            for (res in resourceDependency.lastConsumed(resource.baseName)) {
                val subject = Meme(res.lowercase())
                memes.first.add(subject)
                val element = makeMeme(resource).addPredicate(Meme("consume"))
                element.predicates[0].addPredicate(subject)
                memes.second.add(element)
            }
    return memes
}

fun Pair<List<Meme>, List<Meme>>.flattenMemePair() = first + second