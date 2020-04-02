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
                if (aspect.getProducedResources().stream()
                        .anyMatch(res -> res.getBaseName().equals(resource.getBaseName()))) {
                    return getEvaluator();
                }
            }
        }
        return null;
    }

    @Override
    public ResourceEvaluator getEvaluator() {
        return EvaluatorsKt.resourceEvaluator(resource);
    }

    @Override
    public int satisfactionLevel(Resource sample) {
        return resource == sample ? 1 : 0;
    }

    @Override
    public void end(MutableResourcePack resourcePack) {
        int amount = 0;
        MutableResourcePack _rp = new MutableResourcePack();

        MutableResourcePack _r = resourcePack.getResourcePart(resource, getCeiling());
        amount = _r.getAmount();
        _rp.addAll(_r);

        if (amount < getFloor()) {
            getPenalty().apply(new Pair<>(getGroup(), _rp), amount / ((double) getFloor()));
            return;
        }
        getReward().apply(new Pair<>(getGroup(), _rp), amount / ((double) getFloor()) - 1);
    }

    @Override
    public String toString() {
        return "resource " + resource.getBaseName();
    }
}
