package shmp.simulation.culture.thinking.meaning

import shmp.random.randomElement
import shmp.random.testProbability
import shmp.simulation.CulturesController.session
import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.space.resource.Resource
import kotlin.math.pow


class GroupMemes : MemePool() {
    private val memesCombinationsMap = mutableMapOf<String, Meme>()

    private var popularMemes = listOf<Meme>()

    private fun updatePopular() {
        popularMemes = all.sortedByDescending { it.importance }.take(50)
    }

    init {
        val startMemes: MutableList<Meme> = listOf("group", "time", "space", "life", "death", "sun", "luck", "misfortune")
                .map { MemeSubject(it) }
                .toMutableList()
        startMemes += listOf("die", "acquireAspect", "consume", "exist", "and")
                .map { MemePredicate(it) }
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
            memesCombinationsMap[name.toLowerCase()]

    val valuableMeme: Meme
        get() {
            if (testProbability(0.9, session.random))
                return chooseMeme(popularMemes)

            updatePopular()

            return chooseMeme(all)
        }

    val memeWithComplexityBias: Meme
        get() =
            if (testProbability(0.5, session.random))
                valuableMeme
            else
                chooseMeme(memesCombinationsMap.values.toList())

    private fun chooseMeme(memeList: List<Meme>): Meme {
        return randomElement(
                memeList,
                { it.importance.toDouble().pow(2) },
                session.random
        )
    }

    fun addAspectMemes(aspect: Aspect) = addPairMemes(makeAspectMemes(aspect))

    fun addResourceMemes(resource: Resource) = addPairMemes(makeResourceMemes(resource))

    fun addPairMemes(memes: Pair<List<Meme>, List<Meme>>) {
        memes.first.forEach { add(it) }
        memes.second.forEach { addMemeCombination(it) }
    }

    fun addMemeCombination(meme: Meme) {
        if (!memesCombinationsMap.containsKey(meme.toString()))
            memesCombinationsMap[meme.toString().toLowerCase()] = meme.copy()
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
