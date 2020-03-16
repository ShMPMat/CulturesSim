package simulation.space.resource;

import simulation.space.resource.tag.ResourceTag;

import java.util.*;
import java.util.stream.Collectors;

public class ResourcePack {
    protected Map<Resource, Resource> resourcesMap;

    public ResourcePack(Collection<Resource> resources) {
        this.resourcesMap = new HashMap<>();
        resources.forEach(this::internalAdd);
    }

    public ResourcePack() {
        this(Collections.emptyList());
    }

    protected void internalAdd(Resource resource) {
        if (resource.getAmount() == 0) {
            return;
        }
        Resource internal = resourcesMap.get(resource);
        if (internal == null) {
            resourcesMap.put(resource, resource);
        } else {
            internal.merge(resource);
        }
    }

    public List<Resource> getResources() {
        return new ArrayList<>(resourcesMap.values());
    }

    /**
     * @param tag Tag which will be looked up.
     * @return New ResourcePack with all Resources which contain required Tag.
     */
    public ResourcePack getAllResourcesWithTag(ResourceTag tag) {
        return new ResourcePack(getResources().stream()
                .filter(resource -> resource.getTags().contains(tag))
                .collect(Collectors.toList()));
    }

    public ResourcePack getResource(Resource resource) {
        Resource resourceInMap = resourcesMap.get(resource);
        return resourceInMap == null ? new ResourcePack() : new ResourcePack(Collections.singleton(resourceInMap));
    }

    public int getAmount() {
        return getResources().stream()
                .map(Resource::getAmount)
                .reduce(0, Integer::sum);
    }

    public int getAmountOfResourcesWithTag(ResourceTag tag) {
        return getAllResourcesWithTag(tag).getAmount();
    }

    public int getAmountOfResource(Resource resource) {
        return getResource(resource).getAmount();
    }

    public boolean isEmpty() {
        return getAmount() == 0;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Resource resource : getResources()) {
            stringBuilder.append(resource.getFullName()).append(" ")
                    .append(resource.getAmount())
                    .append("; ")
                    .append("\n");
        }
        return stringBuilder.toString();
    }
}
