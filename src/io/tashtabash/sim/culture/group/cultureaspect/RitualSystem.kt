package io.tashtabash.sim.culture.group.cultureaspect

import io.tashtabash.random.singleton.chanceOfNot
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.cultureaspect.util.createRitual
import io.tashtabash.sim.culture.group.reason.Reason
import io.tashtabash.sim.culture.group.request.Request
import java.util.*


class RitualSystem(rituals: Collection<Ritual>, val reason: Reason) : CultureAspect {
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
        session.groupCollapsedAspectUpdate.chanceOfNot {
            return
        }
        val ritual = createRitual(reason, group, session.random)
                ?: return
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
