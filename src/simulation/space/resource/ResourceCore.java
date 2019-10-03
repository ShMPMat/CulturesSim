package simulation.space.resource;

import simulation.World;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.thinking.meaning.Meme;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class which contains all general information about all Resources with the same name.
 */
public class ResourceCore {
    String name, meaningPrefix;
    double defaultSpreadability;
    int defaultAmount;
    World world;
    int efficiencyCoof;

    private boolean hasMeaning = false;
    private boolean isMovable = true;
    private double size;
    private List<Material> materials;
    private Map<Aspect, List<Resource>> aspectConversion;
    private Map<Aspect, String[]> _aspectConversion;
    private List<AspectTag> tags, _tags;

    ResourceCore(String[] tags, int efficiencyCoof, World world) {
        this.efficiencyCoof = efficiencyCoof;
        this.aspectConversion = new HashMap<>();
        this._aspectConversion = new HashMap<>();
        this.world = world;
        this.defaultAmount = 100;
        this.tags = new ArrayList<>();
        this._tags = new ArrayList<>();
        this.materials = new ArrayList<>();
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            if (i == 0) {
                setName(tag);
                this.defaultSpreadability = Double.parseDouble(tags[i + 1]);
                size = Double.parseDouble(tags[i + 2]);
                if (size <= 0.05) {
                    this._tags.add(new AspectTag("small"));
                }
                isMovable = Integer.parseInt(tags[i + 3]) == 1;
                i += 3;
            } else {
                switch ((tag.charAt(0))) {
                    case '+':
                        this._aspectConversion.put(world.getAspectFromPoolByName(tag.substring(1, tag.indexOf(':'))),
                                tag.substring(tag.indexOf(':') + 1).split(","));
                        break;
                    case '@':
                        materials.add(world.getMaterialFromPoolByName(tag.substring(1)));
                }

            }
        }
        if (materials.isEmpty()) {
            System.err.println("Resource " + name + " has no materials.");
        } else {
            this.tags.addAll(_tags);
            this.tags.addAll(materials.get(0).getTags(this));
        }
    }

    private ResourceCore(String name, String meaningPrefix, World world, int defaultAmount, double defaultSpreadability, List<AspectTag> tags,
                         List<Material> materials, int efficiencyCoof, double size, boolean isMovable) {
        this.isMovable = isMovable;
        this.efficiencyCoof = efficiencyCoof;
        setName(name + meaningPrefix);
        this.defaultSpreadability = defaultSpreadability;
        this.size = size;
        this.world = world;
        this.defaultAmount = defaultAmount;
        this._tags = tags;
        this.tags = new ArrayList<>();
        this.materials = materials;
        this.aspectConversion = new HashMap<>();
        this._aspectConversion = new HashMap<>();
        if (materials.isEmpty()) {
            System.err.println("Resource " + name + " has no materials.");
        } else {
            this.tags.addAll(_tags);
            this.tags.addAll(materials.get(0).getTags(this));
        }
    }

    public void actualizeLinks() {
        for (Aspect aspect : _aspectConversion.keySet()) {
            aspectConversion.put(aspect, Arrays.stream(_aspectConversion.get(aspect))
                    .map(s -> world.getResourceFromPoolByName(s)).collect(Collectors.toList()));
        }
        _aspectConversion = null;
    }

    private void setName(String fullName) {
        if (fullName.contains("_representing_")) {
            name = fullName.substring(0, fullName.indexOf("_representing_"));
            meaningPrefix = fullName.substring(fullName.indexOf("_representing_"));
        } else {
            name = fullName;
            meaningPrefix = "";
        }
    }

    Resource copy() {
        return new Resource(this);
    }

    Resource copy(int amount) {
        return new Resource(this, amount);
    }

    Resource fullCopy() {
        return new Resource(new ResourceCore(name, meaningPrefix, world, defaultAmount, 0,
                new ArrayList<>(_tags), new ArrayList<>(materials), efficiencyCoof, size, isMovable()));
    }

    public ResourceCore insertMeaning(Meme meaning, Aspect aspect) { //TODO insert meaning in some list or smth
        ResourceCore _r = new ResourceCore(name, "_representing_" + meaning + "_with_" + aspect.getName(),
                world, defaultAmount, 0, new ArrayList<>(_tags), new ArrayList<>(materials),
                efficiencyCoof, size, isMovable());
        _r.hasMeaning = true;
        return _r;
    }

    List<Resource> applyAspect(Aspect aspect) {
        if (aspectConversion.containsKey(aspect)) {
            return aspectConversion.get(aspect).stream().map(resource -> resource.copy(1)).collect(Collectors.toList());
        }
        return Collections.singletonList(applyAspectToMaterials(aspect).copy(1));
    }

    private ResourceCore applyAspectToMaterials(Aspect aspect) {
        List<Material> newMaterials = materials.stream().map(material -> material.applyAspect(aspect))
                .collect(Collectors.toList());
        return new ResourceCore(name + (newMaterials.equals(materials) ? "" : "_" + aspect.getName()), meaningPrefix,
                world, defaultAmount, 0, new ArrayList<>(_tags), newMaterials, efficiencyCoof, size, isMovable());
    }

    public List<AspectTag> getTags() {
        return tags;
    }

    public boolean hasApplicationForAspect(Aspect aspect) {
        if (aspect.canApplyMeaning()) {
            return getTags().containsAll(aspect.getRequirements());
        }
        return aspectConversion.containsKey(aspect) || materials.stream()
                .anyMatch(material -> material.hasApplicationForAspect(aspect));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        ResourceCore resourceCore = (ResourceCore) o;
        return name.equals(resourceCore.name) && getTags().equals(resourceCore.getTags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    boolean isHasMeaning() {
        return hasMeaning;
    }

    void setHasMeaning(boolean b) {
        hasMeaning = b;
    }

    boolean isMovable() {
        return isMovable;
    }
}
