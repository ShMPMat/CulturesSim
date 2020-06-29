package simulation.culture.group.centers

import extra.OutputFunc
import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller.*
import simulation.Event
import simulation.culture.aspect.Aspect
import simulation.culture.group.*
import simulation.culture.group.cultureaspect.CultureAspect
import simulation.culture.group.request.Request
import simulation.culture.thinking.meaning.GroupMemes
import simulation.culture.thinking.meaning.MemeSubject
import simulation.space.Territory
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.container.ResourcePack
import simulation.space.tile.Tile
import java.util.*

class Group(
        val administrationCenter: AdministrationCenter,
        val resourceCenter: ResourceCenter,
        var parentGroup: GroupConglomerate,
        var name: String,
        val populationCenter: PopulationCenter,
        val relationCenter: RelationCenter,
        tile: Tile,
        aspects: List<Aspect>,
        memePool: GroupMemes,
        cultureAspects: Collection<CultureAspect>,
        spreadAbility: Double
) {
    var state = State.Live
    val fertility = session.defaultGroupFertility
    val cultureCenter = CultureCenter(this, memePool, aspects)
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
        addEvent(Event(Event.Type.Death, "Group $name died", "group", this))
        for (group in relationCenter.relatedGroups)
            group.cultureCenter.memePool.addMemeCombination(
                    cultureCenter.memePool.getMeme("group")
                            .addPredicate(MemeSubject(name))
                            .addPredicate(cultureCenter.memePool.getMeme("die"))
            )
    }

    fun addEvent(event: Event) {
        cultureCenter.events.add(event)
    }

    fun update() {
        val others = System.nanoTime()
        populationCenter.update(territoryCenter.accessibleTerritory, this)
        cultureCenter.requestCenter.updateRequests(this)
        populationCenter.executeRequests(cultureCenter.requestCenter.turnRequests)
        territoryCenter.update()
        if (state == State.Dead)
            return
        val main = System.nanoTime()
        if (shouldMigrate())
            if (territoryCenter.migrate()) {
                resourceCenter.moveToNewStorage(territoryCenter.territory.center)
                populationCenter.movePopulation(territoryCenter.territory.center)
            }
        if (populationCenter.isMinPassed(territoryCenter.territory))
            territoryCenter.expand()
        else
            territoryCenter.shrink()
        session.groupMigrationTime += System.nanoTime() - main
        checkNeeds()
        cultureCenter.update()
        administrationCenter.update(this)
        session.groupInnerOtherTime += System.nanoTime() - others
    }

    private fun checkNeeds() {
        val need = resourceCenter.direNeed ?: return
        cultureCenter.addNeedAspect(need)
        populationCenter.wakeNeedStrata(need)
    }

    private fun shouldMigrate(): Boolean {
        //TODO worth it?
        if (testProbability(0.9, session.random))
            return false
        if (resourceCenter.hasDireNeed())
            _direNeedTurns++
        else
            _direNeedTurns = 0
        return _direNeedTurns > 5 + territoryCenter.notMoved / 10
    }

    fun populationUpdate(): ConglomerateCommand? {
        if (populationCenter.population == 0) {
            die()
            return null
        }
        val denominator = parentGroup.subgroups.size + 1
        val isMax = populationCenter.isMaxReached(territoryCenter.territory)
        if (isMax || testProbability(session.defaultGroupDiverge / denominator, session.random)) {
            val tiles = overallTerritory.getOuterBrink {
                territoryCenter.canSettleAndNoGroup(it) && parentGroup.getClosestInnerGroupDistance(it) > 2
            }
            if (tiles.isEmpty())
                return null
            if (!session.groupMultiplication)
                return null
            val tile = randomElement(tiles, session.random)
            val aspects = cultureCenter.aspectCenter.aspectPool.all
                    .map { it.copy(it.dependencies) }
            val memes = GroupMemes()
            memes.addAll(cultureCenter.memePool)
            val pack = MutableResourcePack()
            resourceCenter.pack.resources.forEach {
                pack.addAll(resourceCenter.takeResource(it, it.amount / 2))
            }
            val name = parentGroup.newName
            return Add(Group(
                    AdministrationCenter(AdministrationType.Subordinate),
                    ResourceCenter(pack, tile, name),
                    parentGroup,
                    name,
                    populationCenter.getPart(0.5, tile),
                    RelationCenter(relationCenter.hostilityCalculator),
                    tile,
                    aspects,
                    memes,
                    cultureCenter.cultureAspectCenter.aspectPool.all,
                    territoryCenter.spreadAbility
            ))
        }
        return null
    }

    fun intergroupUpdate() {
        relationCenter.requestTrade(cultureCenter.requestCenter.turnRequests)
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
        populationCenter.manageNewAspects(cultureCenter.finishAspectUpdate(), territoryCenter.territory.center)
        populationCenter.finishUpdate(this)
        resourceCenter.finishUpdate()
        cultureCenter.finishUpdate()
    }

    fun diverge(): Boolean {
        if (!session.groupDiverge)
            return false
        if (parentGroup.subgroups.size <= 1)
            return false
        val relations = relationCenter.getAvgConglomerateRelation(parentGroup)
        val exitProbability = session.defaultGroupExiting /*/ (relations * relations * relations)*/
        if (testProbability(exitProbability, session.random)) {
            if (checkCoherencyAndDiverge())
                createNewConglomerate(setOf(this))
            return true
        }
        return false
    }

    private fun checkCoherencyAndDiverge(): Boolean {
        val queue: Queue<Group> = ArrayDeque()
        queue.add(this)
        val cluster: MutableSet<Group> = HashSet()
        while (!queue.isEmpty()) {
            val cur = queue.poll()
            cluster.add(cur)
            queue.addAll(cur.territoryCenter.getAllNearGroups(cur)
                    .filter { it.parentGroup === parentGroup }
                    .filter { !cluster.contains(it) }
            )
        }
        if (parentGroup.subgroups.size == cluster.size)
            return false
        createNewConglomerate(cluster)
        return true
    }

    private fun createNewConglomerate(groups: Collection<Group>) {
        val conglomerate = GroupConglomerate(0, territoryCenter.territory.center)
        for (group in groups) {
            group.parentGroup.removeGroup(group)
            conglomerate.addGroup(group)
        }
        session.world.addGroupConglomerate(conglomerate)
    }

    fun askFor(request: Request, owner: Group) =
            if (owner.parentGroup != parentGroup) ResourcePack()
            else populationCenter.executeRequest(request.reassign(this)).pack

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

    override fun toString(): String {
        var builder = StringBuilder("Group $name is $state, population=${populationCenter.population}, aspects:\n")
        for (aspect in cultureCenter.aspectCenter.aspectPool.all)
            builder.append(aspect).append("\n\n")
        val s = StringBuilder()
        s.append("Aspects: ")
        for (aspect in cultureCenter.cultureAspectCenter.aspectPool.all)
            s.append(aspect).append(", ")
        s.append(if (cultureCenter.cultureAspectCenter.aspectPool.isEmpty()) "none\n" else "\n")
        builder.append(s.toString())
        builder.append("\n").append(resourceCenter.toString())
                .append(populationCenter.toString())
                .append("\n").append(relationCenter)
                .append("\n")
        builder.append(cultureCenter.requestCenter)
        builder = OutputFunc.chompToSize(builder, 70)
        return builder.toString()
    }

    enum class State {
        Live, Dead
    }
}