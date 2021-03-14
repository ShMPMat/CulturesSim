package shmp.simulation.culture.group.centers

import shmp.random.singleton.chanceOf
import shmp.utils.chompToSize
import shmp.random.testProbability
import shmp.simulation.Controller.session
import shmp.simulation.event.Event
import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.group.GroupConglomerate
import shmp.simulation.culture.group.GroupTileTag
import shmp.simulation.culture.group.centers.util.MemoryConversion
import shmp.simulation.culture.group.cultureaspect.CultureAspect
import shmp.simulation.culture.group.cultureaspect.reasoning.ReasonField
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.CorrespondingConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.EqualitySubjectCorrelationConversion
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.OppositionConversion
import shmp.simulation.culture.thinking.meaning.GroupMemes
import shmp.simulation.culture.thinking.meaning.MemeSubject
import shmp.simulation.event.Type
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.tile.Tile
import java.util.*


class Group(
        val processCenter: ProcessCenter,
        val resourceCenter: ResourceCenter,
        var parentGroup: GroupConglomerate,
        val name: String,
        val populationCenter: PopulationCenter,
        val relationCenter: RelationCenter,
        cultureAspectCenter: CultureAspectCenter,
        traitCenter: TraitCenter,
        tile: Tile,
        aspectCenter: AspectCenter,
        memoryCenter: MemoryCenter,
        memePool: GroupMemes,
        cultureAspects: Collection<CultureAspect>,
        spreadAbility: Double
) {
    var state = State.Live
    val fertility = session.defaultGroupFertility
    val cultureCenter = CultureCenter(
            this,
            memePool,
            traitCenter,
            memoryCenter,
            cultureAspectCenter,
            aspectCenter
    )
    val territoryCenter = TerritoryCenter(this, spreadAbility, tile)
    private var _direNeedTurns = 0


    init {
        copyCA(cultureAspects)
    }

    private fun copyCA(aspects: Collection<CultureAspect>) {
        val retry = mutableListOf<CultureAspect>()
        for (aspect in aspects) {
            val copy = aspect.adopt(this)
            if (copy == null)
                retry.add(aspect)
            cultureCenter.cultureAspectCenter.addCultureAspect(copy)
        }
        if (retry.isNotEmpty()) {
            if (retry.size == aspects.size)
                return//                throw GroupError("Cannot adopt CultureAspect ${retry[0]}")//TODO deal with it
            copyCA(retry)
        }
    }

    val overallTerritory: Territory
        get() = parentGroup.territory

    fun die() {
        state = State.Dead
        resourceCenter.die()
        populationCenter.die()
        territoryCenter.die()
        cultureCenter.die(this)

        addEvent(Event(Type.Death, "Group $name died"))

        for (group in relationCenter.relatedGroups)
            group.cultureCenter.memePool.addMemeCombination(
                    cultureCenter.memePool.getMeme("group")
                            ?.addPredicate(MemeSubject(name))
                            ?.addPredicate(cultureCenter.memePool.getMeme("die"))
                            ?: continue
            )
    }

    fun addEvent(event: Event) = cultureCenter.events.add(event)

    fun addEvents(events: List<Event>) = cultureCenter.events.addAll(events)

    fun update() {
        val others = System.nanoTime()
        populationCenter.update(territoryCenter.accessibleTerritory, this)
        cultureCenter.requestCenter.updateRequests(this)
        populationCenter.executeRequests(cultureCenter.requestCenter.turnRequests)
        territoryCenter.update()

        if (state == State.Dead)
            return

        move()
        if (populationCenter.isMinPassed(territoryCenter.territory))
            territoryCenter.expand()
        else
            territoryCenter.shrink()
        checkNeeds()
        cultureCenter.update(this)
        processCenter.update(this)

        if (populationCenter.population == 0)
            die()
        session.groupInnerOtherTime += System.nanoTime() - others
    }

    private fun move() {
        if (shouldMigrate())
            if (territoryCenter.migrate()) {
                resourceCenter.moveToNewStorage(territoryCenter.center)
                populationCenter.movePopulation(territoryCenter.center)
            }
    }

    private fun checkNeeds() {
        val need = resourceCenter.direNeed
                ?: return

        cultureCenter.addNeedAspect(need)
        populationCenter.wakeNeedStrata(need)
    }

    private fun shouldMigrate(): Boolean {
        0.9.chanceOf {
            return false
        }
        if (resourceCenter.hasDireNeed())
            _direNeedTurns++
        else
            _direNeedTurns = 0
        return _direNeedTurns > 5 + territoryCenter.notMoved / 10
    }

    fun intergroupUpdate() {
        if (session.isTime(session.groupTurnsBetweenBorderCheck)) {
            var toUpdate = overallTerritory
                    .outerBrink //TODO dont like territory checks in Group
                    .flatMap { it.tagPool.getByType("Group") }
                    .map { (it as GroupTileTag).group }
                    .toMutableList()
            toUpdate.addAll(parentGroup.subgroups)
            toUpdate = toUpdate
                    .distinct()
                    .toMutableList()
            toUpdate.remove(this)
            relationCenter.updateRelations(toUpdate, this)
        }
        cultureCenter.intergroupUpdate(this)
    }

    fun finishUpdate() {
        resourceCenter.addAll(cultureCenter.requestCenter.turnRequests.finish())
        populationCenter.manageNewAspects(cultureCenter.finishAspectUpdate(), territoryCenter.center)
        populationCenter.finishUpdate(this)
        resourceCenter.finishUpdate()
        cultureCenter.finishUpdate()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (other == null || javaClass != other.javaClass)
            return false

        val group = other as Group
        return name == group.name
    }

    override fun hashCode() = Objects.hash(name)

    override fun toString() = chompToSize(
            """
        |Group $name is $state, population = ${populationCenter.population}, parent - ${parentGroup.name}
        |
        |$cultureCenter
        |
        |
        |$resourceCenter
        |
        |
        |$populationCenter
        |
        |
        |$relationCenter
        |
        |$processCenter
            """.trimMargin(),
            70
    ).toString()

    enum class State {
        Live, Dead
    }
}
