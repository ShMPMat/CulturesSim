package simulation.space.resource.dependency;

import simulation.space.Tile;
import simulation.space.resource.Resource;

public class TemperatureMin extends Temperature {
    public TemperatureMin(int threshold, double deprivationCoefficient) {
        super(threshold, deprivationCoefficient);
    }

    @Override
    public double satisfaction(Tile tile, Resource resource) {
        if (tile == null) {
            throw new RuntimeException();
        }
        double result = threshold -  tile.getTemperature();
        result = 1 / Math.sqrt(Math.max(0, result) + 1);
        return result;
    }

    @Override
    public boolean hasNeeded(Tile tile) {
        return tile.getTemperature() <= threshold;
    }
}
