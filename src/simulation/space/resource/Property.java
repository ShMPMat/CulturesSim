package simulation.space.resource;

import simulation.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Basic property of an object or a material.
 */
public class Property {
    private String name;
    private List<ResourceTag> tags;
    private int level = 1;

    public Property(String[] tags) {
        this.tags = new ArrayList<>();
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            if (i == 0) {
                this.name = tag;
            } else {
                switch ((tag.charAt(0))) {
                    case '-':
                        this.tags.add(new ResourceTag(tag.substring(1)));
                }
            }
        }
        setAspectLevel();
    }

    private Property(String name, List<ResourceTag> tags, int level) {
        this.name = name;
        this.tags = tags.stream().map(ResourceTag::copy).collect(Collectors.toList());
        this.level = level;
        setAspectLevel();
    }

    /**
     * Changes level of tags corresponding to this Property level.
     */
    private void setAspectLevel() {
        tags.forEach(aspectTag -> aspectTag.level = level);
    }

    /**
     * Getter for tags.
     * @return AspectTags associated with this Property.
     */
    public List<ResourceTag> getTags() {
        return tags;
    }

    /**
     * Getter for name.
     * @return name of this Property.
     */
    public String getName() {
        return name;
    }

    /**
     * @param level Level of a copy of this Property.
     * @return New instance of This Property with a new level.
     */
    public Property copy(int level) {
        return new Property(name, tags, level);
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
}
