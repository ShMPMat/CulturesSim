package io.tashtabash.simulation.culture.group.cultureaspect.worship

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept.*
import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.generator.culture.worldview.Meme


interface WorshipObject {
    val name: Meme
    val memes: Collection<Meme>
    val concepts: Collection<ReasonConcept>

    fun copy(group: Group): WorshipObject
}

class GodWorship(val godName: Meme, val sphere: ReasonConcept): WorshipObject, ArbitraryObjectConcept(godName) {
    override val name = Meme("god $godName of $sphere")
    override val memes = listOf(name, godName, sphere.meme)
    override val concepts = listOf(this, sphere)

    override val meme = name
    override val oppositeConcepts = sphere.oppositeConcepts
    override val correspondingConcepts = sphere.correspondingConcepts

    override fun copy(group: Group) = GodWorship(godName.copy(), sphere)
}

class ConceptObjectWorship(val objectConcept: ObjectConcept) : WorshipObject {
    override val name = objectConcept.meme
    override val memes = listOf(name)
    override val concepts = listOf(objectConcept)

    override fun copy(group: Group) = ConceptObjectWorship(objectConcept)
}


interface WorshipObjectDependent {
    fun swapWorship(worshipObject: WorshipObject) : WorshipObjectDependent?
}
