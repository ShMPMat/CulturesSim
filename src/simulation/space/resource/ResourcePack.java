package simulation.space.resource;

import extra.ShnyPair;
import simulation.space.Tile;
import simulation.space.resource.tag.ResourceTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bunch of resources stored together.
 */
public class ResourcePack { //TODO subclass which stores all instances of the same Resource on different Tiles
    /**
     * Resources which this pack contains.
     */
    public List<Resource> resources;

    public ResourcePack(Collection<Resource> resources) {
        this.resources = new ArrayList<>(resources);
    }

    public ResourcePack() {
        this(Collections.emptyList());
    }

    /**
     * @return Resources which are contained in this pack.
     */
    public Collection<Resource> getResources() {
        return resources;
    }

    /**
     * @param tag Tag which will be looked up.
     * @return New ResourcePack with all Resources which contain required Tag.
     */
    public ResourcePack getAllResourcesWithTag(ResourceTag tag) {
        return new ResourcePack(resources.stream().filter(resource -> resource.getTags().contains(tag))
                .collect(Collectors.toList()));
    }

    public ResourcePack getResource(Resource resource) {
        List<Resource> _r = resources.stream().filter(res -> res.fullEquals(resource)).collect(Collectors.toList());
        return _r.size() == 0 ? new ResourcePack() : new ResourcePack(_r);
    }

    public ResourcePack getResourceAndRemove(Resource resource) {
        ResourcePack _r = getResource(resource);
        _r.resources.forEach(this::remove);
        return _r;
    }

    public int getAmountOfResourcesWithTag(ResourceTag tag) {
        return getAllResourcesWithTag(tag).getResources().stream().reduce(0, (i, r) -> i + r.getAmount(), Integer::sum);
    }

    public ResourcePack getResourcesWithTagPart(ResourceTag tag, int amount) {
        ResourcePack _r = getAllResourcesWithTag(tag);
        ResourcePack result = new ResourcePack();
        int counter = 0;
        for (Resource resource : _r.getResources()) {
            if (counter >= amount) {
                break;
            }
            result.add(resource);
            counter += resource.getAmount();
        }
        return result;
    }

    /**
     * @param resource Resource which will be looked up.
     * @return Amount of all instances of this Resource in the Pack.
     */
    public int getAmountOfResource(Resource resource) {
        return getResource(resource).getResources().stream().reduce(0, (i, r) -> i += r.getAmount(), Integer::sum);
    }

    public ResourcePack getResourcePart(Resource resource, int ceiling) {
        Collection<Resource> _r = getResource(resource).getResources();
        ResourcePack resourcePack = new ResourcePack();
        int counter = 0;
        for (Resource res : _r) {
            if (counter >= ceiling) {
                break;
            }
            resourcePack.add(res.getCleanPart(ceiling - counter));
            counter += res.getAmount();
        }
        return resourcePack;
    }

    public ShnyPair<Integer, List<Resource>> getAmountOfResourcesWithTagAndErase(ResourceTag tag, int amount) {
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
        resources.removeAll(result);
        return new ShnyPair<>(counter, result);
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
        if (tile == null) {
            int i = 0;
        }
        resources.stream().filter(resource -> resource.getGenome().isMovable()).forEach(tile::addDelayedResource);
        resources.clear();
    }

    /**
     * @return the amount of all resources in pack.
     */
    public int getAmount() {
        return resources.stream().reduce(0, (i, r2) -> i + r2.getAmount(), Integer::sum);
    }

    public void add(Resource resource) {
        if (resource.getAmount() == 0) {
            return;
        }
        int i = -1;
        for (int j = 0; j < resources.size(); j++) {
            if (resources.get(j).fullEquals(resource)) {
                i = j;
                break;
            }
        }
        if (i == -1) {
            resources.add(resource);
            return;
        }
        resources.get(i).merge(resource);
    }

    /**
     * Adds all Resources from a ResourcePack.
     * //TODO will source be affected?
     * @param resourcePack Pack from which all Resources will be added.
     */
    public void add(ResourcePack resourcePack) {
        resourcePack.resources.forEach(this::add);
    }

    public void add(Collection<Resource> resources) {
        resources.forEach(this::add);
    }

    public void remove(Resource resource) {
        for (int i = 0; i < resources.size(); i++) {
            Resource res = resources.get(i);
            if (res.fullEquals(resource)) {
                resources.remove(i);
                return;
            }
        }
    }

    public void removeAll(Collection<Resource> resources) {
        resources.forEach(this::remove);
    }

    public void destroyAllResourcesWithTag(ResourceTag tag) {
        ResourcePack result = getAllResourcesWithTag(tag);
        resources.removeAll(result.resources);
        result.resources.forEach(resource -> resource.amount = 0);
    }

    @Override
    public String toString() {
        resources.removeIf(resource -> resource.getAmount() == 0);
        StringBuilder stringBuilder = new StringBuilder();
        for (Resource resource : resources) {
            stringBuilder.append(resource.getFullName()).append(" ")
                    .append(resource.getAmount())
                    .append("; ")
                    .append("\n");
        }
        return stringBuilder.toString();
    }
}
