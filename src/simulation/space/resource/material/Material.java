package simulation.space.resource.material;

import simulation.World;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectPool;
import simulation.space.resource.Property;
import simulation.space.resource.ResourceCore;
import simulation.space.resource.ResourceTag;

import java.util.*;
import java.util.stream.Collectors;

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

    public Material(String[] tags, World world, AspectPool aspectPool) {
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
                    this._aspectConversion.put(aspectPool.get(tag.substring(1, tag.indexOf(':'))),
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
            aspectConversion.put(aspect, world.getMaterialPool().get(_aspectConversion.get(aspect)));
        }
        _aspectConversion = null;
    }

    public List<ResourceTag> getTags(ResourceCore resourceCore) {
        return computeTags(resourceCore);
    }

    private List<ResourceTag> computeTags(ResourceCore resourceCore) {//TODO where do I add the goddamn goodForEngraving tag?
        List<ResourceTag> _t = properties.stream()
                .flatMap(p -> p.getTags().stream())
                .collect(Collectors.toList());
        if (!resourceCore.containsTag(new ResourceTag("goodForClothes")) &&
                properties.contains(world.getPoolProperty("flexible")) &&
                properties.contains(world.getPoolProperty("solid")) &&
                properties.contains(world.getPoolProperty("soft"))) {
            _t.add(new ResourceTag("goodForClothes"));
        }
        if (!resourceCore.containsTag(new ResourceTag("weapon")) &&
                properties.contains(world.getPoolProperty("hard")) &&
                properties.contains(world.getPoolProperty("sturdy")) &&
                resourceCore.getSize() >= 0.05 && resourceCore.isMovable()) {
            _t.add(new ResourceTag("weapon"));
        }
        if (!resourceCore.containsTag(new ResourceTag("goodForEngraving")) &&
                properties.contains(world.getPoolProperty("hard")) &&
                properties.contains(world.getPoolProperty("sturdy"))) {
            _t.add(new ResourceTag("goodForEngraving"));
        }
        properties.forEach(property -> _t.add(new ResourceTag(property.getName())));
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
