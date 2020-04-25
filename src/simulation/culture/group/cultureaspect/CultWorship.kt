package simulation.culture.group.cultureaspect

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.CultStratum
import simulation.culture.group.GroupError
import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request

data class CultWorship(
        val worship: Worship
) : CultureAspect {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        worship.use(group)
        val stratum = group.populationCenter.strata
                .filterIsInstance<CultStratum>()
                .firstOrNull { it.cultName == toString() }
                ?: kotlin.run {
                    val newStratum = CultStratum(toString())
                    group.populationCenter.addStratum(newStratum)
                    group.populationCenter.strata
                            .filterIsInstance<CultStratum>()
                            .firstOrNull { it.cultName == toString() }
                            ?: throw GroupError("Cannot create Stratum for $this")
                }
        if (testProbability(0.01, Controller.session.random)) {
            stratum.population++
        }
    }

    override fun copy(group: Group) = CultWorship(worship.copy(group))

    override fun toString(): String {
        return "Cult of $worship"
    }


}