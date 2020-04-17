package simulation.space.resource.dependency;

import simulation.space.resource.Resource;
import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.tile.Tile;

import java.util.HashSet;
import java.util.Set;

public class AvoidDependency extends LabelerDependency {
    public Set<String> lastConsumed = new HashSet<>();

    public AvoidDependency(
            double amount,
            double deprivationCoefficient,
            boolean isNecessary,
            ResourceLabeler goodResources
    ) {
        super(deprivationCoefficient, isNecessary, amount, goodResources);
    }

    @Override
    double satisfaction(Tile tile, Resource resource) {
        double result;
        double _amount = amount * (resource == null ? 1 : resource.getAmount());
        int currentAmount = 0;
        for (Resource res : tile.getAccessibleResources()) {
            if (res.equals(resource)) {
                continue;
            }
            if (super.isResourceDependency(res)) {
                currentAmount += res.getAmount();
                if (currentAmount >= _amount) {
                    break;
                }
            }
        }
        result = Math.min(((double) currentAmount) / _amount, 1);
        return 1 - result;
    }

    @Override
    public boolean hasNeeded(Tile tile) {
        return tile.getAccessibleResources().stream().anyMatch(this::isResourceDependency);
    }

    @Override
    public boolean isResourceDependency(Resource resource) {
        return false;
    }

    @Override
    public boolean isPositive() {
        return false;
    }
}
