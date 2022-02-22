package shmp.simulation.space.resource.dependency;

import shmp.simulation.space.tile.Tile;
import shmp.simulation.space.resource.Resource;


public class TemperatureMin extends Temperature {
    public TemperatureMin(int threshold, double deprivationCoefficient) {
        super(threshold, deprivationCoefficient);
    }

    @Override
    public double satisfaction(Tile tile, Resource resource) {
        double result = threshold - tile.getTemperature();
        result = 1 / Math.sqrt(Math.max(0, result) + 1);
        return result;
    }

    @Override
    public boolean hasNeeded(Tile tile) {
        return tile.getTemperature() <= threshold;
    }
}
