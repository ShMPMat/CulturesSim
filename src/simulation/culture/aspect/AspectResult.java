package simulation.culture.aspect;

import kotlin.Pair;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.tag.ResourceTag;
import simulation.space.resource.tag.labeler.ResourceLabeler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AspectResult {
    public boolean isFinished;
    public List<Pair<ResourceLabeler, Integer>> neededResources;
    public MutableResourcePack resources;
    public ResultNode node;

    public AspectResult(
            boolean isFinished,
            List<Pair<ResourceLabeler, Integer>> neededResources,
            MutableResourcePack resources,
            ResultNode node
    ) {
        this.isFinished = isFinished;
        this.neededResources = neededResources;
        this.resources = resources;
        this.node = node;
    }

    public AspectResult(boolean isFinished, ResultNode node) {
        this(isFinished, new ArrayList<>(), new MutableResourcePack(), node);
    }

    public AspectResult(MutableResourcePack resources, ResultNode node) {
        this(true, new ArrayList<>(), resources, node);
    }

    public static class ResultNode {
        public Aspect aspect;
        public Map<ResourceTag, MutableResourcePack> resourceUsed = new HashMap<>();

        public ResultNode(Aspect aspect) {
            this.aspect = aspect;
        }
    }
}

