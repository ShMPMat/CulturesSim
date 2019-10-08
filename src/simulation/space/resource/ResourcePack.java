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
    public List<Resource> resources;

    public ResourcePack() {
        resources = new ArrayList<>();
    }

    public void add(Resource resource) {
        if (resource.amount == 0) {
            return;
        }
        if (!resource.isMovable()) {
            int i = 0;
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

    public Collection<Resource> getAllResourcesWithTag(AspectTag tag) {
        return resources.stream().filter(resource -> resource.getTags().contains(tag)).collect(Collectors.toList());
    }

    public Collection<Resource> getResource(Resource resource) {
        List<Resource> _r = resources.stream().filter(res -> res.fullEquals(resource)).collect(Collectors.toList());
        return _r.size() == 0 ? Collections.singleton(resource.cleanCopy(0)) : _r;
    }

    public Collection<Resource> removeAllResourcesWithTag(AspectTag tag) {
        Collection<Resource> result = getAllResourcesWithTag(tag);
        resources.removeAll(result);
        return result;
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

    public Collection<Resource> getResourceAndRemove(Resource resource) {
        Collection<Resource> _r = getResource(resource);
        _r.forEach(this::removeResource);
        return _r;
    }

    public int getAmountOfResourcesWithTag(AspectTag tag) {
        return getAllResourcesWithTag(tag).stream().reduce(0, (i, r) -> i + r.amount, Integer::sum);
    }

    public int getAmountOfResource(Resource resource) {
        return getResource(resource).stream().reduce(0, (i, r) -> i += r.amount, Integer::sum);
    }

    public ResourcePack getResourcePart(Resource resource, int ceiling) {//TODO give only part
        Collection<Resource> _r = getResource(resource);
        ResourcePack resourcePack = new ResourcePack();
        int counter = 0;
        for (Resource res: _r) {
            if (counter >= ceiling) {
                break;
            }
            resourcePack.add(res);
            counter += res.amount;
            removeResource(res);
        }
        return resourcePack;
    }

    public int getAmountOfResourcesWithTagAndRemove(AspectTag tag, int amount) {
        Collection<Resource> _r = getAllResourcesWithTag(tag), result = new ArrayList<>();
        int counter = 0;
        for (Resource resource : _r) {
            if (counter >= amount) {
                break;
            }
            result.add(resource);
            counter += resource.amount;
        }
        resources.removeAll(result);
        return counter;
    }

    public boolean isEmpty() {
        return resources.stream().noneMatch(resource -> resource.amount > 0);
    }

    public void disbandOnTile(Tile tile) {
        resources.stream().filter(resource -> resource.getTile() == null).forEach(tile::addResource);
        resources.clear();
    }

    public int getAmountOfResources() {
        return resources.stream().reduce(0, (i, r2) -> i + r2.amount, Integer::sum);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Resource resource : resources) {
            stringBuilder.append(resource.getFullName()).append(" ").append(resource.amount).append(" ")
                    .append(resource.getTile() != null ? resource.getTile().x + " " + resource.getTile().y : "")
                    .append("\n");
        }
        return stringBuilder.toString();
    }
}
