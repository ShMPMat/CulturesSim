package simulation.culture.thinking.meaning

import java.util.*


open class MemePool private constructor(memes: Collection<Meme>) {
    internal constructor() : this(ArrayList<Meme>())

    protected val memes = mutableMapOf<String, Meme>()

    init {
        addAll(memes)
    }

    open val isEmpty: Boolean
        get() = memes.isEmpty()

    fun add(meme: Meme): Boolean {
        if (memes.containsKey(meme.toString()))
            return false

        memes[meme.toString()] = meme.copy()

        return true
    }

    fun addAll(memes: Collection<Meme>) = memes.forEach { add(it) }

    fun addAll(pool: MemePool) = addAll(pool.memes.values)

    open val all get() = memes.values.toList()

    open fun getMeme(name: String) = memes[name.toLowerCase()]//TODO wat?

    open fun getMemeCopy(name: String) = getMeme(name)?.copy()

    open fun strengthenMeme(meme: Meme) = strengthenMeme(meme, 1)

    open fun strengthenMeme(meme: Meme, delta: Int) = memes[meme.toString()]
            ?.let {
                it.increaseImportance(delta)
                return true
            } ?: false
}
