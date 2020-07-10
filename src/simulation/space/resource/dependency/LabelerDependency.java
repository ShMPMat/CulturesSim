package simulation.space.resource.dependency;

import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.tile.Tile;
import simulation.space.resource.Resource;

public abstract class LabelerDependency extends CoefficientDependency {
    private ResourceLabeler goodResource;
    private boolean isNecessary;
    double amount;

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
        return tile.getResourcePack().any(this::isResourceGood);
    }

    boolean isResourceGood(Resource resource) {
        return isResourceDependency(resource);
    }

    public boolean isResourceDependency(Resource resource) {
        return goodResource.isSuitable(resource.getGenome()) && resource.isNotEmpty();
    }

    public int oneResourceWorth(Resource resource) {
        return goodResource.actualMatches(resource.copy(1, resource.getOwnershipMarker())).stream()
                .map(Resource::getAmount)
                .reduce(0, Integer::sum);
    }

    public int partByResource(Resource resource, double amount) {
        return (int) Math.ceil(amount / goodResource.actualMatches(resource.copy(1, resource.getOwnershipMarker())).stream()
                .map(Resource::getAmount)
                .reduce(0, Integer::sum));
    }
}
