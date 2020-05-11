package simulation.space.resource.dependency;

import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.tile.Tile;
import simulation.space.resource.Resource;

import java.util.HashSet;
import java.util.Set;

public class ConsumeDependency extends LabelerDependency {
    public Set<String> lastConsumed = new HashSet<>();
    int currentAmount = 0;

    public ConsumeDependency(
            double deprivationCoefficient,
            boolean isNecessary,
            double amount,
            ResourceLabeler goodResource) {
        super(deprivationCoefficient, isNecessary, amount, goodResource);
    }

    @Override
    double satisfaction(Tile tile, Resource resource) {
        if (currentAmount < 0) currentAmount = 0;
        double result;
        double neededAmount = amount * (resource == null ? 1 : resource.getAmount());
        if (currentAmount < neededAmount) {
            for (Resource res : tile.getAccessibleResources()) {
                if (res.equals(resource)) {
                    continue;
                }
                if (isResourceDependency(res)) {
                    Resource part = res.getPart(partByResource(res, neededAmount - currentAmount));
                    if (part.isNotEmpty()) {
                        lastConsumed.add(part.getFullName());
                        currentAmount += part.getAmount() * oneResourceWorth(res);
                    }
                    part.destroy();
                    if (currentAmount >= neededAmount) {
                        break;
                    }
                }
            }
        }
        result = Math.min(((double) currentAmount) / neededAmount, 1);
        if (currentAmount >= neededAmount) {
            currentAmount -= neededAmount;
        }
        return result;
    }

    @Override
    public boolean isPositive() {
        return true;
    }
}
