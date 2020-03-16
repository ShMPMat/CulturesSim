package simulation.culture.group.request;

import kotlin.Pair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.Stratum;
import simulation.space.resource.Resource;
import simulation.space.resource.MutableResourcePack;

import java.util.function.BiFunction;

public class ResourceRequest extends Request {
    private Resource resource;

    public ResourceRequest(Group group, Resource resource, int floor, int ceiling, BiFunction<Pair<Group, MutableResourcePack>, Double, Void> penalty, BiFunction<Pair<Group, MutableResourcePack>, Double, Void> reward) {
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
    public void end(MutableResourcePack resourcePack) {
        int amount = 0;
        MutableResourcePack _rp = new MutableResourcePack();

        MutableResourcePack _r = resourcePack.getResourcePart(resource, ceiling);
        amount = _r.getAmount();
        _rp.addAll(_r);

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
