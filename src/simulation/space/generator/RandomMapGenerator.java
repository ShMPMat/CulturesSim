package simulation.space.generator;

import extra.SpaceProbabilityFuncs;
import kotlin.collections.ArraysKt;
import kotlin.random.Random;
import simulation.space.*;
import simulation.space.resource.Genome;
import simulation.space.resource.Resource;
import simulation.space.resource.dependency.ResourceDependency;
import simulation.space.resource.dependency.ResourceDependencyDep;

import java.util.*;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.*;
import static shmp.random.RandomSpaceKt.randomTile;

/**
 * Basic map generator.
 */
public class RandomMapGenerator {
    public static WorldMap generateMap(int x, int y, int platesAmount, Random random) {
        List<List<Tile>> tiles = createTiles(x, y);
        WorldMap map = new WorldMap(tiles);
        setTileNeighbours(map);
        List<TectonicPlate> tectonicPlates = randomPlates(
                platesAmount,
                map,
                random
        );
        tectonicPlates.forEach(map::addPlate);
        fill(map);
        return map;
    }

    public static void fillResources(
            WorldMap map,
            ResourcePool resourcePool,
            MapGeneratorSupplement supplement,
            Random random
    ) {
        for (Resource resource : resourcePool.getResourcesWithPredicate(r ->
                r.getSpreadProbability() != 0 || r.getGenome().getType().equals(Genome.Type.Mineral))) {
            scatter(
                    map,
                    resourcePool,
                    resource,
                    random.nextInt(
                            supplement.getStartResourceAmountRange().getStart(),
                            supplement.getStartResourceAmountRange().getEndInclusive()
                    ),
                    random
            );
        }
    }

    private static void setTileNeighbours(WorldMap map) {
        int x = map.getX();
        int y = map.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                map.get(i, j).setNeighbours(
                        Arrays.stream(new Tile[]{
                                map.get(i, j + 1),
                                map.get(i, j - 1),
                                map.get(i + 1, j),
                                map.get(i - 1, j)
                        })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
            }
        }
    }

    private static List<List<Tile>> createTiles(int x, int y) {
        List<List<Tile>> map = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            map.add(new ArrayList<>());
            for (int j = 0; j < y; j++) {
                map.get(i).add(new Tile(i, j));
            }
        }
        return map;
    }

    private static List<TectonicPlate> randomPlates(int platesAmount, WorldMap map, Random random) {
        List<TectonicPlate> tectonicPlates = new ArrayList<>();
        Set<Tile> usedTiles = new HashSet<>();
        for (int i = 0; i < platesAmount; i++) {
            TectonicPlate.Direction direction = randomElement(
                    ArraysKt.toList(TectonicPlate.Direction.values()),
                    random
            );
            TectonicPlate.Type type = randomElement(
                    ArraysKt.toList(TectonicPlate.Type.values()),
                    random
            );
            TectonicPlate tectonicPlate = new TectonicPlate(direction, type);
            Tile tile = randomTile(map, random);
            tectonicPlate.add(tile);
            tectonicPlates.add(tectonicPlate);
            usedTiles.add(tile);
        }
        boolean sw = true;
        while (sw) {
            sw = false;
            for (Territory territory : tectonicPlates) {
                List<Tile> brink = territory.getBrinkWithCondition(t -> !usedTiles.contains(t));
                if (brink.isEmpty()) {
                    continue;
                }
                Tile tile = randomElement(brink, random);
                territory.add(tile);
                usedTiles.add(tile);
                sw = true;
            }
        }
        return tectonicPlates;
    }

    private static void fill(WorldMap map) {
        boolean sw = true, ssw = true;
        for (TectonicPlate plate : map.getTectonicPlates()) {
            if (sw) {
                plate.setType(TectonicPlate.Type.Terrain);
                sw = false;
            } else if (ssw) {
                plate.setType(TectonicPlate.Type.Oceanic);
                ssw = false;
            }
            plate.initialize();
        }
        map.platesUpdate();
    }

    private static void scatter(WorldMap map, ResourcePool resourcePool, Resource resource, int n, Random random) {
        List<Tile> goodTiles = map.getTilesWithPredicate(t -> resource.getGenome().isOptimal(t));
        for (int i = 0; i < n; i++) {
            Tile tile;
            if (goodTiles.isEmpty()) {
                tile = randomTile(map, random);
                while (!resource.getGenome().isAcceptable(tile)) {
                    tile = randomTile(map, random);
                }
            } else {
                tile = randomElement(goodTiles, random);
            }
            tile.addDelayedResource(resource.copy());
            addDependencies(resource, tile, resourcePool);
        }
    }

    private static void addDependencies(Resource resource, Tile tile, ResourcePool resourcePool) {
        for (ResourceDependency dependency : resource.getGenome().getDependencies()) {
            if (!dependency.isPositive() || !dependency.isResourceNeeded()) {
                continue;
            }
            if (dependency instanceof ResourceDependencyDep && ((ResourceDependencyDep) dependency).getResourceNames()
                    .stream().anyMatch(s -> s.equals("Vapour"))) {
                return;
            }
            if (dependency instanceof ResourceDependencyDep) {
                for (String name : ((ResourceDependencyDep) dependency).getResourceNames()) {
                    Resource dep = resourcePool.getResource(name);
                    if (dep.getGenome().isAcceptable(tile)) {
                        tile.addDelayedResource(dep);
                    }
                    addDependencies(dep, tile, resourcePool);
                }
                for (String name : ((ResourceDependencyDep) dependency).getMaterialNames()) {
                    for (Resource dep : resourcePool.getResourcesWithPredicate(r ->
                            r.getSpreadProbability() > 0
                                    && !r.getSimpleName().equals(resource.getSimpleName())
                                    && r.getGenome().getPrimaryMaterial() != null
                                    && r.getGenome().getPrimaryMaterial().getName().equals(name))) {
                        if (dep.getGenome().isAcceptable(tile)) {
                            tile.addDelayedResource(dep.copy());
                            addDependencies(dep, tile, resourcePool);
                        }
                    }
                }
            }
        }
    }
}
