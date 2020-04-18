package simulation.space.resource.dependency;

import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.tile.Tile;
import simulation.space.resource.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NeedDependency extends LabelerDependency {
    public Set<String> lastConsumed = new HashSet<>();

    public NeedDependency(
            double amount,
            double deprivationCoefficient,
            boolean isNecessary,
            ResourceLabeler goodResources
    ) {
        super(deprivationCoefficient, isNecessary, amount, goodResources);
    }

    @Override
    double satisfaction(Tile tile, Resource resource) {
        double _amount = amount * (resource == null ? 1 : resource.getAmount());
        int currentAmount = 0;
        for (Resource res : tile.getAccessibleResources()) {
            if (res.equals(resource)) {
                continue;
            }
            if (isResourceDependency(res)) {
                currentAmount += res.getAmount();
                if (currentAmount >= _amount) {
                    break;
                }
            }
        }
        return Math.min(((double) currentAmount) / _amount, 1);
    }

    @Override
    public boolean hasNeeded(Tile tile) {
        return tile.getAccessibleResources().stream().anyMatch(this::isResourceDependency);
    }

    @Override
    public boolean isPositive() {
        return true;
    }
}
