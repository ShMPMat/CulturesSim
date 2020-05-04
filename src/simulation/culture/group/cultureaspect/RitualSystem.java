package simulation.culture.group.cultureaspect;

import shmp.random.RandomProbabilitiesKt;
import simulation.Controller;
import simulation.culture.group.centers.Group;
import simulation.culture.group.reason.Reason;
import simulation.culture.group.request.Request;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RitualSystem implements CultureAspect {
    private Set<Ritual> rituals;
    private Reason reason;

    public RitualSystem(Group group, Collection<Ritual> rituals, Reason reason) {
        this.rituals = new HashSet<>(rituals);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public Set<Ritual> getRituals() {
        return rituals;
    }

    public void addRitual(Ritual ritual) {
        rituals.add(ritual);
    }

    @Override
    public Request getRequest(Group group) {
        return null;
    }

    @Override
    public void use(Group group) {
        rituals.forEach(r -> r.use(group));
        if (!RandomProbabilitiesKt.testProbability(
                Controller.session.groupCollapsedAspectUpdate,
                Controller.session.random)
        ) {
            return;
        }
        Ritual ritual = ConstructCultureAspectKt.constructRitual(
                reason,
                group,
                Controller.session.random
        );
        if (ritual != null) {
            rituals.add(ritual);
        }
    }

    @Override
    public RitualSystem adopt(Group group) {
        return new RitualSystem(group, rituals.stream()
                .map(r -> (Ritual) r.adopt(group)).collect(Collectors.toList()),
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

    @Override
    public void die(Group group) {
        rituals.forEach(r -> r.die(group));
    }
}
