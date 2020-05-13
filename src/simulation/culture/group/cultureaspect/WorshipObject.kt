package simulation.culture.group.cultureaspect

import simulation.culture.group.centers.Group
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.MemePredicate

interface WorshipObject {
    val name: Meme
    fun copy(group: Group): WorshipObject
}

class MemeWorship(override val name: Meme) : WorshipObject {
    override fun copy(group: Group) = MemeWorship(name.copy())
}

class GodWorship(val godName: Meme, val sphere: Meme): WorshipObject {
    override val name = godName.copy().addPredicate(MemePredicate("of").addPredicate(sphere.copy()))

    override fun copy(group: Group) = GodWorship(godName.copy(), sphere.copy())
}


interface WorshipObjectDependent {
    fun swapWorship(worshipObject: WorshipObject) : WorshipObjectDependent
}