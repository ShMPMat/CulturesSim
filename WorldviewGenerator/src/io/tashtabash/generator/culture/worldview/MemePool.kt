package io.tashtabash.generator.culture.worldview

import java.util.*
import kotlin.collections.forEach
import kotlin.collections.sortedBy
import kotlin.let


open class MemePool private constructor(memes: Collection<Meme>) {
    protected constructor() : this(ArrayList<Meme>())

    protected val memesMap = TreeMap<String, Meme>()

    init {
        addAll(memes)
    }

    open val isEmpty: Boolean
        get() = memesMap.isEmpty()

    fun add(meme: Meme): Boolean {
        if (memesMap.containsKey(meme.toString()))
            return false

        memesMap[meme.toString()] = meme.copy()

        return true
    }

    fun addAll(memes: Collection<Meme>) = memes.forEach { add(it) }

    fun addAll(pool: MemePool) = addAll(pool.memesMap.values)

    open val all get() = memesMap.values.sortedBy { it.toString() }

    open fun getMeme(name: String) = memesMap[name.lowercase()]

    open fun getMemeCopy(name: String) = getMeme(name)?.copy()

    open fun strengthenMeme(meme: Meme) = strengthenMeme(meme, 1)

    open fun strengthenMeme(meme: Meme, delta: Int) = memesMap[meme.toString()]
            ?.let {
                it.increaseImportance(delta)
                return true
            } ?: false
}
