package io.tashtabash.simulation.culture.thinking.meaning

import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.simulation.culture.aspect.Aspect
import io.tashtabash.simulation.culture.aspect.ConverseWrapper
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.stratum.Stratum
import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.container.ResourcePack
import io.tashtabash.simulation.space.resource.dependency.ConsumeDependency


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

fun makePredicateChain(vararg memes: Meme) = memes.toList()
        .asReversed()
        .let {
            if (it.isEmpty())
                throw ArrayIndexOutOfBoundsException("Non-empty array is expected for a predicate chain")

            it.drop(1).fold(it[0]) { meme, prevMeme ->
                prevMeme.addPredicate(meme)
            }
        }

fun makeResourcePackMemes(pack: ResourcePack) = pack.resources
        .map { makeResourceMemes(it).flattenMemePair() }
        .flatten()

fun makeStratumMemes(stratum: Stratum): List<Meme> =
    stratum.places.flatMap { makeResourcePackMemes(it.owned) } + listOf(makeMeme(stratum))


fun makeAspectMemes(aspect: Aspect): Pair<MutableList<Meme>, List<Meme>> {
    val memes = mutableListOf<Meme>() to mutableListOf<Meme>()

    memes.first += makeMeme(aspect)

    if (aspect is ConverseWrapper) {
        val (first, second) = makeResourceMemes(aspect.resource)
        memes.first += first
        memes.second += second
        aspect.producedResources
                .map { makeResourceMemes(it) }
                .forEach { (first1, second1) ->
                    memes.first += first1
                    memes.second += second1
                }
    }

    return memes
}

fun makeResourceMemes(resource: Resource): Pair<List<Meme>, List<Meme>> {
    val memes = makeResourceInfoMemes(resource)

    memes.first += makeMeme(resource)

    return memes
}

private fun makeResourceInfoMemes(resource: Resource): Pair<MutableList<Meme>, MutableList<Meme>> {
    val memes = mutableListOf<Meme>() to mutableListOf<Meme>()

    for (resourceDependency in resource.genome.dependencies)
        if (resourceDependency is ConsumeDependency)
            for (res in resourceDependency.lastConsumed(resource.baseName)) {
                val subject = Meme(res)
                memes.first += subject
                memes.second += makePredicateChain(makeMeme(resource), Meme("consume"), subject)
            }

    return memes
}

fun Pair<List<Meme>, List<Meme>>.flattenMemePair() = first + second