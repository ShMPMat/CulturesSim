package simulation.culture.aspect;

import extra.ShnyPair;
import simulation.space.resource.ResourcePool;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourceCore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AspectMatcher {
    private AspectCore core;
    private List<String> neededProperties = new ArrayList<>();
    private List<String> badProperties = new ArrayList<>();
    private List<ShnyPair<String, Integer>> results = new ArrayList<>();
    private boolean isMovable = false;
    private double size = 0;

    AspectMatcher(String[] tags, AspectCore core) {
        this.core = core;
        for (String tag: tags) {
            switch (tag.charAt(0)) {
                case '@':
                    neededProperties.add(tag.substring(1));
                    break;
                case '!':
                    badProperties.add(tag.substring(1));
                    break;
                case '#':
                    String[] temp = tag.substring(1).split(":");
                    results.add(new ShnyPair<>(temp[0], Integer.parseInt(temp[1])));
                    break;
                case 'i':
                    if (tag.equals("isMovable")) {
                        isMovable = true;
                    } else {
                        throw new RuntimeException("Wrong tag for matcher");
                    }
                    break;
                case 's':
                    size = Double.parseDouble(tag.substring(1));
                    break;
                default:
                    throw new RuntimeException("Wrong tag for matcher");
            }
        }
        if (results.isEmpty()) {
            throw new RuntimeException("Aspect matcher does nothing");
        }
    }

    public boolean match(ResourceCore core) {
        if (core.getAspectConversion().keySet().stream().map(Aspect::getName)
                .anyMatch(name -> name.equals(this.core.name))) {
            return false;
        }
        if (neededProperties.stream().anyMatch(property -> !core.getMainMaterial().hasPropertyWithName(property))) {
            return false;
        }
        if (badProperties.stream().anyMatch(property -> core.getMainMaterial().hasPropertyWithName(property))) {
            return false;
        }
        if (isMovable && !core.isMovable()) {
            return false;
        }
        if (core.getSize() < size) {
            return false;
        }

        return true;
    }

    public List<ShnyPair<Resource, Integer>> getResults(Resource resource, ResourcePool resourcePool) {
        return results.stream().map(pair -> new ShnyPair<>(pair.first.equals("MATCHED") ?
                resource : resourcePool.get(pair.first), pair.second)).collect(Collectors.toList());
    }
}
