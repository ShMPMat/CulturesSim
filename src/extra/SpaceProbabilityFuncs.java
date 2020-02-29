package extra;

import simulation.space.Territory;
import simulation.space.Tile;
import simulation.space.WorldMap;

import java.util.Collection;
import java.util.function.Predicate;

public class SpaceProbabilityFuncs {
    /**
     *
     * @param tiles any set of tiles.
     * @param predicate predicate which tiles on brink will satisfy.
     * @return random Tile on brink of tiles, which satisfies predicate.
     * If such Tile does not exists, returns null.
     */
    public static Tile randomTileOnBrink(Collection<Tile> tiles, Predicate<Tile> predicate) {
        return ProbabilityFuncs.randomElement(new Territory(tiles).getBrinkWithCondition(predicate));
    }

    /**
     * @param territory A Territory from which a random Tile will be chosen.
     * @return A random Tile from the Territory.
     */
    public static Tile randomTile(Territory territory) {
        return ProbabilityFuncs.randomElement(territory.getTiles());
    }

    public static Tile randomTile(WorldMap map) {
        return ProbabilityFuncs.randomElement(ProbabilityFuncs.randomElement(map.map));
    }
}
