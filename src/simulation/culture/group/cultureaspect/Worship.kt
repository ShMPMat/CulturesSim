package simulation.culture.group.cultureaspect

import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request

open class Worship(
        val taleSystem: TaleSystem,
        val depictSystem: DepictSystem,
        val placeSystem: PlaceSystem
) : CultureAspect {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        taleSystem.use(group)
        depictSystem.use(group)
        placeSystem.use(group)
        if (testProbability(0.05 / (depictSystem.depictions.size + 1), session.random)) {
            val depiction = createDepictObject(
                    group.cultureCenter.aspectCenter.aspectPool.getMeaningAspects(),
                    taleSystem.groupingMeme,
                    group,
                    session.random
            ) ?: return
            depictSystem.addDepiction(depiction)
        }
        if (testProbability(0.05 / (taleSystem.tales.size + 1), session.random)) {
            val tale = createTale(
                    group,
                    session.templateBase,
                    session.random
            ) ?: return
            taleSystem.addTale(tale)
        }
        update(group)
    }

    private fun update(group: Group) {
        if (!testProbability(session.worshipPlaceProb / (1 + placeSystem.places.size), session.random))
            return
        placeSystem.addPlace(createSpecialPlaceForWorship(this, group, session.random) ?: return)
    }

    override fun copy(group: Group): Worship {
        return Worship(taleSystem.copy(group), depictSystem.copy(group), placeSystem.copy(group))
    }

    override fun toString(): String {
        return "Worship of ${taleSystem.groupingMeme}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Worship

        if (taleSystem != other.taleSystem) return false

        return true
    }

    override fun hashCode(): Int {
        return taleSystem.hashCode()
    }
}