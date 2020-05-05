package simulation.culture.group.cultureaspect

import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.group.centers.Group
import simulation.culture.group.reason.Reason
import simulation.culture.group.request.Request
import java.util.*

class RitualSystem constructor(rituals: Collection<Ritual>, val reason: Reason) : CultureAspect {
    private val _rituals: MutableSet<Ritual>

    val rituals: Set<Ritual>
        get() = _rituals

    init {
        this._rituals = HashSet(rituals)
    }

    fun addRitual(ritual: Ritual) {
        _rituals.add(ritual)
    }

    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        _rituals.forEach { it.use(group) }
        if (!testProbability(session.groupCollapsedAspectUpdate, session.random))
            return
        val ritual = constructRitual(reason, group, session.random)
        if (ritual != null)
            _rituals.add(ritual)
    }

    override fun adopt(group: Group): RitualSystem? {
        val newRituals = _rituals.map { it.adopt(group) }
        if (newRituals.any { it == null }) return null
        return RitualSystem(_rituals, reason)
    }

    override fun toString() = "Ritual system for $reason"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RitualSystem
        return _rituals == that._rituals
    }

    override fun hashCode() = Objects.hash(_rituals)

    override fun die(group: Group) = _rituals.forEach { it.die(group) }
}