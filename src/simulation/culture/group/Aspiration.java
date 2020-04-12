package simulation.culture.group;

import simulation.culture.aspect.Aspect;
import simulation.space.resource.tag.ResourceTag;
import simulation.culture.aspect.ConverseWrapper;
import simulation.space.resource.Resource;
import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.resource.tag.labeler.TagLabeler;

import java.util.Objects;

/**
 * Represents a goal which Group strives to fulfill.
 */
public class Aspiration {
    int level;
    String usedOn;
    private ResourceTag need;
    private String turn;

    public Aspiration(int level, ResourceTag need, String turn) {
        this.level = level;
        this.need = need;
        this.turn = turn;
    }

    boolean isAcceptable(Aspect aspect) {
        if (need != null) {
            return aspect.getTags().contains(need);
        }
        return false;
    }

    ResourceLabeler getLabeler() {
        return new TagLabeler(need);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aspiration that = (Aspiration) o;
        return Objects.equals(need, that.need);
    }

    @Override
    public int hashCode() {
        return Objects.hash(need);
    }

    @Override
    public String toString() {
        if (need != null) {
            return need.name;
        }
        return "WRONG ASPIRATION";
    }
}
