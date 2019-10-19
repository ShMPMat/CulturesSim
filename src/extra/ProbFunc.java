package extra;

import simulation.culture.aspect.Aspect;
import simulation.culture.group.Group;
import simulation.space.Territory;
import simulation.space.WorldMap;
import simulation.space.Tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Functions related to random and probability.
 */
public class ProbFunc {
    /**
     * Returns boolean with Probability.
     * @param probability double from 0 to 1 inclusively.
     * @return true with Probability, otherwise returns false.
     */
    public static boolean getChances(double probability) {
        return Math.random() <= probability;
    }

    /**
     * Returns rendom integer lower then ceiling.
     * @param ceiling non-negative number.
     * @return random int from 0 to ceiling.
     */
    public static int randomInt(int ceiling) {
        int n = (int) (Math.random() * ceiling);
        return  (n == ceiling ? n - 1 : n);
    }

    public static <E> E randomElement(List<E> list) {
        return list.get(randomInt(list.size()));
    }

    public static <E> E randomElement(E[] array) {
        return array[randomInt(array.length)];
    }

    /**
     * Returns random tile from map.
     * @param map any non-empty map.
     * @return random Tile from map.
     */
    public static Tile randomTile(WorldMap map) {
        return randomElement(randomElement(map.map));
    }

    /**
     *
     * @param tiles any set of tiles.
     * @param predicate predicate which tiles on brink will satisfy.
     * @return random Tile on brink of tiles, which satisfies predicate.
     * If such Tile does not exists, returns null.
     */
    public static Tile randomTileOnBrink(Collection<Tile> tiles, Predicate<Tile> predicate) {
        List<Tile> goodTiles = new Territory(tiles).getBrinkWithCondition(predicate);
        if (goodTiles.size() == 0) {
            return null;
        }
        return randomElement(goodTiles);
    }

    public static Tile randomTile(Territory territory) {
        if (territory.isEmpty()) {
            return null;
        }
        return randomElement(territory.getTiles());
    }

    private static ShnyPair<Aspect, Group> getRandomAspectWithPairExcept(Collection<ShnyPair<Aspect, Group>> pool,
                                                                         Predicate<ShnyPair<Aspect, Group>> predicate) {
        List<ShnyPair<Aspect, Group>> pairs = new ArrayList<>();
        for (ShnyPair<Aspect, Group> pair : pool) {
            if (predicate.test(pair)) {
                pairs.add(pair);
            }
        }
        if (pairs.size() == 0) {
            return null;
        }
        return randomElement(pairs);
    }

    public static ShnyPair<Aspect, Group> addRandomAspectWithPairExcept(Collection<Aspect> target,
                                                                        Collection<ShnyPair<Aspect, Group>> pool,
                                                                        Predicate<ShnyPair<Aspect, Group>> predicate,
                                                                        double probability) {
        if (!getChances(probability)) {
            return null;
        }
        ShnyPair<Aspect, Group> pair = getRandomAspectWithPairExcept(pool, predicate);
        if (pair == null) {
            return null;
        }
        return pair;
    }

    public static Aspect getRandomAspectExcept(Collection<Aspect> pool, Predicate<Aspect> predicate) {
        List<Aspect> aspects = pool.stream().filter(predicate).collect(Collectors.toList());
        if (aspects.size() == 0) {
            return null;
        }
        return randomElement(aspects);
    }
}
