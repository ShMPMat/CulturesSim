package simulation.culture.thinking.meaning

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller
import simulation.culture.aspect.Aspect
import simulation.space.resource.Resource
import java.util.*
import kotlin.math.pow


class GroupMemes : MemePool() {
    private val memesCombinations = mutableMapOf<String, Meme>()

    init {
        addAll(
                listOf("group", "time", "space", "life", "death", "sun", "luck", "misfortune")
                        .map { MemeSubject(it) }
        )

        addAll(
                listOf("die", "acquireAspect", "consume", "exist", "and")
                        .map { MemePredicate(it) }
        )

        addAll(
                Controller.session.templateBase.wordBase.values
                        .flatten()
                        .map { it.copy() }
        )
    }

    override val isEmpty: Boolean
        get() = super.isEmpty && memesCombinations.isEmpty()

    fun addAll(groupMemes: GroupMemes) {
        super.addAll(groupMemes)

        groupMemes.memesCombinations.values
                .forEach { addMemeCombination(it) }
    }

    override val all get() = memes.values.toMutableList() + memesCombinations.values

    override fun getMeme(name: String) = super.getMeme(name)
            ?: getMemeCombinationByName(name)

    override fun getMemeCopy(name: String): Meme? {
        super.getMemeCopy(name)?.let { return it.copy() }

        return getMemeCombinationByName(name)?.copy()
    }

    private fun getMemeCombinationByName(name: String) =
            memesCombinations[name.toLowerCase()]//TODO doublewut

    val valuableMeme: Meme
        get() = chooseMeme(all)

    val memeWithComplexityBias: Meme
        get() =
            if (testProbability(0.5, Controller.session.random))
                valuableMeme
            else
                chooseMeme(ArrayList(memesCombinations.values))

    private fun chooseMeme(memeList: List<Meme>): Meme {
        return randomElement(
                memeList,
                { it.importance.toDouble().pow(2) },
                Controller.session.random
        )
    }

    fun addAspectMemes(aspect: Aspect) = addPairMemes(makeAspectMemes(aspect))

    fun addResourceMemes(resource: Resource) = addPairMemes(makeResourceMemes(resource))

    fun addPairMemes(memes: Pair<List<Meme>, List<Meme>>) {
        memes.first.forEach { add(it) }
        memes.second.forEach { addMemeCombination(it) }
    }

    fun addMemeCombination(meme: Meme) {
        if (!memesCombinations.containsKey(meme.toString()))
            memesCombinations[meme.toString()] = meme.copy()
    }

    override fun strengthenMeme(meme: Meme)
            = strengthenMeme(meme, 1)

    override fun strengthenMeme(meme: Meme, delta: Int): Boolean {
        if (super.strengthenMeme(meme, delta))
            return true

        getMemeCombinationByName(meme.toString())
                ?.let {
                    it.increaseImportance(delta)
                    return true
                }

        if (meme.isSimple) {
            add(meme.copy())
            return true
        }

        addMemeCombination(meme.copy())
        return true
    }
}