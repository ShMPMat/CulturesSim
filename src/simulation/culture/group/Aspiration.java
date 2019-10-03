package simulation.culture.group;

import simulation.culture.aspect.AspectTag;

import java.util.Objects;

/**
 * Represents a goal which Group strives to fulfill.
 */
public class Aspiration {
    public int level;
    public AspectTag need;

    public Aspiration(int level, AspectTag need) {
        this.level = level;
        this.need = need;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aspiration that = (Aspiration) o;
        return need.equals(that.need);
    }

    @Override
    public int hashCode() {
        return Objects.hash(need);
    }
}
