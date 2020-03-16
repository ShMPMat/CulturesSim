package simulation.culture.group;

import extra.SpaceProbabilityFuncs;
import simulation.Controller;
import simulation.Event;
import simulation.space.Territory;
import simulation.space.Tile;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static shmp.random.RandomProbabilitiesKt.*;

public class TerritoryCenter {
    private Territory territory = new Territory();
    private GroupTileTag tileTag;
    private Function<Tile, Integer> tilePotentialMapper = t ->
            t.getNeighbours(tile1 -> tile1.getTagPool().contains(tileTag)).size()
                    + 3 * t.hasResources(tileTag.getGroup().getCultureCenter().getAspectCenter().getAspectPool().getResourceRequirements())
                    + tileTag.getGroup().getRelationCenter().evaluateTile(t);
    private double spreadAbility;

    TerritoryCenter(Group group, double spreadAbility, Tile tile) {
        this.tileTag = new GroupTileTag(group);
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
        Set<Group> groups = territory.getOuterBrink().stream()
                .flatMap(t -> t.getTagPool().getByType(tileTag.getType()).stream())
                .map(t -> ((GroupTileTag) t).getGroup())
                .collect(Collectors.toSet());
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
        return getTerritory().getCenter()
                .getNeighbours(tile -> canSettle(tile, t -> t.getTagPool().getByType(tileTag.getType()).isEmpty()))
                .stream()
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
                t -> canSettle(t, t2 -> t2.getTagPool().getByType(tileTag.getType()).isEmpty() && isTileReachable(t2)),
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
        if (!tile.getTagPool().contains(tileTag) && !tile.getTagPool().getByType(tileTag.getType()).isEmpty()) {
            throw new RuntimeException();
        }
        tileTag.getGroup().getParentGroup().claimTile(tile);
        tile.getTagPool().add(tileTag);
        getTerritory().add(tile);
        tileTag.getGroup().addEvent(new Event(Event.Type.TileAcquisition, "Group " + tileTag.getGroup().name + " claimed tile " + tile.x + " " +
                tile.y, "group", this, "tile", tile));
    }

    void die() {
        for (Tile tile : getTerritory().getTiles()) {
            tile.getTagPool().remove(tileTag);
        }
    }

    private void leaveTile(Tile tile) {
        if (tile == null) {
            return;
        }
        tile.getTagPool().remove(tileTag);
        getTerritory().removeTile(tile);
        tileTag.getGroup().getParentGroup().removeTile(tile);
    }

    private void leaveTiles(Collection<Tile> tiles) {
        tiles.forEach(this::leaveTile);
    }

    public boolean canSettle(Tile tile) {
        return tile.getType() != Tile.Type.Water && tile.getType() != Tile.Type.Mountain
                || (tile.getType() == Tile.Type.Mountain
                && !tileTag.getGroup().getCultureCenter().getAspectCenter().getAspectPool().filter(a ->
                a.getTags().stream()
                        .anyMatch(aspectTag -> aspectTag.name.equals("mountainLiving")))//TODO set of accessible tiles
                .isEmpty());
    }

    public boolean canSettle(Tile tile, Predicate<Tile> additionalCondition) {
        return canSettle(tile) && additionalCondition.test(tile);
    }
}
