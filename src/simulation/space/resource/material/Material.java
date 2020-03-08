package simulation.space.resource.material;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectPool;
import simulation.space.resource.ResourceTag;

import java.util.*;

/**
 * Represents material from which objects can be made.
 */
public class Material {
    private String name;
    private List<ResourceTag> tags = new ArrayList<>();
    private Map<Aspect, Material> aspectConversion;
    private Map<Aspect, String> _aspectConversion;
    private double density;

    public Material(String[] tags, AspectPool aspectPool) {
        this.aspectConversion = new HashMap<>();
        this._aspectConversion = new HashMap<>();
        name = tags[0];
        density = Double.parseDouble(tags[1]);
        for (int i = 2; i < tags.length; i++) {
            String tag = tags[i];
            switch ((tag.charAt(0))) {
                case '+':
                    this._aspectConversion.put(aspectPool.get(tag.substring(1, tag.indexOf(':'))),
                            tag.substring(tag.indexOf(':') + 1));
                    break;
                case '-':
                    this.tags.add(new ResourceTag(
                            tag.substring(1, tag.indexOf(':')),
                            Integer.parseInt(tag.substring(tag.indexOf(':') + 1))
                            ));
            }
        }
    }

    public void actualizeLinks(MaterialPool materialPool) {
        for (Aspect aspect : _aspectConversion.keySet()) {
            aspectConversion.put(aspect, materialPool.get(_aspectConversion.get(aspect)));
        }
        _aspectConversion = null;
    }

    public List<ResourceTag> getTags() {
        return tags;
    }

    public Material applyAspect(Aspect aspect) {
        if (aspectConversion.containsKey(aspect)) {
            return aspectConversion.get(aspect);
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public double getDensity() {
        return density;
    }

    public boolean hasTagWithName(String name) {
        return tags.stream().anyMatch(tag -> tag.name.equals(name));
    }

    public boolean hasApplicationForAspect(Aspect aspect) {
        return aspectConversion.containsKey(aspect);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return Objects.equals(name, material.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
