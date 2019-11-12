package simulation.space.resource;

import simulation.culture.aspect.AspectTag;
import simulation.space.Tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bunch of resources stored together.
 */
public class ResourcePack {
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

    public Collection<Resource> getResources() {
        return resources;
    }

    public ResourcePack getAllResourcesWithTag(AspectTag tag) {
        return new ResourcePack(resources.stream().filter(resource -> resource.getTags().contains(tag))
                .collect(Collectors.toList()));
    }

    public ResourcePack getResource(Resource resource) {
        List<Resource> _r = resources.stream().filter(res -> res.fullEquals(resource)).collect(Collectors.toList());
        return _r.size() == 0 ? new ResourcePack() : new ResourcePack(_r);
    }

    public ResourcePack getResourceAndRemove(Resource resource) {
        ResourcePack _r = getResource(resource);
        _r.resources.forEach(this::removeResource);
        return _r;
    }

    public int getAmountOfResourcesWithTag(AspectTag tag) {
        return getAllResourcesWithTag(tag).getResources().stream().reduce(0, (i, r) -> i + r.amount, Integer::sum);
    }

    public int getAmountOfResourcesWithTagPart(AspectTag tag, int amount) {
        Collection<Resource> _r = getAllResourcesWithTag(tag).getResources(), result = new ArrayList<>();
        int counter = 0;
        for (Resource resource : _r) {
            if (counter >= amount) {
                break;
            }
            result.add(resource);
            counter += resource.amount;
        }
        return counter;
    }

    public ResourcePack getResourcesWithTagPart(AspectTag tag, int amount) {
        ResourcePack _r = getAllResourcesWithTag(tag);
        ResourcePack result = new ResourcePack();
        int counter = 0;
        for (Resource resource : _r.getResources()) {
            if (counter >= amount) {
                break;
            }
            result.add(resource);
            counter += resource.amount;
        }
        return result;
    }

    public int getAmountOfResource(Resource resource) {
        return getResource(resource).getResources().stream().reduce(0, (i, r) -> i += r.amount, Integer::sum);
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
            counter += res.amount;
        }
        return resourcePack;
    }

    public int getAmountOfResourcesWithTagAndErase(AspectTag tag, int amount) {
        Collection<Resource> _r = getAllResourcesWithTag(tag).getResources(), result = new ArrayList<>();
        int counter = 0;
        for (Resource resource : _r) {
            if (counter >= amount) {
                break;
            }
            result.add(resource);
            counter += resource.amount;
        }
        resources.removeAll(result);
        result.forEach(resource -> resource.amount = 0);
        return counter;
    }

    /**
     * @return whether this ResourcePack has any amount of Resources.
     */
    public boolean isEmpty() {
        return getAmount() > 0;
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
        resources.stream().filter(resource -> resource.getTile() == null).forEach(tile::addDelayedResource);
        resources.clear();
    }

    /**
     * @return the amount of all resources in pack.
     */
    public int getAmount() {
        return resources.stream().reduce(0, (i, r2) -> i + r2.amount, Integer::sum);
    }

    public void add(Resource resource) {
        if (resource.amount == 0) {
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

    public void add(ResourcePack resourcePack) {
        resourcePack.resources.forEach(this::add);
    }

    public void add(Collection<Resource> resources) {
        resources.forEach(this::add);
    }

    public void removeResource(Resource resource) {
        for (int i = 0; i < resources.size(); i++) {
            Resource res = resources.get(i);
            if (res.fullEquals(resource)) {
                resources.remove(i);
                return;
            }
        }
    }

    public ResourcePack destroyAllResourcesWithTag(AspectTag tag) {
        ResourcePack result = getAllResourcesWithTag(tag);
        resources.removeAll(result.resources);
        result.resources.forEach(resource -> resource.amount = 0);
        return result;
    }

    @Override
    public String toString() {
        resources.removeIf(resource -> resource.amount == 0);
        StringBuilder stringBuilder = new StringBuilder();
        for (Resource resource : resources) {
            stringBuilder.append(resource.getFullName()).append(" ").append(resource.amount).append("; ")
                    .append(resource.getTile() != null ? resource.getTile().x + " " + resource.getTile().y : "")
                    .append("\n");
        }
        return stringBuilder.toString();
    }
}
