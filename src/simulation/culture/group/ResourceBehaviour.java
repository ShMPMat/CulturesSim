package simulation.culture.group;

import extra.ProbabilityFuncs;
import simulation.space.resource.ResourcePack;

public class ResourceBehaviour {
    private PlacementStrategy placementStrategy;

    ResourceBehaviour(PlacementStrategy placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    public void procedeResources(ResourcePack resourcePack) {
        placementStrategy.place(resourcePack);
    }

    @Override
    public String toString() {
        return placementStrategy.toString();
    }

    public static ResourceBehaviour getRandom(Group group) {
        return new ResourceBehaviour(new PlacementStrategy(group.getOverallTerritory(),
                ProbabilityFuncs.randomElement(PlacementStrategy.Strategy.values())));
    }
}
