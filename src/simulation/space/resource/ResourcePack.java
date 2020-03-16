package simulation.space.resource;

import kotlin.Pair;
import simulation.space.Tile;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;
import java.util.stream.Collectors;

public class ResourcePack { //TODO subclass which stores all instances of the same Resource on different Tiles
    private Map<Resource, Resource> resourcesMap;

    public ResourcePack(Collection<Resource> resources) {
        this.resourcesMap = new HashMap<>();
        resources.forEach(this::add);
    }

    public ResourcePack() {
        this(Collections.emptyList());
    }

    /**
     * @return Resources which are contained in this pack.
     */
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

    public ResourcePack getResourceAndRemove(Resource resource) {
        ResourcePack _r = getResource(resource);
        _r.getResources().forEach(this::remove);
        return _r;
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

    public ResourcePack getResourcesWithTagPartIsBigger(ResourceTag tag, int ceiling) {
        return getPart(getAllResourcesWithTag(tag), ceiling);
    }

    public ResourcePack getResourcePart(Resource resource, int ceiling) {
        return getPart(getResource(resource), ceiling);
    }

    private ResourcePack getPart(ResourcePack pack, int amount) {
        ResourcePack result = new ResourcePack();
        int counter = 0;
        for (Resource resource : pack.getResources()) {
            if (counter >= amount) {
                break;
            }
            result.add(resource);
            counter += resource.getAmount();
        }
        return result;
    }

    public Pair<Integer, List<Resource>> getAmountOfResourcesWithTagAndErase(ResourceTag tag, int amount) {//TODO looks bad
        Collection<Resource> _r = getAllResourcesWithTag(tag).getResources();
        List<Resource> result = new ArrayList<>();
        int counter = 0;
        for (Resource resource : _r) {
            if (counter >= amount) {
                break;
            }
            result.add(resource);
            counter += resource.getAmount();
        }
        result.forEach(this::remove);
        return new Pair<>(counter, result);
    }

    /**
     * @return whether this ResourcePack has any amount of Resources.
     */
    public boolean isEmpty() {
        return getAmount() <= 0;
    }

    /**
     * Disbands all the Resources on the particular Tile and clears this ResourcePack.
     *
     * @param tile Tile on which resources will be disbanded.
     */
    public void disbandOnTile(Tile tile) {
        getResources().stream()
                .filter(resource -> resource.getGenome().isMovable())
                .forEach(tile::addDelayedResource);
        resourcesMap.clear();
    }

    public void add(Resource resource) {
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

    /**
     * Adds all Resources from a ResourcePack.
     * //TODO will source be affected?
     * @param resourcePack Pack from which all Resources will be added.
     */
    public void addAll(ResourcePack resourcePack) {
        resourcePack.getResources().forEach(this::add);
    }

    public void addAll(Collection<Resource> resources) {
        resources.forEach(this::add);
    }

    public void remove(Resource resource) {
        resourcesMap.remove(resource);
    }

    public void removeAll(Collection<Resource> resources) {
        resources.forEach(this::remove);
    }

    public void removeAll(ResourcePack pack) {
        pack.getResources().forEach(this::remove);
    }

    public void destroyAllResourcesWithTag(ResourceTag tag) {
        ResourcePack result = getAllResourcesWithTag(tag);
        removeAll(result);
        result.getResources().forEach(resource -> resource.setAmount(0));
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
