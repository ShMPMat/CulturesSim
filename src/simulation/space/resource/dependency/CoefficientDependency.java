package simulation.space.resource.dependency;

import simulation.space.tile.Tile;
import simulation.space.resource.Resource;

public abstract class CoefficientDependency implements ResourceDependency {
    private double deprivationCoefficient;

    public CoefficientDependency(double deprivationCoefficient) {
        this.deprivationCoefficient = deprivationCoefficient;
    }

    @Override
    public double satisfactionPercent(Tile tile, Resource resource) {
        double result = satisfaction(tile, resource);
        return result + (1 - result) / deprivationCoefficient;
    }

    abstract double satisfaction(Tile tile, Resource resource);
}
