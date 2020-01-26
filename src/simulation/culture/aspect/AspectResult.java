package simulation.culture.aspect;

import simulation.space.resource.ResourcePack;

public class AspectResult {
    public boolean isFinished;
    public ResourcePack resources;

    public AspectResult(boolean isFinished, ResourcePack resources) {
        this.isFinished = isFinished;
        this.resources = resources;
    }

    public AspectResult(boolean isFinished) {
        this(isFinished, new ResourcePack());
    }

    public AspectResult(ResourcePack resources) {
        this(true, resources);
    }
}
