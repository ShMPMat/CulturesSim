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
    private String meaningPostfix;
    private boolean hasMeaning = false;
    private Genome genome;
    private List<Material> materials;
    private Map<Aspect, List<ShnyPair<Resource, Integer>>> aspectConversion;
    private Map<Aspect, String[]> _aspectConversion;
    private List<String> _parts;
    private List<AspectTag> tags, _tags;
    private Meme meaning;

    ResourceCore(String[] tags, int efficiencyCoof, World world) {
        initializeMutualFields(tags[0], new ArrayList<>(), new ArrayList<>(),
                new Genome(tags[0], Double.parseDouble(tags[2]), Double.parseDouble(tags[1]),
                        false, Integer.parseInt(tags[4]) == 1, false,
                        Integer.parseInt(tags[5]) == 1, Integer.parseInt(tags[3]), 100, efficiencyCoof, null, world),
                new HashMap<>());
        for (int i = 6; i < tags.length; i++) {
            String tag = tags[i];
            switch ((tag.charAt(0))) {
                case '+':
                    this._aspectConversion.put(world.getAspectFromPoolByName(tag.substring(1, tag.indexOf(':'))),
                            tag.substring(tag.indexOf(':') + 1).split(","));
                    break;
                case '@':
                    if (tag.substring(1).equals("TEMPLATE")) {
                        genome.setTemplate(true);
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

    private ResourceCore(String name, String meaningPostfix, List<AspectTag> tags, List<Material> materials, Genome genome,
                         Map<Aspect, List<ShnyPair<Resource, Integer>>> aspectConversion) {
        initializeMutualFields(name + meaningPostfix, tags, materials, genome, aspectConversion);
        computeMaterials();
    }

    private void replaceLinks() {
        for (List<ShnyPair<Resource, Integer>> resources: aspectConversion.values()) {
            for (ShnyPair<Resource, Integer> resource : resources) {
                if (resource.first.getSimpleName().equals(this.getSimpleName())) {
                    resource.first = this.copy();
                }
            }
        }
    }

    private void initializeMutualFields(String name, List<AspectTag> tags, List<Material> materials, Genome genome,
                                        Map<Aspect, List<ShnyPair<Resource, Integer>>> aspectConversion) {
        this.meaning = null;
        this.aspectConversion = new HashMap<>(aspectConversion);
        this._aspectConversion = new HashMap<>();
        this._parts = new ArrayList<>();
        this._tags = tags;
        this.tags = new ArrayList<>();
        this.materials = materials;
        this.genome = genome;
        setName(name);
    }

    void actualizeLinks() {
        for (Aspect aspect : _aspectConversion.keySet()) {
            aspectConversion.put(aspect, Arrays.stream(_aspectConversion.get(aspect))
                    .map(this::readConversion).collect(Collectors.toList()));
        }
        if (materials.isEmpty()) {
            return;
        }
        Material material = materials.get(0);
        if (material.hasPropertyWithName("_can_be_ignited") && !aspectConversion.containsKey(genome.world.getAspectFromPoolByName("Incinerate"))) {
            List<ShnyPair<Resource, Integer>> resourceList = new ArrayList<>(2);
            resourceList.add(new ShnyPair<>(genome.world.getResourceFromPoolByName("Fire"), 1));
            resourceList.add(new ShnyPair<>(genome.world.getResourceFromPoolByName("Ash"), 1));
            aspectConversion.put(genome.world.getAspectFromPoolByName("Incinerate"), resourceList);
        }
        if (genome.getSize() >= 0.5 && material.hasPropertyWithName("hard") && material.hasPropertyWithName("hard")
                && !aspectConversion.containsKey(genome.world.getAspectFromPoolByName("BuildHouse"))) {
            aspectConversion.put(genome.world.getAspectFromPoolByName("BuildHouse"),
                    Collections.singletonList(new ShnyPair<>(genome.world.getResourceFromPoolByName("House"), 1)));
        }
        if (isMovable()) {
            aspectConversion.put(genome.world.getAspectFromPoolByName("Take"),
                    Collections.singletonList(new ShnyPair<>(this.copy(), 1)));
        }
    }

    void actualizeParts() {
        for (String part : _parts) {
            Resource resource = genome.world.getResourceFromPoolByName(part.split(":")[0]);
            resource = resource.resourceCore.genome.hasLegacy() ? resource.resourceCore.copyWithLegacyInsertion(this)
                    : resource;
            resource.amount = Integer.parseInt(part.split(":")[1]);
            genome.addPart(resource);
        }
        _parts.clear();
        if (!materials.isEmpty() && genome.getPrimaryMaterial() != null
                && !materials.get(0).equals(genome.getPrimaryMaterial())) {
            System.err.println("Genome-computed primary material differs from stated material in Resource " + genome.getName());
        }
        if (!genome.getParts().isEmpty() && !_aspectConversion.containsKey(genome.world.getAspectFromPoolByName("TakeApart"))) {
            List<ShnyPair<Resource, Integer>> resourceList = new ArrayList<>();
            for (Resource resource : genome.getParts()) {
                resourceList.add(new ShnyPair<>(resource, resource.amount));
                aspectConversion.put(genome.world.getAspectFromPoolByName("TakeApart"), resourceList);
            }
        }
        _aspectConversion = null;
    }

    private ShnyPair<Resource, Integer> readConversion(String s) {
        if (s.split(":")[0].equals("LEGACY")) {
            if (genome.getLegacy() == null) {
                //System.err.println("No legacy for LEGACY conversion in genome " + genome.getName());
                return new ShnyPair<>(null, Integer.parseInt(s.split(":")[1]));//TODO insert legacy in another place
            }
            Resource resource = genome.getLegacy().copy();
            return new ShnyPair<>(resource.resourceCore.genome.hasLegacy() ?
                    resource.resourceCore.copyWithLegacyInsertion(this) : resource,
                    Integer.parseInt(s.split(":")[1]));
        }
        Resource resource = genome.world.getResourceFromPoolByName(s.split(":")[0]);
        return new ShnyPair<>(resource.resourceCore.genome.hasLegacy() ?
                resource.resourceCore.copyWithLegacyInsertion(this) : resource,
                Integer.parseInt(s.split(":")[1]));//TODO insert amount in Resource amount;
    }

    private void setName(String fullName) {//TODO remove meaning postfix
        if (fullName.contains("_representing_")) {
            genome.setName(fullName.substring(0, fullName.indexOf("_representing_")));
            meaningPostfix = fullName.substring(fullName.indexOf("_representing_"));
        } else {
            genome.setName(fullName);
            meaningPostfix = "";
        }
    }

    private void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    private void computeMaterials() {
        if (materials.isEmpty() && !genome.isTemplate()) {
            System.err.println("Resource " + genome.getName() + " has no materials.");
        } else if (!materials.isEmpty()){
            this.tags.addAll(_tags);
            this.tags.addAll(materials.get(0).getTags(this));
        }
    }

    public List<AspectTag> getTags() {
        return tags;
    }

    public int getDeathTime() {
        return genome.getDeathTime();
    }

    public int getDefaultAmount() {
        return genome.getDefaultAmount();
    }

    public Genome getGenome() {
        return genome;
    }

    public int getEfficiencyCoof() {
        return genome.getEfficiencyCoof();
    }

    public List<Material> getMaterials() {
        return materials;
    }

    double getSpreadProbability() {
        return genome.getSpreadProbability();
    }

    String getLegacyPostfix() {
        return genome.getLegacyPostfix();
    }

    String getMeaningPostfix() {
        return meaningPostfix;
    }

    public double getSize() {
        return genome.getSize();
    }

    public String getBaseName() {
        return genome.hasLegacy() ? genome.getName() + getLegacyPostfix() : genome.getName();
    }

    public String getSimpleName() {
        return genome.getName();
    }

    boolean isHasMeaning() {
        return hasMeaning;
    }

    void setHasMeaning(boolean b) {
        hasMeaning = b;
    }

    boolean isMovable() {
        return genome.isMovable();
    }

    void setLegacy(ResourceCore legacy) {
        genome.setLegacy(legacy);

        if (_aspectConversion != null) {
            for (Aspect aspect : _aspectConversion.keySet()) {
                if (Arrays.stream(_aspectConversion.get(aspect)).anyMatch(s -> s.split(":")[0].equals("LEGACY"))) {
                    aspectConversion.put(aspect, Arrays.stream(_aspectConversion.get(aspect))
                            .map(this::readConversion).collect(Collectors.toList()));
                }
            }
        } else {
            for (Aspect aspect : aspectConversion.keySet()) {
                for (ShnyPair<Resource, Integer> pair : aspectConversion.get(aspect)) {
                    if (pair.first == null) {
                        pair.first = legacy.fullCopy();
                    }
                }
            }
        }
        replaceLinks();
    }

    Resource copy() {
        return new Resource(this);
    }

    Resource copyWithLegacyInsertion(ResourceCore creator) {
        Resource resource = new Resource(new ResourceCore(genome.getName(), meaningPostfix, new ArrayList<>(_tags),
                new ArrayList<>(materials), new Genome(genome), aspectConversion));
        resource.resourceCore._aspectConversion = _aspectConversion;
        resource.resourceCore.setLegacy(creator);
        return resource;//TODO is legacy passed to parts in genome?
    }

    Resource copy(int amount) {
        return new Resource(this, amount);
    }

    Resource fullCopy() {
        return new Resource(new ResourceCore(genome.getName(), meaningPostfix,
                new ArrayList<>(_tags), new ArrayList<>(materials), new Genome(genome), aspectConversion));
    }

    public ResourceCore insertMeaning(Meme meaning, Aspect aspect) {
        Genome genome = new Genome(this.genome);
        genome.setSpreadProbability(0);
        ResourceCore _r = new ResourceCore(genome.getName(), "_representing_" + meaning + "_with_" + aspect.getName(),
                new ArrayList<>(_tags), new ArrayList<>(materials), genome, aspectConversion);
        _r.hasMeaning = true;
        _r.meaning = meaning;
        return _r;
    }

    List<Resource> applyAspect(Aspect aspect) {
        if (aspectConversion.containsKey(aspect)) {
            if (aspectConversion.get(aspect).stream().anyMatch(resourceIntegerShnyPair -> resourceIntegerShnyPair.first == null)) {
                int i = 0;
            }
            List<Resource> resourceList = aspectConversion.get(aspect).stream().map(pair -> pair.first.copy(pair.second))
                    .collect(Collectors.toList());
            resourceList.forEach(resource -> {
                if (resource.resourceCore.genome.isTemplate()) {//TODO links; legacy seems to leak from House
                    resource.resourceCore = resource.resourceCore.fullCopy().resourceCore;
                    resource.resourceCore.materials.addAll(materials);
                    resource.resourceCore.computeMaterials();
                    //resource.resourceCore.setName(resource.resourceCore.name + name + resource.resourceCore.meaningPostfix,
                      //      resource.resourceCore);
                    resource.resourceCore.genome.setTemplate(false);
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
        return new ResourceCore(genome.getName() + (newMaterials.equals(materials) ? "" : "_" + aspect.getName()),
                meaningPostfix, new ArrayList<>(_tags), newMaterials, genome, aspectConversion);//TODO dangerous shit for genome
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
        return getBaseName().equals(resourceCore.getBaseName()) /*&& getTags().equals(resourceCore.getTags())*/;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBaseName());
    }

    @Override
    public String toString() {
        return getBaseName();
    }
}
