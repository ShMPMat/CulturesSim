package io.tashtabash.simulation.space.resource.dependency;

import io.tashtabash.simulation.space.resource.Resource;
import io.tashtabash.simulation.space.tile.Tile;


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
