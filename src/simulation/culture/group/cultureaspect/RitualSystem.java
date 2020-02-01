package simulation.culture.group.cultureaspect;

import extra.ProbFunc;
import simulation.Controller;
import simulation.culture.group.Group;
import simulation.culture.group.reason.Reason;
import simulation.culture.group.request.Request;
import simulation.culture.thinking.meaning.Meme;

import java.util.*;
import java.util.stream.Collectors;

public class RitualSystem extends AbstractCultureAspect {
    private Set<Ritual> rituals;
    private Reason reason;

    public RitualSystem(Group group, Collection<Ritual> rituals, Reason reason) {
        super(group);
        this.rituals = new HashSet<>(rituals);
        this.reason = reason;
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void use() {
        rituals.forEach(CultureAspect::use);
        if (!ProbFunc.testProbability(Controller.session.groupCollapsedAspectUpdate)) {
            return;
        }
        Ritual ritual = group.getCulturalCenter().constructRitualForReason(reason);
        if (ritual != null) {
            rituals.add(ritual);
        }
    }

    @Override
    public RitualSystem copy(Group group) {
        return new RitualSystem(group, rituals.stream()
                .map(r -> (Ritual) r.copy(group)).collect(Collectors.toList()),
                reason);
    }

    @Override
    public String toString() {
        return String.format("Ritual system for %s", reason);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RitualSystem that = (RitualSystem) o;
        return Objects.equals(rituals, that.rituals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rituals);
    }
}
