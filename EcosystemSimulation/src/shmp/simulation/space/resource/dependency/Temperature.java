package shmp.simulation.space.resource.dependency;


public abstract class Temperature extends CoefficientDependency {
    int threshold;

    public Temperature(int threshold, double deprivationCoefficient) {
        super(deprivationCoefficient);
        this.threshold = threshold;
    }

    @Override
    public boolean isNecessary() {
        return true;
    }

    @Override
    public boolean isPositive() {
        return true;
    }

    @Override
    public boolean isResourceNeeded() {
        return false;
    }
}
