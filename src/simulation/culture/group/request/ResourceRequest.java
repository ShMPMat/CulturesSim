package simulation.culture.group.request;

import kotlin.Pair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.Stratum;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.function.BiFunction;

public class ResourceRequest extends Request {
    private Resource resource;

    public ResourceRequest(Group group, Resource resource, int floor, int ceiling, BiFunction<Pair<Group, ResourcePack>, Double, Void> penalty, BiFunction<Pair<Group, ResourcePack>, Double, Void> reward) {
        super(group, floor, ceiling, penalty, reward);
        this.resource = resource;
    }

    @Override
    public ResourceEvaluator isAcceptable(Stratum stratum) {
        for (Aspect aspect : stratum.getAspects()) {
            if (aspect instanceof ConverseWrapper) {
                if (((ConverseWrapper) aspect).getResult().stream().anyMatch(res -> res.getSimpleName().equals(resource.getSimpleName()))) {
                    return new ResourceEvaluator(resourcePack -> resourcePack.getResource(resource),
                            resourcePack -> resourcePack.getAmountOfResource(resource));
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public int satisfactionLevel(Resource sample) {
        return resource == sample ? 1 : 0;
    }

    @Override
    public void end(ResourcePack resourcePack) {
        int amount = 0;
        ResourcePack _rp = new ResourcePack();

        ResourcePack _r = resourcePack.getResourcePart(resource, ceiling);
        amount = _r.getAmount();
        _rp.add(_r);

        if (amount < floor) {
            penalty.apply(new Pair<>(group, _rp), amount / ((double) floor));
            return;
        }
        reward.apply(new Pair<>(group, _rp), amount / ((double) floor) - 1);
    }

    @Override
    public String toString() {
        return "resource " + resource.getBaseName();
    }
}
