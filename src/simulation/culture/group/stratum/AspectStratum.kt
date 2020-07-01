package simulation.culture.group.stratum

import shmp.random.randomElement
import simulation.Controller.session
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.aspect.dependency.Dependency
import simulation.culture.aspect.getAspectImprovement
import simulation.culture.group.centers.Group
import simulation.culture.group.passingReward
import simulation.culture.group.place.StaticPlace
import simulation.culture.group.request.AspectImprovementRequest
import simulation.culture.group.request.Request
import simulation.culture.group.request.passingEvaluator
import simulation.culture.group.request.tagEvaluator
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.constructMeme
import simulation.space.Territory
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import simulation.space.tile.Tile
import simulation.space.tile.TileTag
import java.util.*
import java.util.function.Consumer
import kotlin.math.ceil
import kotlin.math.min

class AspectStratum(
        override var population: Int,
        val aspect: ConverseWrapper,
        tile: Tile
) : BaseStratum(tile, "Stratum of aspect ${aspect.name}") {
    private var _effectiveness = -1.0

    private var workedAmount = 0
    private var isRaisedAmount = false
    private val dependencies: MutableMap<ResourceTag, MutableResourcePack> = HashMap()
    private val enhancements = MutableResourcePack()
    private val popularMemes: MutableList<Meme> = ArrayList()
    override var importance: Int
        get() = aspect.usefulness
        set(value) {
            aspect.gainUsefulness(value - importance)
        }

    fun getInstrumentByTag(tag: ResourceTag) = dependencies[tag] ?: MutableResourcePack()

    override val freePopulation: Int
        get() = population - workedAmount


    init {
        aspect.dependencies.map.keys.forEach { tag: ResourceTag ->
            if (tag.isInstrumental() && tag.name != "phony")
                dependencies[tag.copy()] = MutableResourcePack()
        }
    }

    override fun decreaseAmount(amount: Int) {
        population -= amount
        if (workedAmount > population)
            workedAmount = population
    }

    fun decreaseWorkedAmount(amount: Int) {
        workedAmount -= amount
    }

    private val effectiveness: Double
        get() {
            if (_effectiveness == -1.0) {
                _effectiveness = 1.0 + places
                        .flatMap { it.owned.resources }
                        .map { it.getAspectImprovement(aspect) }
                        .foldRight(0.0, Double::plus)
            }
            if (_effectiveness > 1) {
                val k = 0
            }
            return _effectiveness
        }

    override fun useAmount(amount: Int, maxOverhead: Int): WorkerBunch {
        if (amount <= 0)
            return WorkerBunch(0, 0)

        var actualAmount = ceil(amount / effectiveness).toInt()
        if (actualAmount <= freePopulation)
            workedAmount += actualAmount
        else {
            val additional = min(maxOverhead, actualAmount - freePopulation)
            actualAmount = additional + freePopulation
            population += additional
            workedAmount = population
            if (additional > 0)
                isRaisedAmount = true
        }

        return WorkerBunch((actualAmount * effectiveness).toInt(), actualAmount)
    }

    override fun update(accessibleResources: MutableResourcePack, accessibleTerritory: Territory, group: Group) {
        if (population == 0)
            return

        val oldPopulation = population
        val evaluator = passingEvaluator
        val overhead = aspect.calculateNeededWorkers(evaluator, freePopulation).toDouble()
        val amount = aspect.calculateProducedValue(evaluator, freePopulation).toInt()
        val pack = use(AspectController(
                1,
                amount,
                amount,
                evaluator,
                group.populationCenter,
                accessibleTerritory,
                false,
                group,
                group.cultureCenter.meaning
        ))

        if (population < oldPopulation)
            population = oldPopulation
        accessibleResources.addAll(pack)
        updateInfrastructure(accessibleTerritory, group)
        isRaisedAmount = false
        ego.update(accessibleResources, accessibleTerritory, group, this)
    }

    fun use(controller: AspectController): MutableResourcePack {
        val resourcePack = MutableResourcePack()
        val result = aspect.use(controller)

        if (result.resources.isNotEmpty) {
            popularMemes.add(constructMeme(aspect))
            result.resources.resources.forEach(Consumer { r: Resource? -> popularMemes.add(constructMeme(r!!)) })
        }
        if (result.isFinished)
            resourcePack.addAll(result.resources)
        result.pushNeeds(controller.group)

        return resourcePack
    }

    private fun updateInfrastructure(accessibleTerritory: Territory, group: Group) {
        if (!session.isTime(session.stratumTurnsBeforeInstrumentRenewal))
            return

        updateTools(accessibleTerritory, group)
        updatePlaces(group)
    }

    private fun updateTools(accessibleTerritory: Territory, group: Group) {
        for ((key, value) in dependencies) {
            var currentAmount = value.amount
            if (currentAmount >= population)
                continue
            if (!key.isInstrumental())
                continue

            val evaluator = tagEvaluator(key)
            //TODO choose the best
            if (currentAmount >= population)
                break
            val deps: Set<Dependency>? = aspect.dependencies.map[key]
            if (deps != null)
                for (dependency in deps) {
                    val result = dependency.useDependency(
                            AspectController(
                                    1,
                                    population - currentAmount,
                                    1,
                                    evaluator,
                                    group.populationCenter,
                                    accessibleTerritory,
                                    false,
                                    group,
                                    group.cultureCenter.meaning
                            ))
                    if (result.isFinished) {
                        currentAmount += (evaluator.evaluate(result.resources)).toInt()
                        value.addAll(evaluator.pick(result.resources)) //TODO disband
                        if (currentAmount >= population)
                            break
                    }
                }
        }
    }

    private fun updatePlaces(group: Group) {
        if (!group.territoryCenter.settled)
            return

        val request: Request = AspectImprovementRequest(
                group,
                aspect,
                1,
                1,
                passingReward,
                passingReward
        )
        val (pack, usedAspects) = group.populationCenter.executeRequest(request)
        usedAspects.forEach { it.gainUsefulness(session.stratumTurnsBeforeInstrumentRenewal * 2) }
        enhancements.addAll(pack.getResources { it.genome.isMovable }
        )
        pack.getResources { !it.genome.isMovable }.resources
                .forEach { addUnmovableEnhancement(it, group) }
    }

    private fun addUnmovableEnhancement(resource: Resource, group: Group) {
        val goodPlaces = innerPlaces.filter { resource.genome.isAcceptable(it.tile) }
        var place: StaticPlace? = null

        if (goodPlaces.isEmpty()) {
            val goodTiles = group.territoryCenter.territory
                    .getTiles { resource.genome.isAcceptable(it) }
            if (goodTiles.isNotEmpty()) {
                val tagType = "(Stratum ${aspect.name} of ${group.name})"
                place = StaticPlace(
                        randomElement(goodTiles, session.random),
                        TileTag(tagType + "_" + innerPlaces.size, tagType)
                )
                innerPlaces.add(place)
            }
        } else
            place = randomElement(innerPlaces, session.random)

        if (place == null) return

        place.addResource(resource)
    }

    override fun finishUpdate(group: Group) {
        popularMemes.forEach { group.cultureCenter.memePool.strengthenMeme(it) }
        popularMemes.clear()

        if (workedAmount < population && !ego.isActive)
            population = workedAmount

        workedAmount = 0
        _effectiveness = -1.0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val stratum = other as AspectStratum
        return aspect == stratum.aspect
    }

    override fun hashCode() = Objects.hash(aspect)

    override fun toString() = "Stratum with population $population, " +
            "effectiveness $effectiveness, importance $importance, ${aspect.name}, Places: " +
            innerPlaces.joinToString { "$it " } +
            super.toString()

    override fun die() {
        population = 0
        workedAmount = 0
        innerPlaces.forEach { session.world.strayPlacesManager.addPlace(it) }
    }
}