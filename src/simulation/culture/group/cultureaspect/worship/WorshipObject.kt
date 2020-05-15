package simulation.culture.group.cultureaspect.worship

import simulation.culture.group.centers.Group
import simulation.culture.group.cultureaspect.CultureAspect
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.MemePredicate
import simulation.culture.thinking.meaning.MemeSubject

interface WorshipObject {
    val name: Meme
    val memes: Collection<Meme>

    fun copy(group: Group): WorshipObject
}

class MemeWorship(override val name: Meme) : WorshipObject {
    override val memes = listOf(name)

    override fun copy(group: Group) = MemeWorship(name.copy())
}

class GodWorship(val godName: Meme, val sphere: Meme): WorshipObject {
    override val name: Meme = MemeSubject("god").addPredicate(
            godName.copy().addPredicate(MemePredicate("of").addPredicate(sphere.copy()))
    )
    override val memes = listOf(name, godName, sphere)

    override fun copy(group: Group) = GodWorship(godName.copy(), sphere.copy())
}


interface WorshipObjectDependent {
    fun swapWorship(worshipObject: WorshipObject) : WorshipObjectDependent
}