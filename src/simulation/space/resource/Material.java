package simulation.space.resource;

import simulation.World;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;

import java.util.*;

/**
 * Represents a material from which objects can consist.
 */
public class Material {
    private List<Property> properties;
    private String name;
    private Map<Aspect, Material> aspectConversion;
    private Map<Aspect, String> _aspectConversion;
    private World world;

    public Material(String[] tags, World world) {
        this.world = world;
        this.aspectConversion = new HashMap<>();
        this._aspectConversion = new HashMap<>();
        this.properties = new ArrayList<>();
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            if (i == 0) {
                this.name = tag;
            } else {
                switch ((tag.charAt(0))) {
                    case '+':
                        this._aspectConversion.put(world.getAspectFromPoolByName(tag.substring(1, tag.indexOf(':'))),
                                tag.substring(tag.indexOf(':') + 1));
                        break;
                    case '-':
                        properties.add(world.getPropertyFromPoolByName(tag.substring(1)));
                }
            }
        }
    }

    public void actualizeLinks() {
        for (Aspect aspect : _aspectConversion.keySet()) {
            aspectConversion.put(aspect, world.getMaterialFromPoolByName(_aspectConversion.get(aspect)));
        }
        _aspectConversion = null;
    }

    public List<AspectTag> getTags(ResourceCore resourceCore) {
        return computeTags(resourceCore);
    }

    private List<AspectTag> computeTags(ResourceCore resourceCore) {
        List<AspectTag> _t = properties.stream().map(Property::getTags).reduce(new ArrayList<>(), (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        });
        if (properties.contains(world.getPropertyFromPoolByName("flexible")) &&
                properties.contains(world.getPropertyFromPoolByName("solid")) &&
                properties.contains(world.getPropertyFromPoolByName("soft"))) {
            _t.add(new AspectTag("goodForClothes"));
        }
        if (properties.contains(world.getPropertyFromPoolByName("hard")) &&
                properties.contains(world.getPropertyFromPoolByName("sturdy")) &&
                !resourceCore.getTags().contains(new AspectTag("small")) &&
                resourceCore.isMovable()) {
            _t.add(new AspectTag("weapon"));
        }
        if (properties.contains(world.getPropertyFromPoolByName("hard")) &&
                properties.contains(world.getPropertyFromPoolByName("sturdy"))) {
            _t.add(new AspectTag("goodForEngraving"));
        }
        return _t;
    }

    public boolean hasProperty(Property property) {
        return properties.contains(property);
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
