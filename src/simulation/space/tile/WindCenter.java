package simulation.space.tile;

import kotlin.Pair;
import simulation.space.SpaceData;
import simulation.space.WorldMap;
import simulation.space.resource.Resource;

import java.util.List;

import static simulation.Controller.session;

public class WindCenter {
    private Wind wind;
    private Wind _newWind;

     WindCenter() {
        wind = new Wind();
    }

    Wind getWind() {
        return wind;
    }

    void startUpdate() {
        _newWind = new Wind();
    }

    void useWind(List<Resource> resources) {
        for (Resource resource : resources) {
            if (!resource.getGenome().isMovable()) {
                continue;
            }
            double overallWindLevel = wind.affectedTiles.stream()
                    .map(Pair::getSecond)
                    .reduce(Double::sum)
                    .orElse(0.0);
            for (Pair<Tile, Double> pair : wind.affectedTiles) {
                int part = (int) (resource.getAmount() * pair.getSecond() / overallWindLevel *
                        Math.min(pair.getSecond() * 0.0001 / resource.getGenome().getMass(), 1));
                if (part > 0) {
                    pair.getFirst().addDelayedResource(resource.getCleanPart(part));
                }
            }
        }
    }

    void middleUpdate(int x, int y) {
        WorldMap map = session.world.getMap();
        Tile host = map.get(x, y);
        host.getNeighbours().forEach(n -> setWindByTemperature(n, host));

        if (!_newWind.isStill())
            return;

        propagateWindStraight(map.get(x - 1, y), map.get(x + 1, y), host);
        propagateWindStraight(map.get(x + 1, y), map.get(x - 1, y), host);
        propagateWindStraight(map.get(x, y - 1), map.get(x, y + 1), host);
        propagateWindStraight(map.get(x, y + 1), map.get(x, y - 1), host);

        if (!_newWind.isStill()) {//TODO better to addAll wind for cross tiles than try to fetch it; cut wind on large level changes
            return;
        }

        propagateWindFillIn(map.get(x - 1, y), map.get(x - 2, y));
        propagateWindFillIn(map.get(x + 1, y), map.get(x + 2, y));
        propagateWindFillIn(map.get(x, y - 1), map.get(x, y - 2));
        propagateWindFillIn(map.get(x, y + 1), map.get(x, y + 2));
    }

    public void finishUpdate() {
        wind = _newWind;
    }

    private void setWindByTemperature(Tile tile, Tile master) {
        int temperatureChange = SpaceData.INSTANCE.getData().getTemperatureToWindCoefficient();
        if (tile.getLevel() + 2 < master.getLevel()) {
            temperatureChange *= 5;
        }
        if (tile != null) {
            double level = ((double) tile.getTemperature() - 1 - master.getTemperature()) / temperatureChange;
            if (level > 0) {
                _newWind.changeLevelOnTile(tile, level);
            }
        }
    }

    private void propagateWindStraight(Tile target, Tile tile, Tile master) {
        if (tile != null && target != null) {
            double level = tile.getWind().getPureLevelByTile(master) - SpaceData.INSTANCE.getData().getWindPropagation();
            if (level > 0) {
                _newWind.changeLevelOnTile(target, level);
            }
        }
    }

    private void propagateWindFillIn(Tile tile, Tile target) {
        if (tile != null && target != null) {
            double level = tile.getWind().getLevelByTile(target) - session.windFillIn;
            if (level > 0) {
                _newWind.isFilling = true;
                _newWind.changeLevelOnTile(tile, level);
            }
        }
    }
}
