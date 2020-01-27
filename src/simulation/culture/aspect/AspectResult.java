package simulation.culture.aspect;

import simulation.culture.aspect.dependency.Dependency;
import simulation.space.resource.ResourcePack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AspectResult {
    public boolean isFinished;
    public ResourcePack resources;
    public ResultNode node;

    public AspectResult(boolean isFinished, ResourcePack resources, ResultNode node) {
        this.isFinished = isFinished;
        this.resources = resources;
        this.node = node;
    }

    public AspectResult(boolean isFinished, ResultNode node) {
        this(isFinished, new ResourcePack(), node);
    }

    public AspectResult(ResourcePack resources, ResultNode node) {
        this(true, resources, node);
    }

    public static class ResultNode {
        public Aspect aspect;
        public Map<AspectTag, ResourcePack> resourceUsed = new HashMap<>();

        public ResultNode(Aspect aspect) {
            this.aspect = aspect;
        }
    }
}

