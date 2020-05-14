package simulation.culture.group.cultureaspect.worship

import shmp.random.testProbability
import simulation.Controller.session
import simulation.culture.group.GroupError
import simulation.culture.group.centers.Group
import simulation.culture.group.cultureaspect.*
import simulation.culture.group.request.Request

open class Worship(
        val worshipObject: WorshipObject,
        val taleSystem: TaleSystem,
        val depictSystem: DepictSystem,
        val placeSystem: PlaceSystem
) : WorshipObjectDependent {
    init {
        if (worshipObject.name != taleSystem.groupingMeme || worshipObject.name != depictSystem.groupingMeme)
            throw GroupError("Inconsistent Worship: worship object's name is ${worshipObject.name}" +
                    " but TaleSystem's meme is ${taleSystem.groupingMeme}")
    }

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
        placeSystem.addPlace(createSpecialPlaceForWorship(this, group, session.random)
                ?: return)
    }

    override fun adopt(group: Group): Worship? {
        return Worship(
                worshipObject.copy(group),
                taleSystem.adopt(group) ?: return null,
                depictSystem.adopt(group) ?: return null,
                placeSystem.adopt(group)
        )
    }

    override fun die(group: Group) {
        taleSystem.die(group)
        depictSystem.die(group)
        placeSystem.die(group)
    }

    override fun swapWorship(worshipObject: WorshipObject) =
            Worship(
                    worshipObject,
                    taleSystem.swapWorship(worshipObject),
                    depictSystem.swapWorship(worshipObject),
                    PlaceSystem(mutableSetOf())
            )

    override fun toString(): String {
        return "Worship of ${taleSystem.groupingMeme}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Worship

        if (worshipObject.name != other.worshipObject.name) return false

        return true
    }

    override fun hashCode(): Int {
        return worshipObject.name.hashCode()
    }
}

