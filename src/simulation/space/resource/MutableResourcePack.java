package simulation.space.resource;

import kotlin.Pair;
import simulation.space.Tile;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;

public class MutableResourcePack extends ResourcePack{ //TODO subclass which stores all instances of the same Resource on different Tiles

    public MutableResourcePack(Collection<Resource> resources) {
        super(resources);
    }

    public MutableResourcePack() {
        this(Collections.emptyList());
    }

    public ResourcePack getResourceAndRemove(Resource resource) {
        ResourcePack _r = getResource(resource);
        _r.getResources().forEach(this::remove);
        return _r;
    }

    public MutableResourcePack getResourcesWithTagPartIsBigger(ResourceTag tag, int ceiling) {
        return getPart(getAllResourcesWithTag(tag), ceiling);
    }

    public MutableResourcePack getResourcePart(Resource resource, int ceiling) {
        return getPart(getResource(resource), ceiling);
    }

    private MutableResourcePack getPart(ResourcePack pack, int amount) {
        MutableResourcePack result = new MutableResourcePack();
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

    public void disbandOnTile(Tile tile) {
        getResources().stream()
                .filter(resource -> resource.getGenome().isMovable())
                .forEach(tile::addDelayedResource);
        resourcesMap.clear();
    }

    public void add(Resource resource) {
        internalAdd(resource);
    }

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
