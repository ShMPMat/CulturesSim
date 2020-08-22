package simulation.culture.group.centers

import extra.chompToSize
import shmp.random.testProbability
import simulation.Controller.session
import simulation.event.Event
import simulation.culture.aspect.Aspect
import simulation.culture.group.GroupConglomerate
import simulation.culture.group.GroupTileTag
import simulation.culture.group.cultureaspect.CultureAspect
import simulation.culture.thinking.meaning.GroupMemes
import simulation.culture.thinking.meaning.MemeSubject
import simulation.event.Type
import simulation.space.territory.Territory
import simulation.space.tile.Tile
import java.util.*

class Group(
        val processCenter: ProcessCenter,
        val resourceCenter: ResourceCenter,
        var parentGroup: GroupConglomerate,
        var name: String,
        val populationCenter: PopulationCenter,
        val relationCenter: RelationCenter,
        traitCenter: TraitCenter,
        tile: Tile,
        aspects: List<Aspect>,
        memePool: GroupMemes,
        cultureAspects: Collection<CultureAspect>,
        spreadAbility: Double
) {
    var state = State.Live
    val fertility = session.defaultGroupFertility
    val cultureCenter = CultureCenter(this, memePool, traitCenter, aspects)
    val territoryCenter = TerritoryCenter(this, spreadAbility, tile)
    private var _direNeedTurns = 0


    init {
        copyCA(cultureAspects)
    }

    private fun copyCA(aspects: Collection<CultureAspect>) {
        val retry: MutableList<CultureAspect> = ArrayList()
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
        cultureCenter.die()
        addEvent(Event(Type.Death, "Group $name died"))
        for (group in relationCenter.relatedGroups)
            group.cultureCenter.memePool.addMemeCombination(
                    cultureCenter.memePool.getMeme("group")
                            ?.addPredicate(MemeSubject(name))
                            ?.addPredicate(cultureCenter.memePool.getMeme("die"))
                            ?: continue
            )
    }

    fun addEvent(event: Event) {
        cultureCenter.events.add(event)
    }

    fun addEvents(events: List<Event>) {
        cultureCenter.events.addAll(events)
    }

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
        cultureCenter.update()
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
        val need = resourceCenter.direNeed ?: return
        cultureCenter.addNeedAspect(need)
        populationCenter.wakeNeedStrata(need)
    }

    private fun shouldMigrate(): Boolean {
        if (testProbability(0.9, session.random))
            return false
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
        cultureCenter.intergroupUpdate()
    }

    fun finishUpdate() {
        resourceCenter.addAll(cultureCenter.requestCenter.turnRequests.finish())
        populationCenter.manageNewAspects(cultureCenter.finishAspectUpdate(), territoryCenter.center)
        populationCenter.finishUpdate(this)
        resourceCenter.finishUpdate()
        cultureCenter.finishUpdate()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val group = other as Group
        return name == group.name
    }

    override fun hashCode() = Objects.hash(name)

    override fun toString() = chompToSize("""
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
    """.trimMargin(), 70).toString()

    enum class State {
        Live, Dead
    }
}
