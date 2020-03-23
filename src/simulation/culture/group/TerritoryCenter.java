package simulation.culture.group;

import extra.SpaceProbabilityFuncs;
import simulation.Controller;
import simulation.Event;
import simulation.space.Territory;
import simulation.space.tile.Tile;
import simulation.space.tile.TileDistanceKt;

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
                    + 3 * t.getResourcePack().getAmount(r ->
                    tileTag.getGroup().getCultureCenter().getAspectCenter().getAspectPool().getResourceRequirements()
                            .contains(r)
            )
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

    public Function<Tile, Integer> getTilePotentialMapper() {
        return tilePotentialMapper;
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
        return getMigrationTiles()
                .stream()
                .max(Comparator.comparingInt(tile -> tilePotentialMapper.apply(tile)))
                .orElse(null);
    }

    private Collection<Tile> getMigrationTiles() {
        Set<Tile> tiles = new HashSet<>();
        Set<Tile> queue = new HashSet<>();
        queue.add(territory.getCenter());
        while (true) {
            tiles.addAll(queue);
            List<Tile> currentTiles = queue.stream()
                    .flatMap(t -> t.getNeighbours().stream())
                    .distinct()
                    .filter(t -> !tiles.contains(t))
                    .filter(this::isTileReachable)
                    .filter(this::canTraverse)
                    .collect(Collectors.toList());
            if (currentTiles.isEmpty()) {
                break;
            }
            queue.clear();
            queue.addAll(currentTiles);
        }
        return tiles.stream()
                .filter(this::canSettleAndNoGroup)
                .collect(Collectors.toList());
    }

    private boolean canTraverse(Tile tile) {
        if (tile.getType() == Tile.Type.Water) {
            return false;
        } else if (tile.getType() == Tile.Type.Mountain) {
            if (!tileTag.getGroup().getCultureCenter().getAspectCenter().getAspectPool().contains("MountainLiving")) {
                return false;
            }
        }
        return true;
    }

    public Tile getDisbandTile() {
        return SpaceProbabilityFuncs.randomTile(getTerritory());
    }

    boolean expand() {
        if (!testProbability(spreadAbility, Controller.session.random)) {
            return false;
        }
        claimTile(territory.getMostUsefulTileOnOuterBrink(
                t -> canSettleAndNoGroup(t) && isTileReachable(t),
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
        return TileDistanceKt.getDistance(tile, getTerritory().getCenter()) < 4;
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
        tileTag.getGroup().addEvent(new Event(
                Event.Type.TileAcquisition,
                "Group " + tileTag.getGroup().name + " claimed tile " + tile.getX() + " " + tile.getY(),
                "group", this, "tile", tile
        ));
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

    boolean canSettleAndNoGroup(Tile tile) {
        return canSettle(tile, t -> t.getTagPool().getByType(tileTag.getType()).isEmpty());
    }

    public boolean canSettle(Tile tile, Predicate<Tile> additionalCondition) {
        return canSettle(tile) && additionalCondition.test(tile);
    }
}
