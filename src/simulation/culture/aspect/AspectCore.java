package simulation.culture.aspect;

import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.Group;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;

/**
 * Class which contains all general information about all Aspects with the same name.
 */
class AspectCore {
    private String name;
    /**
     * AspectTags which represent which resources are provided by this Aspect.
     */
    private List<ResourceTag> tags;
    /**
     * Requirements for an Aspect to be added to the group.
     */
    private List<ResourceTag> requirements;
    private List<AspectMatcher> matchers;
    boolean applyMeaning;

    public AspectCore(String name,
                      ArrayList<ResourceTag> aspectTags,
                      ArrayList<ResourceTag> requirements,
                      ArrayList<AspectMatcher> matchers,
                      boolean applyMeaning) {
        this.name = name;
        this.tags = aspectTags;
        this.requirements = requirements;
        this.matchers = matchers;
        this.applyMeaning = applyMeaning;
    }


    String getName() {
        return name;
    }

    List<ResourceTag> getTags() {
        return tags;
    }

    Collection<ResourceTag> getRequirements() {
        return requirements;
    }

    public List<AspectMatcher> getMatchers() {
        return matchers;
    }

    void addAllTags(Collection<ResourceTag> tags) {
        for (ResourceTag tag : tags) {
            if (!this.tags.contains(tag)) {
                this.tags.add(tag);
            } else if (this.tags.get(this.tags.indexOf(tag)).level < tag.level) {
                this.tags.remove(tag);
                this.tags.add(tag);
            }
        }
    }

    Aspect copy(Map<ResourceTag, Set<Dependency>> dependencies, Group group) {
        return new Aspect(this, dependencies, group);
    }
}
