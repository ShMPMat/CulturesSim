package simulation.culture.group;

import extra.SpaceProbabilityFuncs;
import simulation.Controller;
import simulation.culture.Event;
import simulation.culture.group.intergroup.Relation;
import simulation.space.Territory;
import simulation.space.Tile;

import java.util.*;
import java.util.function.Function;

import static shmp.random.RandomProbabilitiesKt.*;

public class TerritoryCenter {
    private Territory territory = new Territory();
    private Group group;
    private Function<Tile, Integer> tileRelationAspectMapper = t -> {//TODO move functions somewhere else
        int accumulator = 0;
        for (Relation relation : group.getCulturalCenter().relations.values()) {
            if (relation.getPositive() < 0) {
                accumulator += relation.getPositive() * 10000 / relation.other.getTerritoryCenter().getTerritory().getCenter().getDistance(t);
            }
        }
        return accumulator;
    };
    private Function<Tile, Integer> tilePotentialMapper = t -> t.getNeighbours(tile1 -> this.group.equals(tile1.group)).size() +
            3 * t.hasResources(group.getCulturalCenter().getAspectCenter().getAspectPool().getResourceRequirements())
            + tileRelationAspectMapper.apply(t);

    TerritoryCenter(Group group) {
        this.group = group;
    }

    public Territory getTerritory() {
        return territory;
    }

    public Set<Group> getAllNearGroups() {
        Set<Group> groups = new HashSet<>();
        for (Tile tile : getTerritory().getTiles()) {
            tile.getNeighbours(t -> t.group != null).forEach(t -> groups.add(t.group));
        }
        groups.remove(group);
        return groups;
    }

    void update() {
        if (group.state == Group.State.Dead) {
            return;
        }
        migrate();
        expand();
    }

    private boolean migrate() {
        if (!shouldMigrate()) {
            return false;
        }

        Tile newCenter = getMigrationTile();
        if (newCenter == null) {
            return false;
        }
        getTerritory().setCenter(newCenter);
        claimTile(newCenter);//TODO move claim and leave here
        leaveTiles(getTerritory().getTilesWithPredicate(tile -> !isTileReachable(tile)));
        return true;
    }

    private boolean shouldMigrate() {
        return !group.getCulturalCenter().getAspirations().isEmpty();
    }

    private Tile getMigrationTile() {
        return getTerritory().getCenter().getNeighbours(tile -> tile.canSettle(group) && tile.group == null).stream()
                .max(Comparator.comparingInt(tile -> tilePotentialMapper.apply(tile))).orElse(null);
    }

    public Tile getDisbandTile() {
        return SpaceProbabilityFuncs.randomTile(getTerritory());
    }

    private boolean expand() {
        if (!testProbability(group.getSpreadability(), Controller.session.random)) {
            return false;
        }
        if (group.population <= group.getMinPopulationPerTile() * territory.size()) {
            group.getParentGroup().getTerritory().removeTile(territory.excludeMostUselessTileExcept(new ArrayList<>(), tilePotentialMapper));
            if (group.population <= group.getMinPopulationPerTile() * territory.size()) {
                group.getParentGroup().getTerritory().removeTile(territory.excludeMostUselessTileExcept(new ArrayList<>(), tilePotentialMapper));
            }
        }

        claimTile(territory.getMostUsefulTile(newTile -> newTile.group == null && newTile.canSettle(group) &&
                isTileReachable(newTile), tilePotentialMapper));
        return true;
    }

    private boolean isTileReachable(Tile tile) {
        return getTerritory().getCenter().getDistance(tile) < 4;
    }

    void claimTile(Tile tile) {
        if (tile == null) {
            return;
        }
        if (tile.group != group && tile.group != null) {
            throw new RuntimeException();
        }
        group.getParentGroup().claimTile(tile);
        tile.group = group;
        getTerritory().add(tile);
        group.addEvent(new Event(Event.Type.TileAcquisition, "Group " + group.name + " claimed tile " + tile.x + " " +
                tile.y, "group", this, "tile", tile));
    }

    void die() {
        for (Tile tile : getTerritory().getTiles()) {
            tile.group = null;
        }
    }

    private void leaveTile(Tile tile) {
        if (tile == null) {
            return;
        }
        getTerritory().removeTile(tile);
        group.getParentGroup().removeTile(tile);
    }

    private void leaveTiles(Collection<Tile> tiles) {
        tiles.forEach(this::leaveTile);
    }
}
