package simulation.culture.thinking.meaning

import shmp.random.testProbability
import kotlin.random.Random

fun constructAndAddMeme(
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