package simulation.culture.group;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.aspect.ConverseWrapper;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Request {
    private Group group;
    AspectTag tag;
    Resource resource;
    int floor, ceiling;
    private BiFunction<ShnyPair<Group, ResourcePack>, Double,Void> penalty, reward;

    Request(Group group, int floor, int ceiling, BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> penalty,
            BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> reward) {
        this.group = group;
        this.floor = floor;
        this.ceiling = ceiling;
        this.penalty = penalty;
        this.reward = reward;
        this.tag = null;
        this.resource = null;
    }

    Request(Group group, AspectTag tag, int floor, int ceiling, BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> penalty,
                   BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> reward) {
        this(group, floor, ceiling, penalty, reward);
        this.tag = tag;
    }

    Request(Group group, Resource resource, int floor, int ceiling, BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> penalty,
            BiFunction<ShnyPair<Group, ResourcePack>, Double, Void> reward) {
        this(group, floor, ceiling, penalty, reward);
        this.resource = resource;
    }

    boolean hasTagFrom(Collection<AspectTag> tags) {
        return tags.contains(tag);
    }

    Function<ResourcePack, Integer> isAcceptable(Aspect aspect) {
        if (tag != null) {
            if (hasTagFrom(aspect.getTags())) {
                return resourcePack -> resourcePack.getAmountOfResourcesWithTag(tag);
            } else {
                return null;
            }
        }
        if (resource != null) {
            if (aspect instanceof ConverseWrapper) {
                if (((ConverseWrapper) aspect).getResult().stream().anyMatch(res -> res.getSimpleName().equals(resource.getSimpleName()))) {
                    return resourcePack -> resourcePack.getAmountOfResource(resource);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    Function<ResourcePack, ResourcePack> isAcceptable(Stratum stratum) {
        if (tag != null) {
            if (stratum.getAspects().stream().anyMatch(aspect -> hasTagFrom(aspect.getTags()))) {
                return resourcePack -> resourcePack.getAllResourcesWithTag(tag);
            } else {
                return null;
            }
        }
        if (resource != null) {
            for (Aspect aspect : stratum.getAspects()) {
                if (aspect instanceof ConverseWrapper) {
                    if (((ConverseWrapper) aspect).getResult().stream().anyMatch(res -> res.getSimpleName().equals(resource.getSimpleName()))) {
                        return resourcePack -> resourcePack.getResource(resource);
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    int satisfactionLevel(Resource sample) {
        if (tag != null) {
            int index = sample.getTags().indexOf(tag);
            return index == -1 ? 0 : sample.getTags().get(index).level;
        }
        if (resource != null) {
            return resource == sample ? 1 : 0;
        }
        return 0;
    }

    int satisfactionLevel(Stratum stratum) {
        int result = 0;
        for (ConverseWrapper converseWrapper: stratum.getAspects().stream().filter(aspect ->
                aspect instanceof ConverseWrapper).map(aspect -> (ConverseWrapper) aspect).collect(Collectors.toList())) {
            result += converseWrapper.getResult().stream().reduce(0, (x, y) -> x += satisfactionLevel(y), Integer::sum);
        }
        return result;
    }

    int howMuchOfNeeded(ResourcePack resourcePack) {
        if (tag != null) {
            return resourcePack.getAmountOfResourcesWithTag(tag);
        }
        if (resource != null) {
            return resourcePack.getAmountOfResource(resource);
        }
        return 0;
    }

    void end(ResourcePack resourcePack) {
        int amount = 0;
        ResourcePack _rp = new ResourcePack();
        if (tag != null) {
            ResourcePack _r = resourcePack.getResourcesWithTagPart(tag, ceiling);
            amount = _r.getAmount();
            _rp.add(_r);
        }
        if (resource != null) {
            ResourcePack _r = resourcePack.getResourcePart(resource, ceiling);
            amount = _r.getAmount();
            _rp.add(_r);
        }

        if (amount < floor) {
            penalty.apply(new ShnyPair<>(group, _rp), amount / ((double) floor));
            return;
        }
        reward.apply(new ShnyPair<>(group, _rp), amount / ((double) floor) - 1);
    }

    @Override
    public String toString() {
        if (tag != null) {
            return "want for " + tag;
        }
        if (resource != null) {
            return "resource " + resource.getBaseName();
        }
        return "W H A T ?";
    }
}
