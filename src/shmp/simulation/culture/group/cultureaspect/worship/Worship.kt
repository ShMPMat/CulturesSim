package shmp.simulation.culture.group.cultureaspect.worship

import shmp.random.singleton.chanceOf
import shmp.random.singleton.chanceOfNot
import shmp.random.singleton.otherwise
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.Controller.session
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.baseConversions
import shmp.simulation.culture.group.cultureaspect.*
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonComplex
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept
import shmp.simulation.culture.group.cultureaspect.util.createDepictObject
import shmp.simulation.culture.group.cultureaspect.util.createSpecialPlaceForWorship
import shmp.simulation.culture.group.cultureaspect.util.createTale
import shmp.simulation.culture.group.request.Request
import kotlin.math.pow


open class Worship(
        val worshipObject: WorshipObject,
        val taleSystem: TaleSystem,
        val depictSystem: DepictSystem,
        val placeSystem: PlaceSystem,
        val reasonComplex: ReasonComplex,
        val features: MutableList<WorshipFeature>
) : CultureAspect, WorshipObjectDependent {
    init {
        if (worshipObject.name != taleSystem.groupingConcept.meme || worshipObject.name != depictSystem.groupingMeme)
            throw GroupError("Inconsistent Worship: worship object's name is ${worshipObject.name}" +
                    " but TaleSystem's concept is ${taleSystem.groupingConcept}")
    }

    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        taleSystem.use(group)
        depictSystem.use(group)
        placeSystem.use(group)
        (0.05 / (depictSystem.depictions.size + 1)).chanceOf {
            val depiction = createDepictObject(
                    group.cultureCenter.aspectCenter.aspectPool.getMeaningAspects(),
                    taleSystem.groupingConcept.meme,
                    taleSystem.groupingConcept.takeIf { it is ObjectConcept } as ObjectConcept
            ) ?: return
            depictSystem.addDepiction(depiction)
        }
        (0.05 / (taleSystem.tales.size + 1)).chanceOf {
            val tale = createTale(
                    group,
                    session.templateBase
            ) ?: return
            taleSystem.addTale(tale)
        }
        update(group)
    }

    private fun update(group: Group) {
        features.forEach { it.use(group, this) }
        if (group.populationCenter.freePopulation >= session.minimalStableFreePopulation
                && features.filterIsInstance<Cult>().isEmpty())
            0.01.chanceOf {
                features.add(Cult(simpleName))
            }

        session.reasoningUpdate.pow(0.5).chanceOf {
            baseConversions().randomElementOrNull()
                    ?.enrichComplex(reasonComplex, group.cultureCenter.cultureAspectCenter.reasonField)
        }

        (session.worshipPlaceProb / (1 + placeSystem.places.size)).chanceOfNot {
            return
        } otherwise {
            placeSystem.addPlace(
                    createSpecialPlaceForWorship(this, group)
                            ?: return
            )
        }
    }

    override fun adopt(group: Group): Worship? {
        val newFeatures = features.map { it.adopt(group) }
        if (newFeatures.any { it == null }) return null
        return Worship(
                worshipObject.copy(group),
                taleSystem.adopt(group) ?: return null,
                depictSystem.adopt(group) ?: return null,
                placeSystem.adopt(group),
                reasonComplex.copy(),
                newFeatures.filterNotNull().toMutableList()
        )
    }

    override fun die(group: Group) {
        features.forEach { it.die(group, this) }
        taleSystem.die(group)
        depictSystem.die(group)
        placeSystem.die(group)
    }

    override fun swapWorship(worshipObject: WorshipObject): Worship? {
        return Worship(
                worshipObject,
                taleSystem.swapWorship(worshipObject) ?: return null,
                depictSystem.swapWorship(worshipObject),
                PlaceSystem(mutableSetOf()),
                reasonComplex.copy(),
                features.map { it.swapWorship(worshipObject) }.toMutableList()
        )
    }

    val simpleName = "Worship of ${taleSystem.groupingConcept}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Worship

        if (worshipObject.name != other.worshipObject.name) return false

        return true
    }

    override fun hashCode() = worshipObject.name.hashCode()

    override fun toString() = "$simpleName, " +
            "reasons: ${reasonComplex.reasonings.joinToString()} " +
            "features - ${features.joinToString()}"
}

