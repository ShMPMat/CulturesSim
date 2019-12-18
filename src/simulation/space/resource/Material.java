package simulation.space.resource;

import simulation.World;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;

import java.util.*;

/**
 * Represents material from which objects can be made.
 */
public class Material {
    /**
     * Link to the World in which this Material is present.
     */
    private World world;
    /**
     * Name of the Material.
     */
    private String name;
    /**
     * Properties associated with this Material.
     */
    private List<Property> properties;
    private Map<Aspect, Material> aspectConversion;
    private Map<Aspect, String> _aspectConversion;
    private double density;

    public Material(String[] tags, World world) {
        this.world = world;
        this.aspectConversion = new HashMap<>();
        this._aspectConversion = new HashMap<>();
        this.properties = new ArrayList<>();
        name = tags[0];
        density = Double.parseDouble(tags[1]);
        for (int i = 2; i < tags.length; i++) {
            String tag = tags[i];
            switch ((tag.charAt(0))) {
                case '+':
                    this._aspectConversion.put(world.getPoolAspect(tag.substring(1, tag.indexOf(':'))),
                            tag.substring(tag.indexOf(':') + 1));
                    break;
                case '-':
                    properties.add(world.getPoolProperty(tag.substring(1, tag.indexOf(':')))
                            .copy(Integer.parseInt(tag.substring(tag.indexOf(':') + 1))));
            }
        }
    }

    public void actualizeLinks() {
        for (Aspect aspect : _aspectConversion.keySet()) {
            aspectConversion.put(aspect, world.getPoolMaterial(_aspectConversion.get(aspect)));
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
        if (!resourceCore.containsTag(new AspectTag("goodForClothes")) &&
                properties.contains(world.getPoolProperty("flexible")) &&
                properties.contains(world.getPoolProperty("solid")) &&
                properties.contains(world.getPoolProperty("soft"))) {
            _t.add(new AspectTag("goodForClothes"));
        }
        if (!resourceCore.containsTag(new AspectTag("weapon")) &&
                properties.contains(world.getPoolProperty("hard")) &&
                properties.contains(world.getPoolProperty("sturdy")) &&
                resourceCore.getSize() >= 0.05 && resourceCore.isMovable()) {
            _t.add(new AspectTag("weapon"));
        }
        if (!resourceCore.containsTag(new AspectTag("goodForEngraving")) &&
                properties.contains(world.getPoolProperty("hard")) &&
                properties.contains(world.getPoolProperty("sturdy"))) {
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

    public double getDensity() {
        return density;
    }

    public boolean hasPropertyWithName(String name) {
        return hasProperty(world.getPoolProperty(name));
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
