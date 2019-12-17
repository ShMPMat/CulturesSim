package simulation.culture.group;

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
}
