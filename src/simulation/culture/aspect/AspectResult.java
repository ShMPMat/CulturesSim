package simulation.culture.aspect;

import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public class AspectResult {
    public boolean isFinished;
    public MutableResourcePack resources;
    public ResultNode node;

    public AspectResult(boolean isFinished, MutableResourcePack resources, ResultNode node) {
        this.isFinished = isFinished;
        this.resources = resources;
        this.node = node;
    }

    public AspectResult(boolean isFinished, ResultNode node) {
        this(isFinished, new MutableResourcePack(), node);
    }

    public AspectResult(MutableResourcePack resources, ResultNode node) {
        this(true, resources, node);
    }

    public static class ResultNode {
        public Aspect aspect;
        public Map<ResourceTag, MutableResourcePack> resourceUsed = new HashMap<>();

        public ResultNode(Aspect aspect) {
            this.aspect = aspect;
        }
    }
}

