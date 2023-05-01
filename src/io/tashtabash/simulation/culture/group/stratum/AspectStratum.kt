package io.tashtabash.simulation.culture.group.stratum

import io.tashtabash.random.singleton.chanceOfNot
import io.tashtabash.simulation.CulturesController.Companion.session
import io.tashtabash.simulation.culture.aspect.*
import io.tashtabash.simulation.culture.aspect.dependency.Dependency
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.passingReward
import io.tashtabash.simulation.culture.group.request.*
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.simulation.culture.thinking.meaning.makeMeme
import io.tashtabash.simulation.space.territory.Territory
import io.tashtabash.simulation.space.resource.container.MutableResourcePack
import io.tashtabash.simulation.space.resource.tag.ResourceTag
import io.tashtabash.simulation.space.tile.Tile
import io.tashtabash.utils.SoftValue
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


class AspectStratum(
        override var population: Int,
        val aspect: ConverseWrapper,
        tile: Tile
) : BaseStratum(tile, "Stratum of aspect ${aspect.name}", "") {
    private var _effectiveness = -1.0
    private var workedAmount = 0
    private var isRaisedAmount = false
    private var workedStraight = 0
    private val dependencies: MutableMap<ResourceTag, MutableResourcePack> = HashMap()
    private val popularMemes: MutableList<Meme> = ArrayList()
    override var importance: Int
        get() = aspect.usefulness
        set(value) {
            aspect.gainUsefulness(value - importance)
        }

    fun getInstrumentByTag(tag: ResourceTag) = dependencies[tag] ?: MutableResourcePack()

    override val freePopulation: Int
        get() = population - workedAmount
    override val cumulativeWorkAblePopulation: Double
        get() = freePopulation * effectiveness

    init {
        aspect.dependencies.map.keys.forEach { tag: ResourceTag ->
            if (tag.isInstrumental && tag.name != "phony")
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
                _effectiveness = 1.0 +
                        places.flatMap { it.owned.resources }.sumOf { it.getAspectImprovement(aspect) }
            }
            return _effectiveness
        }

    override fun useAmount(amount: Int, maxOverhead: Int): StratumPeople {
        if (amount <= 0)
            return StratumPeople(0, this)

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

        return StratumPeople(actualAmount, this, effectiveness)
    }

    override fun update(accessibleResources: MutableResourcePack, accessibleTerritory: Territory, group: Group) {
        if (population == 0)
            return

        super.update(accessibleResources, accessibleTerritory, group)

        if (aspect !is MeaningInserter) {
            val oldPopulation = population
            val evaluator = passingEvaluator
            val decreaseCoefficient = SoftValue(workedStraight).value.pow(2)
            val amountToRise = min(
                    freePopulation,
                    (freePopulation * decreaseCoefficient).toInt() + 1
            )
            if (amountToRise > 0) {
                val amount = aspect.calculateProducedValue(evaluator, amountToRise)
                val pack = use(AspectController(
                        1,
                        amount,
                        amount,
                        evaluator,
                        group.populationCenter,
                        accessibleTerritory,
                        false,
                        group,
                        setOf(RequestType.Improvement)
                ))

                if (population < oldPopulation)
                    population = oldPopulation
                accessibleResources.addAll(pack)
            }
        }

        updateInfrastructure(accessibleTerritory, group)
        isRaisedAmount = false
        ego.update(accessibleResources, accessibleTerritory, group, this)
    }

    fun use(controller: AspectController): MutableResourcePack {
        val resourcePack = MutableResourcePack()
        val result = aspect.use(controller)

        if (result.resources.isNotEmpty) {
            popularMemes.add(makeMeme(aspect))
            result.resources.resources.forEach { popularMemes.add(makeMeme(it)) }
        }
        if (result.isFinished)
            resourcePack.addAll(result.resources)
        result.pushNeeds(controller.group)

        return resourcePack
    }

    private fun updateInfrastructure(accessibleTerritory: Territory, group: Group) {
        session.stratumInstrumentRenewalProb.chanceOfNot {
            return
        }

        if (population == 0)
            return

        updateTools(accessibleTerritory, group)
        updatePlaces(group)
    }

    private fun updateTools(accessibleTerritory: Territory, group: Group) {
        for ((key, value) in dependencies) {
            var currentAmount = value.amount
            if (currentAmount >= population)
                continue
            if (!key.isInstrumental)
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
                                    (population - currentAmount).toDouble(),
                                    1.0,
                                    evaluator,
                                    group.populationCenter,
                                    accessibleTerritory,
                                    false,
                                    group,
                                    setOf(RequestType.Improvement)
                            ))
                    if (result.isFinished) {
                        currentAmount += (evaluator.evaluatePack(result.resources)).toInt()
                        value.addAll(evaluator.pickAndRemove(result.resources))
                        group.populationCenter.turnResources.addAll(result.resources)

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
                aspect,
                RequestCore(
                        group,
                        0.5,
                        0.5 * max(1, importance),
                        passingReward,
                        passingReward,
                        30,
                        setOf(RequestType.Improvement)
                )
        )
        val (pack, usedAspects) = group.populationCenter.executeRequest(request)

        usedAspects.forEach {
            it.gainUsefulness((2 / session.stratumInstrumentRenewalProb).toInt())
        }
        pack.resources.forEach { addEnhancement(it, group) }
    }

    override fun finishUpdate(group: Group) {
        popularMemes.forEach { group.cultureCenter.memePool.strengthenMeme(it) }
        popularMemes.clear()

        if (workedAmount > 0) {
            workedStraight++
        }

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
