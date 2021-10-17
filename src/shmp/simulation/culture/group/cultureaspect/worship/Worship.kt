package shmp.simulation.culture.group.cultureaspect.worship

import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement
import shmp.simulation.CulturesController.session
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.cultureaspect.*
import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.concept.ObjectConcept
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.culture.group.centers.groupBaseConversions
import shmp.simulation.culture.group.cultureaspect.util.createDepictObject
import shmp.simulation.culture.group.cultureaspect.util.createSpecialPlaceForWorship
import shmp.simulation.culture.group.cultureaspect.util.createTale
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.group.request.RequestType
import kotlin.math.pow


open class Worship(
        val worshipObject: WorshipObject,
        val taleSystem: TaleSystem,
        val depictSystem: DepictSystem,
        val placeSystem: PlaceSystem,
        val reasonComplex: ReasonComplex,
        private val _features: MutableList<WorshipFeature>
) : CultureAspect, WorshipObjectDependent {
    init {
        if (worshipObject.name != taleSystem.groupingConcept.meme || worshipObject.name != depictSystem.groupingMeme)
            throw GroupError("Inconsistent Worship: worship object's name is ${worshipObject.name}" +
                    " but TaleSystem's concept is ${taleSystem.groupingConcept}" +
                    " and DepictSystem's is ${depictSystem.groupingMeme}")
    }

    val features: List<WorshipFeature> = _features

    private val newFeatures = mutableListOf<WorshipFeature>()

    internal val usingGroups = mutableSetOf<Group>()

    val cult
        get() = _features.filterIsInstance<Cult>().firstOrNull()

    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        usingGroups.add(group)

        taleSystem.use(group)
        depictSystem.use(group)
        placeSystem.use(group)
        _features.forEach { it.use(group, this) }

        update(group)
        manageFeatures(group)
    }

    private fun update(group: Group) {
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
        (0.25 / (_features.filterIsInstance<Fetish>().size + 1.0).pow(2)).chanceOf {
            makeWorshipObject(this, group)?.let {
                val fetish = Fetish(it)

                if (!_features.contains(fetish))
                    _features.add(fetish)
            }
        }
        if (group.populationCenter.freePopulation > 0 && cult == null)
            0.01.chanceOf {
                _features.add(Cult(simpleName))
            }

        session.reasoningUpdate.pow(0.5).chanceOf {
            groupBaseConversions().randomElementOrNull() //TODO mb culture conversions?
                    ?.enrichComplex(reasonComplex, group.cultureCenter.cultureAspectCenter.reasonField)
        }

        (0.002 / (_features.filterIsInstance<BannedResource>().size + 1)).chanceOf {
            makeWorshipObject(this, group)?.let {
                _features.add(BannedResource(
                        it,
                        listOf(setOf(), setOf(RequestType.Spiritual)).randomElement()
                ))
            }
        }
    }

    private fun manageFeatures(group: Group) {
        _features.filter { it.defunctTurns >= session.worshipFeatureFalloff }.forEach {
            _features.remove(it)
            it.die(group, this)
        }

        _features.addAll(newFeatures)
    }

    internal fun addWorshipPlace(group: Group) {
        val place = createSpecialPlaceForWorship(this, group)
                ?: return

        placeSystem.addPlace(place)
    }

    fun addFeature(feature: WorshipFeature) = newFeatures.add(feature)

    override fun adopt(group: Group): Worship? {
        val newFeatures = _features.map { it.adopt(group) }
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
        usingGroups.remove(group)

        if (usingGroups.isNotEmpty())
            return

        _features.forEach { it.die(group, this) }
        taleSystem.die(group)
        depictSystem.die(group)
        placeSystem.die(group)
    }

    override fun swapWorship(worshipObject: WorshipObject): Worship? {
        return Worship(
                worshipObject,
                taleSystem.swapWorship(worshipObject) ?: return null,
                depictSystem.swapWorship(worshipObject),
                PlaceSystem(mutableSetOf(), true),
                reasonComplex.copy(),
                _features.map { it.swapWorship(worshipObject) }.toMutableList()
        )
    }

    val simpleName = "Worship of ${taleSystem.groupingConcept}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Worship) return false

        if (worshipObject.name != other.worshipObject.name) return false

        return true
    }

    override fun hashCode() = worshipObject.name.hashCode()

    override fun toString() = "$simpleName, " +
            "${usingGroups.size} groups, " +
            "reasons: ${reasonComplex.reasonings.joinToString()} " +
            "features - ${_features.joinToString()}"
}
