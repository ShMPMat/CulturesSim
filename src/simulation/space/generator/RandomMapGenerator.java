package simulation.space.generator;

import extra.ProbFunc;
import simulation.World;
import simulation.culture.aspect.AspectTag;
import simulation.space.resource.Resource;
import simulation.space.Tile;
import simulation.space.WorldMap;
import simulation.space.resource.ResourceIdeal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic map generator.
 */
public class RandomMapGenerator {
    public static List<AspectTag> aspectTagPool;

    public static void createResources(World world, int numberOrResources) {
        fillAspectTagPool();
        fillResourcePool(world);
        //fillResourcePoolWithRandomResources(world, numberOrResources);//TODO off for debug
        world.resourcePool.forEach(Resource::actualizeLinks);
    }

    public static void fill(WorldMap map) {
        createBlob(map, Tile.Type.Mountain, 10);
        createBlob(map, Tile.Type.Water, 10);
        createBlob(map, Tile.Type.Mountain, 10);
        createBlob(map, Tile.Type.Water, 10);
        createBlob(map, Tile.Type.Mountain, 10);
        createBlob(map, Tile.Type.Water, 10);
        createBlob(map, Tile.Type.Mountain, 10);
        createBlob(map, Tile.Type.Water, 10);
        RandomMapGenerator.fillResources(map);
    }

    private static void fillResourcePool(World world) {
        world.resourcePool = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("SupplementFiles/Resources"))) {
            String line;
            String[] tags;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                tags = line.split("\\s+");
                world.resourcePool.add(new ResourceIdeal(tags, 1, world));
            }
        } catch (Throwable t) {
            System.err.println(t.toString());
        }
    }

    private static void fillResourcePoolWithRandomResources(World world, int numberOrResources) {
        for (int i = 0; i < numberOrResources; i++) {
            List<String> tags = new ArrayList<>();
            tags.add("r" + i);
            tags.add(((Double) (Math.random() / 100)).toString());
            int n = ProbFunc.randomInt(aspectTagPool.size());
            for (int j = 0; j < n; j++) {
                while (true) {
                    String name = aspectTagPool.get(ProbFunc.randomInt(aspectTagPool.size())).name;
                    if (!tags.contains("-" + name)) {
                        tags.add("-" + name);
                        break;
                    }
                }
            }
            world.resourcePool.add(new ResourceIdeal(tags.toArray(new String[n + 2]),
                    1 + ProbFunc.randomInt(5), world));
        }
    }

    private static void fillAspectTagPool() {
        aspectTagPool = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("SupplementFiles/ResourceTags"))) {
            String line;
            String[] tags;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                aspectTagPool.add(new AspectTag(line));
            }
        } catch (Throwable t) {
            System.err.println(t.toString());
        }
    }

    private static void fillResources(WorldMap map) {
        for (Resource resource : map.resourcePool) {
            if (resource.getSpreadAbility() == 0 && !resource.getName().matches("Clay") &&
                    !resource.getName().matches("Stone")) {
                continue;
            }
            scatter(map, resource, 40 + ProbFunc.randomInt(30));
        }
    }

    private static void createBlob(WorldMap map, Tile.Type type, int n) {
        List<Tile> tiles = new ArrayList<>();
        tiles.add(ProbFunc.randomTile(map));
        tiles.get(0).type = type;
        for (int i = 0; i < n; i++) {
            Tile tile = ProbFunc.randomTileOnBrink(tiles, t -> true);
            if (tile != null) {
                tile.type = type;
                tiles.add(tile);
            } else {
                i+=0;
            }
        }
    }

    private static void scatter(WorldMap map, Resource resource, int n) {
        for (int i = 0; i < n; i++) {
            Tile tile = ProbFunc.randomTile(map);
            if (!tile.getResources().contains(resource)) {
                if (resource.isMovable()) {
                    tile.addResource(resource.copy());
                } else {
                    tile.addDelayedResource(resource.copy());
                }
            }
        }
    }
}
