package io.tashtabash.sim.culture.thinking.meaning

import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.generator.culture.worldview.MemePool
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.space.resource.Resource


class GroupMemes : MemePool() {
    private val memesCombinationsMap = mutableMapOf<String, Meme>()

    private var popularMemes = listOf<Meme>()

    private fun updatePopular() {
        popularMemes = all.sortedByDescending { it.importance }.take(40)
    }

    init {
        val startMemes: MutableList<Meme> = listOf("group", "time", "space", "life", "death", "sun", "luck", "misfortune")
                .map { Meme(it) }
                .toMutableList()
        startMemes += listOf("die", "acquireAspect", "consume", "exist", "and")
                .map { Meme(it) }
        startMemes += session.templateBase.wordBase.values
                .flatten()
                .map { it.copy() }
        addAll(startMemes)

        updatePopular()
    }

    override val isEmpty: Boolean
        get() = super.isEmpty && memesCombinationsMap.isEmpty()

    fun addAll(groupMemes: GroupMemes) {
        super.addAll(groupMemes)

        groupMemes.memesCombinationsMap.values
                .forEach { addMemeCombination(it) }
    }

    override val all get() = memesMap.values + memesCombinationsMap.values

    override fun getMeme(name: String) = super.getMeme(name)
            ?: getMemeCombinationByName(name)

    override fun getMemeCopy(name: String): Meme? {
        super.getMemeCopy(name)?.let { return it.copy() }

        return getMemeCombinationByName(name)?.copy()
    }

    private fun getMemeCombinationByName(name: String) =
            memesCombinationsMap[name.lowercase()]

    val valuableMeme: Meme
        get() {
            0.95.chanceOf {
                return chooseMeme(popularMemes)
            }

            updatePopular()

            return chooseMeme(all)
        }

    val memeWithComplexityBias: Meme
        get() = 0.75.chanceOf<Meme> { valuableMeme }
                ?: chooseMeme(memesCombinationsMap.values.sortedBy { it.toString() })

    private fun chooseMeme(memeList: List<Meme>) = memeList.randomElement { it.importance.toDouble() }

    fun addAspectMemes(aspect: Aspect) = addPairMemes(makeAspectMemes(aspect))

    fun addResourceMemes(resource: Resource) = addPairMemes(makeResourceMemes(resource))

    fun addPairMemes(memes: Pair<List<Meme>, List<Meme>>) {
        memes.first.forEach { add(it) }
        memes.second.forEach { addMemeCombination(it) }
    }

    fun addMemeCombination(meme: Meme) {
        if (!memesCombinationsMap.containsKey(meme.toString()))
            memesCombinationsMap[meme.toString()] = meme.copy()
    }

    override fun strengthenMeme(meme: Meme) = strengthenMeme(meme, 1)

    fun strengthenMemes(memes: List<Meme>) = memes.forEach { strengthenMeme(it, it.importance) }

    override fun strengthenMeme(meme: Meme, delta: Int): Boolean {
        if (super.strengthenMeme(meme, delta))
            return true

        getMemeCombinationByName(meme.toString())
                ?.let {
                    it.increaseImportance(delta)
                    return true
                }

        if (meme.isSimple)
            add(meme.copy())
        else
            addMemeCombination(meme.copy())

        return true
    }
}
