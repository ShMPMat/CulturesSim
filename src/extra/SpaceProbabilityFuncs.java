package extra;

import simulation.space.Territory;
import simulation.space.tile.Tile;
import simulation.space.WorldMap;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static shmp.random.RandomCollectionsKt.*;
import static simulation.Controller.*;

public class SpaceProbabilityFuncs {
    /**
     *
     * @param tiles any set of tiles.
     * @param predicate predicate which tiles on brink will satisfy.
     * @return random Tile on brink of tiles, which satisfies predicate.
     * If such Tile does not exists, returns null.
     */
    public static Tile randomTileOnBrink(Collection<Tile> tiles, Predicate<Tile> predicate) {
        List<Tile> brink = new Territory(tiles).getOuterBrink(predicate);
        return brink.isEmpty()
                ? null
                : randomElement(brink, session.random);
    }

    /**
     * @param territory A Territory from which a random Tile will be chosen.
     * @return A random Tile from the Territory.
     */
    public static Tile randomTile(Territory territory) {
        return randomElement(territory.getTiles(), session.random);
    }

    public static Tile randomTile(WorldMap map) {
        return randomElement(randomElement(map.getLinedTiles(), session.random), session.random);
    }
}
