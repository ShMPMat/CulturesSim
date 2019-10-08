package simulation.space.resource;

import extra.ShnyPair;
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
    String meaningPostfix, legacyPostfix;
    int defaultAmount;

    private World world;
    private int efficiencyCoof;
    private boolean hasMeaning = false;
    private boolean isMovable = true;
    private boolean isTemplate = false;
    private boolean hasLegacy = false;
    private Genome genome;
    private List<Material> materials;
    private Map<Aspect, List<ShnyPair<Resource, Integer>>> aspectConversion;
    private Map<Aspect, String[]> _aspectConversion;
    private List<String> _parts;
    private List<AspectTag> tags, _tags;
    private Meme meaning;

    private int deathTime;

    ResourceCore(String[] tags, int efficiencyCoof, World world) {
        initializeMutualFields(tags[0], efficiencyCoof, world, 100, new ArrayList<>(), new ArrayList<>(),
                Integer.parseInt(tags[3]), Integer.parseInt(tags[4]) == 1,
                Integer.parseInt(tags[5]) == 1, "",
                new Genome(tags[0], new ArrayList<>(), Double.parseDouble(tags[2]), Double.parseDouble(tags[1]), false));
        for (int i = 6; i < tags.length; i++) {
            String tag = tags[i];
            switch ((tag.charAt(0))) {
                case '+':
                    this._aspectConversion.put(world.getAspectFromPoolByName(tag.substring(1, tag.indexOf(':'))),
                            tag.substring(tag.indexOf(':') + 1).split(","));
                    break;
                case '@':
                    if (tag.substring(1).equals("TEMPLATE")) {
                        isTemplate = true;
                        break;
                    }
                    materials.add(world.getMaterialFromPoolByName(tag.substring(1)));
                    break;
                case '^':
                    _parts.add(tag.substring(1));
                    break;
            }
        }
        computeMaterials();
    }

    private ResourceCore(String name, String meaningPostfix, World world, int defaultAmount, List<AspectTag> tags,
                         List<Material> materials, int efficiencyCoof, boolean isMovable,
                         boolean isTemplate, boolean hasLegacy, String legacy, int deathTime, Genome genome) {
        initializeMutualFields(name + meaningPostfix, efficiencyCoof, world, defaultAmount, tags, materials,
                deathTime, isMovable, hasLegacy, legacy, genome);
        this.isTemplate = isTemplate;
        computeMaterials();
    }

    private void initializeMutualFields(String name, int efficiencyCoof, World world, int defaultAmount,
                                        List<AspectTag> tags, List<Material> materials,
                                        int deathTime, boolean isMovable, boolean hasLegacy, String legacy,
                                        Genome genome) {
        this.deathTime = deathTime;
        this.isMovable = isMovable;
        this.meaning = null;
        this.efficiencyCoof = efficiencyCoof;
        this.aspectConversion = new HashMap<>();
        this._aspectConversion = new HashMap<>();
        this._parts = new ArrayList<>();
        this.world = world;
        this.defaultAmount = defaultAmount;
        this._tags = tags;
        this.tags = new ArrayList<>();
        this.materials = materials;
        this.hasLegacy = hasLegacy;
        this.genome = genome;
        setName(name, legacy);
    }

    void actualizeLinks() {
        for (String part : _parts) {
            Resource resource = world.getResourceFromPoolByName(part.split(":")[0]);
            resource = resource.resourceCore.hasLegacy ? resource.resourceCore.copyWithLegacyInsertion(this)
                    : resource;
            resource.amount = Integer.parseInt(part.split(":")[1]);
            genome.addPart(resource);
        }
        _parts.clear();
        if (!materials.isEmpty() && genome.getPrimaryMaterial() != null
                && !materials.get(0).equals(genome.getPrimaryMaterial())) {
            System.err.println("Genome-computed primary material differs from stated material in Resource " + genome.getName());
        }
        for (Aspect aspect : _aspectConversion.keySet()) {
            aspectConversion.put(aspect, Arrays.stream(_aspectConversion.get(aspect))
                    .map(s -> {
                        Resource resource = world.getResourceFromPoolByName(s.split(":")[0]);
                        return new ShnyPair<>(resource.resourceCore.hasLegacy ?
                                resource.resourceCore.copyWithLegacyInsertion(this) : resource,
                                Integer.parseInt(s.split(":")[1]));
                    }).collect(Collectors.toList()));
        }
        if (materials.isEmpty()) {
            return;
        }
        Material material = materials.get(0);
        if (material.hasPropertyWithName("_can_be_ignited") && !aspectConversion.containsKey(world.getAspectFromPoolByName("Incinerate"))) {
            List<ShnyPair<Resource, Integer>> resourceList = new ArrayList<>(2);
            resourceList.add(new ShnyPair<>(world.getResourceFromPoolByName("Fire"), 1));
            resourceList.add(new ShnyPair<>(world.getResourceFromPoolByName("Ash"), 1));
            aspectConversion.put(world.getAspectFromPoolByName("Incinerate"), resourceList);
        }
        if (genome.getSize() >= 0.5 && material.hasPropertyWithName("hard") && material.hasPropertyWithName("hard")
                && !aspectConversion.containsKey(world.getAspectFromPoolByName("BuildHouse"))) {
            aspectConversion.put(world.getAspectFromPoolByName("BuildHouse"),
                    Collections.singletonList(new ShnyPair<>(world.getResourceFromPoolByName("House"), 1)));
        }
        if (!genome.getParts().isEmpty() && !_aspectConversion.containsKey(world.getAspectFromPoolByName("TakeApart"))) {
            List<ShnyPair<Resource, Integer>> resourceList = new ArrayList<>();
            for (Resource resource : genome.getParts()) {
                resourceList.add(new ShnyPair<>(resource, resource.amount));
                aspectConversion.put(world.getAspectFromPoolByName("TakeApart"), resourceList);
            }
        }
        if (isMovable()) {
            aspectConversion.put(world.getAspectFromPoolByName("Take"),
                    Collections.singletonList(new ShnyPair<>(this.copy(), 1)));
        }
        _aspectConversion = null;
    }

    private void setName(String fullName, String legacy) {
        if (fullName.contains("_representing_")) {
            genome.setName(fullName.substring(0, fullName.indexOf("_representing_")));
            meaningPostfix = fullName.substring(fullName.indexOf("_representing_"));
        } else {
            genome.setName(fullName);
            meaningPostfix = "";
        }
        if (hasLegacy && legacy != null) {
            legacyPostfix = legacy;
        } else {
            legacyPostfix = "";
        }
    }

    private void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    public List<AspectTag> getTags() {
        return tags;
    }

    public int getDeathTime() {
        return deathTime;
    }

    public int getEfficiencyCoof() {
        return efficiencyCoof;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    double getSpreadProbability() {
        return genome.getSpreadProbability();
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

    private void computeMaterials() {
        if (materials.isEmpty() && !isTemplate) {
            System.err.println("Resource " + genome.getName() + " has no materials.");
        } else if (!materials.isEmpty()){
            this.tags.addAll(_tags);
            this.tags.addAll(materials.get(0).getTags(this));
        }
    }

    Resource copy() {
        return new Resource(this);
    }

    Resource copyWithLegacyInsertion(ResourceCore creator) {
        return new Resource(new ResourceCore(genome.getName(), meaningPostfix, world, defaultAmount,
                new ArrayList<>(_tags), new ArrayList<>(materials), efficiencyCoof, isMovable(), isTemplate,
                hasLegacy, "_of_" + creator.genome.getName() + creator.legacyPostfix, deathTime, genome));//TODO is legacy passed to parts in genome?
    }

    Resource copy(int amount) {
        return new Resource(this, amount);
    }

    Resource fullCopy() {
        return new Resource(new ResourceCore(genome.getName(), meaningPostfix, world, defaultAmount,
                new ArrayList<>(_tags), new ArrayList<>(materials), efficiencyCoof, isMovable(), isTemplate,
                hasLegacy, legacyPostfix, deathTime, genome));
    }

    public ResourceCore insertMeaning(Meme meaning, Aspect aspect) {
        Genome genome = new Genome(this.genome);
        genome.setSpreadProbability(0);
        ResourceCore _r = new ResourceCore(genome.getName(), "_representing_" + meaning + "_with_" + aspect.getName(),
                world, defaultAmount, new ArrayList<>(_tags), new ArrayList<>(materials),
                efficiencyCoof, isMovable(), isTemplate, hasLegacy, legacyPostfix, deathTime, genome);
        _r.hasMeaning = true;
        _r.meaning = meaning;
        return _r;
    }

    List<Resource> applyAspect(Aspect aspect) {
        if (aspectConversion.containsKey(aspect)) {
            List<Resource> resourceList = aspectConversion.get(aspect).stream().map(pair -> pair.first.copy(pair.second))
                    .collect(Collectors.toList());
            resourceList.forEach(resource -> {
                if (resource.resourceCore.isTemplate) {//TODO links
                    resource.resourceCore = resource.resourceCore.fullCopy().resourceCore;
                    resource.resourceCore.materials.addAll(materials);
                    resource.resourceCore.computeMaterials();
                    //resource.resourceCore.setName(resource.resourceCore.name + name + resource.resourceCore.meaningPostfix,
                      //      resource.resourceCore);
                    resource.resourceCore.isTemplate = false;
                    resource.computeHash();
                }
            });
            return resourceList;
        }
        return Collections.singletonList(applyAspectToMaterials(aspect).copy(1));
    }

    private ResourceCore applyAspectToMaterials(Aspect aspect) {
        List<Material> newMaterials = materials.stream().map(material -> material.applyAspect(aspect))
                .collect(Collectors.toList());
        Genome genome = new Genome(this.genome);
        genome.setSpreadProbability(0);
        return new ResourceCore(genome.getName() + (newMaterials.equals(materials) ? "" : "_" + aspect.getName()), meaningPostfix,
                world, defaultAmount, new ArrayList<>(_tags), newMaterials, efficiencyCoof, isMovable(),
                isTemplate, hasLegacy, legacyPostfix, deathTime, genome);//TODO dangerous shit for genome
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
        return genome.getName().equals(resourceCore.genome.getName()) /*&& getTags().equals(resourceCore.getTags())*/;
    }

    @Override
    public int hashCode() {
        return Objects.hash(genome.getName());
    }

    public double getSize() {
        return genome.getSize();
    }

    public String getBaseName() {
        return genome.getName();
    }
}
