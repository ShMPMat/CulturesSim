package simulation.space.resource;

import simulation.World;
import simulation.culture.aspect.AspectTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Basic property of an object or a material.
 */
public class Property {
    private String name;
    private World world;
    private List<AspectTag> tags;

    public Property(String[] tags, World world) {
        this.tags = new ArrayList<>();
        this.world = world;
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            if (i == 0) {
                this.name = tag;
            } else {
                switch ((tag.charAt(0))) {
                    case '-':
                        this.tags.add(new AspectTag(tag.substring(1)));
                }
            }
        }
    }

    public List<AspectTag> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return Objects.equals(name, property.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getName() {
        return name;
    }
}
