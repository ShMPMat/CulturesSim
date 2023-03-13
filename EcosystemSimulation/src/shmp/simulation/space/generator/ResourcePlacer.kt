package shmp.simulation.space.generator

import shmp.random.randomTile
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.space.WorldMap
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.ResourceType
import shmp.simulation.space.resource.container.ResourcePool
import shmp.simulation.space.resource.dependency.LabelerDependency
import shmp.simulation.space.tile.Tile
import kotlin.random.Random


class ResourcePlacer(
        val map: WorldMap,
        val resourcePool: ResourcePool,
        val supplement: MapGeneratorSupplement,
        val random: Random
) {
    fun placeResources() {
        val startTime = System.nanoTime()
        println("Start spread")
        val validTypes = listOf(ResourceType.Mineral, ResourceType.Animal, ResourceType.Plant)
        val resourcesToScatter = resourcePool.getAll {
            it.genome.spreadProbability != 0.0 || it.genome.type in validTypes
        }

        for (resource in resourcesToScatter)
            scatter(
                    resource,
                    random.nextInt(supplement.startResourceAmountRange.first, supplement.startResourceAmountRange.last)
            )
        println("End spread")
        println((System.nanoTime() - startTime).toDouble() / 1000 / 1000 / 1000)
    }

    private fun scatter(resource: Resource, n: Int) {
        val idealTiles = map.getTiles { resource.areNecessaryDependenciesSatisfied(it) }
        val goodTiles = map.getTiles { resource.isAcceptable(it) }

        val dependencyResources = computeDependencies(resource)

        for (i in 0 until n) {
            val tile: Tile = idealTiles.randomElementOrNull()
                    ?: goodTiles.randomElementOrNull()
                    ?: randomTile(map)

            tile.addDelayedResource(resource.copy())
            addDependencies(dependencyResources, tile)
        }
    }

    private fun computeDependencies(resource: Resource): List<Resource> {
        val resourcesQueue = mutableListOf(resource)
        val dependencyResources = mutableListOf<Resource>()
        val nextResources = mutableListOf<Resource>()

        while (resourcesQueue.isNotEmpty()) {
            for (res in resourcesQueue) {
                for (dependency in res.genome.dependencies) {
                    if (!dependency.isPositive || !dependency.isResourceNeeded)
                        continue

                    if (dependency is LabelerDependency)
                        nextResources += resourcePool
                                .getAll { dependency.isResourceDependency(it.largeSample) }
                                .filter { filterDependencyResources(it, dependencyResources, resource) }
                }
            }
            resourcesQueue.clear()
            resourcesQueue += nextResources
            dependencyResources += nextResources
            nextResources.clear()
        }

        return dependencyResources.reversed()
    }

    private fun addDependencies(resources: List<Resource>, tile: Tile) {
        for (dependencyResource in resources)
            if (dependencyResource.areNecessaryDependenciesSatisfied(tile))
                tile.addDelayedResource(dependencyResource)
    }

    private fun filterDependencyResources(resource: Resource, previous: List<Resource>, rootResource: Resource? = null) =
            resource.genome.type in listOf(ResourceType.Plant, ResourceType.Animal)
                    && previous.none { resource.simpleName == it.simpleName }
                    && resource.simpleName != rootResource?.simpleName
                    && resource.genome.primaryMaterial != null

}
