package io.tashtabash.sim.culture.thinking.meaning

import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept
import io.tashtabash.generator.culture.worldview.toMeme
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.stratum.Stratum
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.action.ResourceProbabilityAction
import io.tashtabash.sim.space.resource.container.ResourcePack
import io.tashtabash.sim.space.resource.dependency.ConsumeDependency


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

fun makePredicateChain(memes: List<Meme>) = memes.asReversed()
        .let {
            if (it.isEmpty())
                throw ArrayIndexOutOfBoundsException("Non-empty array is expected for a predicate chain")

            it.drop(1).fold(it[0]) { meme, prevMeme ->
                prevMeme.addPredicate(meme)
            }
        }

fun makePredicateChain(vararg memes: Meme) = makePredicateChain(memes.toList())

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

    for ((resourceAction, resources) in resource.genome.conversionCore.actionConversions) {
        if (resourceAction !is ResourceProbabilityAction)
            continue

        val actionMemeChain = resourceAction.name
                .split("(?<=[a-z])(?=[A-Z])".toRegex())
                .map { it.toMeme() }

        for (r in resources) {
            val fullChain = listOf(makeMeme(resource)) + actionMemeChain + makeMeme(r)

            memes.first += actionMemeChain + makeMeme(r)
            memes.second += makePredicateChain(fullChain)
        }
    }

    val predicateMemes = mutableListOf<Meme>()
    // Add appearance
    predicateMemes += listOfNotNull(
            resource.genome.appearance.colour?.name?.toMeme(),
            resource.genome.appearance.texture?.name?.toMeme(),
            resource.genome.appearance.shape?.name?.toMeme()
    )
    // Add behaviours
     predicateMemes += listOfNotNull(
            resource.genome.behaviour.resistance.takeIf { it > 0.5 }?.let { IdeationalConcept.Robustness.meme },
            resource.genome.behaviour.resistance.takeIf { it == 0.0 }?.let { IdeationalConcept.Fragility.meme },
            resource.genome.behaviour.danger.takeIf { it > 0.5 }?.let { IdeationalConcept.Danger.meme },
            resource.genome.behaviour.danger.takeIf { it == 0.0 }?.let { IdeationalConcept.Safety.meme },
            resource.genome.behaviour.camouflage.takeIf { it > 0.5 }?.let { IdeationalConcept.Secret.meme },
            resource.genome.behaviour.speed.takeIf { it >= 3 }?.let { IdeationalConcept.Fast.meme },
            resource.genome.behaviour.danger.takeIf { it <= 0.1 }?.let { IdeationalConcept.Slow.meme },
    )
    //Merge predicateMemes
    memes.first += predicateMemes
    memes.second += predicateMemes.map {
        makeMeme(resource).addPredicate(it)
    }

    return memes
}

fun Pair<List<Meme>, List<Meme>>.flattenMemePair() = first + second