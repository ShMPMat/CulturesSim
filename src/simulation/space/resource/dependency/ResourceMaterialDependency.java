package simulation.space.resource.dependency;

import simulation.space.Tile;
import simulation.space.resource.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ResourceMaterialDependency extends CoefficientDependency {
    private List<String> resourceNames = new ArrayList<>();
    private List<String> materialNames = new ArrayList<>();
    private boolean isNecessary;
    double amount;
    int currentAmount = 0;

    public ResourceMaterialDependency(double deprivationCoefficient, boolean isNecessary, double amount,
                                      Collection<String> names) {
        super(deprivationCoefficient);
        this.isNecessary = isNecessary;
        this.amount = amount;
        for (String name: names) {
            if (name.charAt(0) == '@') {
                this.materialNames.add(name.substring(1));
            } else {
                this.resourceNames.add(name);
            }
        }
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

    boolean isResourceDependency(Resource resource) {
        return (resourceNames.contains(resource.getSimpleName()) || resource.getGenome().getPrimaryMaterial() != null &&
                materialNames.contains(resource.getGenome().getPrimaryMaterial().getName())) && resource.getAmount() > 0;
    }
}
