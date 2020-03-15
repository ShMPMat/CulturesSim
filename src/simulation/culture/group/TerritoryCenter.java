package simulation.culture.group;

import extra.SpaceProbabilityFuncs;
import simulation.Controller;
import simulation.Event;
import simulation.space.Territory;
import simulation.space.Tile;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static shmp.random.RandomProbabilitiesKt.*;

public class TerritoryCenter {
    private Territory territory = new Territory();
    private Group group;
    private Function<Tile, Integer> tilePotentialMapper = t ->
            t.getNeighbours(tile1 -> this.group.equals(tile1.group)).size()
                    + 3 * t.hasResources(group.getCultureCenter().getAspectCenter().getAspectPool().getResourceRequirements())
                    + group.getRelationCenter().evaluateTile(t);
    private double spreadAbility;

    TerritoryCenter(Group group, double spreadAbility, Tile tile) {
        this.group = group;
        this.spreadAbility = spreadAbility;
        claimTile(tile);
    }

    public Territory getTerritory() {
        return territory;
    }

    public double getSpreadAbility() {
        return spreadAbility;
    }

    public Set<Group> getAllNearGroups(Group exception) {
        Set<Group> groups = new HashSet<>();
        for (Tile tile : getTerritory().getTiles()) {
            tile.getNeighbours(t -> t.group != null).forEach(t -> groups.add(t.group));
        }
        groups.remove(exception);
        return groups;
    }

    boolean migrate() {
        Tile newCenter = getMigrationTile();
        if (newCenter == null) {
            return false;
        }
        getTerritory().setCenter(newCenter);
        claimTile(newCenter);//TODO move claim and leave here
        leaveTiles(getTerritory().getTiles(tile -> !isTileReachable(tile)));
        return true;
    }

    private Tile getMigrationTile() {
        return getTerritory().getCenter().getNeighbours(t -> canSettle(t, tile -> tile.group == null)).stream()
                .max(Comparator.comparingInt(tile -> tilePotentialMapper.apply(tile)))
                .orElse(null);
    }

    public Tile getDisbandTile() {
        return SpaceProbabilityFuncs.randomTile(getTerritory());
    }

    boolean expand() {
        if (!testProbability(spreadAbility, Controller.session.random)) {
            return false;
        }
        claimTile(territory.getMostUsefulTileOnOuterBrink(
                t -> canSettle(t, t2 -> t2.group == null && isTileReachable(t2)),
                tilePotentialMapper
        ));
        return true;
    }

    void shrink() {
        if (territory.size() <= 1) {
            return;
        }
        leaveTile(territory.getMostUselessTile(tilePotentialMapper));
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
        tile.group = null;
        getTerritory().removeTile(tile);
        group.getParentGroup().removeTile(tile);
    }

    private void leaveTiles(Collection<Tile> tiles) {
        tiles.forEach(this::leaveTile);
    }

    public boolean canSettle(Tile tile) {
        return tile.getType() != Tile.Type.Water && tile.getType() != Tile.Type.Mountain
                || (tile.getType() == Tile.Type.Mountain
                && !group.getCultureCenter().getAspectCenter().getAspectPool().filter(a ->
                a.getTags().stream()
                        .anyMatch(aspectTag -> aspectTag.name.equals("mountainLiving")))
                .isEmpty());
    }

    public boolean canSettle(Tile tile, Predicate<Tile> additionalCondition) {
        return canSettle(tile) && additionalCondition.test(tile);
    }
}
