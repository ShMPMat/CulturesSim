package simulation.culture.group.cultureaspect

import shmp.random.testProbability
import simulation.Controller
import simulation.Controller.*
import simulation.culture.group.CultStratum
import simulation.culture.group.GroupError
import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request
import simulation.culture.group.request.ResourceRequest
import java.util.function.BiFunction

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
        if (testProbability(0.01, session.random)) {
            stratum.population++
        }
        if (testProbability(0.1, session.random)) {//TODO lesser probability
//            val request = ResourceRequest(
//                    group,
//                    session.world.resourcePool.get("Temple"),
//                    1,
//                    1,
//                    BiFunction<> { foo, bar ->
//                        print()
//                    },
//                    {}
//            )
        }
    }

    override fun copy(group: Group) = CultWorship(worship.copy(group))

    override fun toString(): String {
        return "Cult of $worship"
    }


}