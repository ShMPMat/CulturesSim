package simulation.space.resource.dependency;

import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.tile.Tile;
import simulation.space.resource.Resource;

public abstract class LabelerDependency extends CoefficientDependency {
    private ResourceLabeler goodResource;
    private boolean isNecessary;
    double amount;
    int currentAmount = 0;

    public LabelerDependency(
            double deprivationCoefficient,
            boolean isNecessary,
            double amount,
            ResourceLabeler goodResource
    ) {
        super(deprivationCoefficient);
        this.isNecessary = isNecessary;
        this.amount = amount;
        this.goodResource = goodResource;
    }

    @Override
    public boolean isNecessary() {
        return isNecessary;
    }

    @Override
    public boolean isResourceNeeded() {
        return true;
    }

    @Override
    public boolean hasNeeded(Tile tile) {
        return tile.getResourcePack().getResources().stream().anyMatch(this::isResourceGood);
    }

    boolean isResourceGood(Resource resource) {
        return isResourceDependency(resource);
    }

    public boolean isResourceDependency(Resource resource) {
        return goodResource.isSuitable(resource.getGenome()) && resource.getAmount() > 0;
    }
}
