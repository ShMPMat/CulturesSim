package simulation.culture.group.cultureaspect

import simulation.culture.group.centers.Group
import simulation.culture.group.centers.TraitChange
import simulation.culture.group.request.Request
import simulation.culture.thinking.meaning.Meme
import java.util.*


class Concept private constructor(
        val meme: Meme,
        val traitChanges: List<TraitChange>,
        private var appliedTimes: Int
) : CultureAspect {
    constructor(meme: Meme, traitChanges: List<TraitChange>) : this(meme, traitChanges, 0)

    private val applyTimes = 10

    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        if (appliedTimes >= applyTimes)
            return

        group.cultureCenter.consumeAllTraitChanges(
                traitChanges.map { it.copy(delta = it.delta / applyTimes) }
        )

        appliedTimes++
    }

    override fun adopt(group: Group) = Concept(meme.copy(), traitChanges, appliedTimes)

    override fun die(group: Group) {}

    override fun toString() = "Concept '$meme' affecting " + traitChanges.joinToString()

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (other == null || javaClass != other.javaClass)
            return false

        val that = other as Concept

        return traitChanges == that.traitChanges
    }

    override fun hashCode() = Objects.hash(traitChanges)
}
