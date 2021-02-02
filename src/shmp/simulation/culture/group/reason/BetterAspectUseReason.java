package shmp.simulation.culture.group.reason;

import shmp.simulation.culture.aspect.ConverseWrapper;
import shmp.simulation.culture.group.centers.Group;

import java.util.Objects;

public class BetterAspectUseReason extends AbstractReason {
    ConverseWrapper converseWrapper;

    public BetterAspectUseReason(Group group, ConverseWrapper converseWrapper) {
        super(group);
        this.converseWrapper = converseWrapper;
    }

    public ConverseWrapper getConverseWrapper() {
        return converseWrapper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BetterAspectUseReason that = (BetterAspectUseReason) o;
        return Objects.equals(converseWrapper, that.converseWrapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(converseWrapper);
    }

    @Override
    public String toString() {
        return "Better " + converseWrapper.getName();
    }
}
