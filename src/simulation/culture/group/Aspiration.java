package simulation.culture.group;

import simulation.culture.aspect.Aspect;
import simulation.space.resource.tag.ResourceTag;
import simulation.culture.aspect.ConverseWrapper;
import simulation.space.resource.Resource;

import java.util.Objects;

/**
 * Represents a goal which Group strives to fulfill.
 */
public class Aspiration {
    int level;
    private ResourceTag need;
    private Resource resource;

    public Aspiration(int level, ResourceTag need) {
        this.level = level;
        this.need = need;
        resource = null;
    }

    public Aspiration(int level, Resource resource) {
        this.level = level;
        this.resource = resource;
        need = null;
    }

    boolean isAcceptable(Aspect aspect) {
        if (need != null) {
            return aspect.getTags().contains(need);
        }
        if (resource != null) {
            if (aspect instanceof ConverseWrapper) {
                return ((ConverseWrapper) aspect).getProducedResources().contains(resource);
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aspiration that = (Aspiration) o;
        return Objects.equals(need, that.need) && Objects.equals(resource, that.resource);
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
        if (resource != null) {
            return resource.getBaseName();
        }
        return "WRONG ASPIRATION";
    }
}
