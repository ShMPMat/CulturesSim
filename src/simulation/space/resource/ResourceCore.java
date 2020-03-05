package simulation.space.resource;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectMatcher;
import simulation.culture.aspect.AspectResult;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.Tile;
import simulation.space.resource.dependency.AvoidTiles;
import simulation.space.resource.dependency.ConsumeDependency;
import simulation.space.resource.dependency.ResourceNeedDependency;

import java.util.*;
import java.util.stream.Collectors;

import static simulation.Controller.session;

/**
 * Class which contains all general information about all Resources with the same name.
 */
public class ResourceCore {
    private String meaningPostfix = "";
    private boolean hasMeaning = false;
    private Genome genome;
    private List<Material> materials;
    private Map<Aspect, List<ShnyPair<Resource, Integer>>> aspectConversion;
    private Map<Aspect, String[]> _aspectConversion;
    private List<String> _parts;
    private List<ResourceTag> tags;
    private Meme meaning;

    ResourceCore(String[] tags) {
        initializeMutualFields(tags[0], new ArrayList<>(),
                new Genome(tags[0], Genome.Type.valueOf(tags[11]), Double.parseDouble(tags[2]),
                        Double.parseDouble(tags[1]), Integer.parseInt(tags[4]), Integer.parseInt(tags[5]),
                        Integer.parseInt(tags[7]), tags[9].equals("1"), false, tags[8].equals("1"),
                        tags[9].equals("1"), Integer.parseInt(tags[3]), Integer.parseInt(tags[6]),
                        null, null, null),
                new HashMap<>(), null);
        String[] elements;
        for (int i = 12; i < tags.length; i++) {
            String tag = tags[i];
            switch ((tag.charAt(0))) {
                case '+':
                    this._aspectConversion.put(session.world.getPoolAspect(tag.substring(1, tag.indexOf(':'))),
                            tag.substring(tag.indexOf(':') + 1).split(","));
                    break;
                case '@':
                    if (tag.substring(1).equals("TEMPLATE")) {
                        genome = new GenomeTemplate(genome);
                        break;
                    }
                    materials.add(session.world.getPoolMaterial(tag.substring(1)));
                    genome.setPrimaryMaterial(materials.get(0));
                    break;
                case '^':
                    _parts.add(tag.substring(1));
                    break;
                case '~':
                    elements = tag.split(":");
                    if (elements[4].equals("CONSUME")) {
                        genome.addDependency(new ConsumeDependency(Double.parseDouble(elements[2]),
                                elements[3].equals("1"), Double.parseDouble(elements[1]),
                                Arrays.asList(elements[0].substring(1).split(","))));
                    } else {
                        genome.addDependency(new ResourceNeedDependency(
                                ResourceNeedDependency.Type.valueOf(elements[4]), Double.parseDouble(elements[1]),
                                Double.parseDouble(elements[2]), elements[3].equals("1"), Arrays.asList(elements[0].substring(1).split(","))));
                    }
                    break;
                case '#':
                    genome.addDependency(new AvoidTiles(Arrays.stream(tag.substring(1).split(":"))
                            .map(Tile.Type::valueOf).collect(Collectors.toSet())));
                    break;
                case '$':
                    elements = tag.split(":");
                    genome.addAspectTag(new ResourceTag(elements[0].substring(1), Integer.parseInt(elements[1])));
                    break;
                case 'R':
                    genome.setWillResist(true);
                    break;
            }
        }
        computeMaterials();
    }

    private ResourceCore(String name, String meaningPostfix, List<Material> materials, Genome genome,
                         Map<Aspect, List<ShnyPair<Resource, Integer>>> aspectConversion, Meme meaning) {
        initializeMutualFields(name + meaningPostfix, materials, genome, aspectConversion, meaning);
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

    private void initializeMutualFields(String name, List<Material> materials, Genome genome,
                                        Map<Aspect, List<ShnyPair<Resource, Integer>>> aspectConversion, Meme meaning) {
        this.meaning = meaning;
        this.aspectConversion = new HashMap<>(aspectConversion);
        this._aspectConversion = new HashMap<>();
        this._parts = new ArrayList<>();
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
        Material material = getMainMaterial();
        for (Aspect aspect: session.world.aspectPool) {
            for (AspectMatcher matcher: aspect.getMatchers()) {
                if (matcher.match(this)) {
                    addAspectConversion(aspect.getName(), matcher.getResults(copy()));
                }
            }
        }
    }

    public void addAspectConversion(String aspectName, List<ShnyPair<Resource, Integer>> resourceList) {
        aspectConversion.put(session.world.getPoolAspect(aspectName), resourceList);
    }

    void actualizeParts() {
        for (String part : _parts) {
            Resource resource = session.world.getPoolResource(part.split(":")[0]);
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
        if (!genome.getParts().isEmpty() && !_aspectConversion.containsKey(session.world.getPoolAspect("TakeApart"))) {
            List<ShnyPair<Resource, Integer>> resourceList = new ArrayList<>();
            for (Resource resource : genome.getParts()) {
                resourceList.add(new ShnyPair<>(resource, resource.amount));
                aspectConversion.put(session.world.getPoolAspect("TakeApart"), resourceList);
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
        Resource resource = session.world.getPoolResource(s.split(":")[0]);
        ShnyPair<Resource, Integer> pair = new ShnyPair<>(resource.resourceCore.genome.hasLegacy() ?
                resource.resourceCore.copyWithLegacyInsertion(this) : resource,
                Integer.parseInt(s.split(":")[1]));//TODO insert amount in Resource amount;
        pair.first.resourceCore._aspectConversion = new HashMap<>(resource.resourceCore._aspectConversion);
        pair.first.resourceCore.actualizeLinks();
        return pair;
    }

    public Map<Aspect, List<ShnyPair<Resource, Integer>>> getAspectConversion() {
        return aspectConversion;
    }

    private void setName(String fullName) {
        if (fullName.contains("_representing_")) {
            genome.setName(fullName.substring(0, fullName.indexOf("_representing_")));
            meaningPostfix = fullName.substring(fullName.indexOf("_representing_"));
        } else {
            genome.setName(fullName);
        }
    }

    private void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    private void computeMaterials() {
        if (materials.isEmpty() && !(genome instanceof GenomeTemplate)) {//TODO it happens in initialization
            System.err.println("Resource " + genome.getName() + " has no materials.");
//            throw new ExceptionInInitializerError("Resource " + genome.getName() + " has no materials.");
        } else if (!materials.isEmpty()){
            tags.addAll(genome.getTags());
            tags.addAll(materials.get(0).getTags(this));
        }
    }

    public List<ResourceTag> getTags() {
        return tags;
    }

    public Material getMainMaterial() {
        return materials.get(0);
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

    public boolean isMovable() {
        return genome.isMovable();
    }

    boolean containsTag(ResourceTag tag) {
        return tags.contains(tag);
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

    void setHasMeaning(boolean b) {
        hasMeaning = b;
    }

    Resource copy() {
        return new Resource(this);
    }

    Resource copyWithLegacyInsertion(ResourceCore creator) {//TODO WHAT IS IT?
        Resource resource = new Resource(new ResourceCore(genome.getName(), meaningPostfix, new ArrayList<>(materials),
                new Genome(genome), aspectConversion, meaning));
        resource.resourceCore._aspectConversion = _aspectConversion;
        resource.resourceCore.setLegacy(creator);
        return resource;//TODO is legacy passed to parts in genome?
    }

    Resource copy(int amount) {
        return new Resource(this, amount);
    }

    Resource fullCopy() {
        if (genome instanceof GenomeTemplate) {
            throw new ExceptionInInitializerError("Cant make a full copy of a template");//TODO normal exception
        }
        return new Resource(new ResourceCore(genome.getName(), meaningPostfix, new ArrayList<>(materials),
                new Genome(genome), aspectConversion, meaning));
    }

    private ResourceCore instantiateTemplateCopy(ResourceCore legacy) {
        if (!(genome instanceof GenomeTemplate)) {
            throw new ExceptionInInitializerError("Cant make a instantiated copy not from a template");//TODO normal exception
        }
        ResourceCore resourceCore = new ResourceCore(genome.getName(), meaningPostfix, new ArrayList<>(legacy.materials),
                ((GenomeTemplate) genome).getInstantiatedGenome(legacy), aspectConversion, meaning);
        resourceCore.materials.addAll(legacy.materials);
        resourceCore.computeMaterials();
        return resourceCore;
    }

    public ResourceCore insertMeaning(Meme meaning, AspectResult result) {
        Genome genome = new Genome(this.genome);
        genome.setSpreadProbability(0);
        StringBuilder meaningPostfix = new StringBuilder("_representing_" + meaning + "_with_" +
                result.node.aspect.getName());
        if (result.node.resourceUsed.size() > 1) {
            List<String> names = result.node.resourceUsed.entrySet().stream()
                    .filter(entry -> !entry.getKey().name.equals(ResourceTag.phony().name))
                    .flatMap(entry -> entry.getValue().resources.stream().map(Resource::getFullName)).distinct()
                    .collect(Collectors.toList());
            meaningPostfix.append("(");
            for (String name: names) {
                meaningPostfix.append(name).append(", ");
            }
            meaningPostfix = new StringBuilder(meaningPostfix.substring(0, meaningPostfix.length() - 2) + ")");
        }
        ResourceCore _r = new ResourceCore(genome.getName(), meaningPostfix.toString(), new ArrayList<>(materials), genome,
                aspectConversion, meaning);
        _r.hasMeaning = true;
        return _r;
    }

    List<Resource> applyAspect(Aspect aspect) {
        if (aspectConversion.containsKey(aspect)) {
            List<Resource> resourceList = aspectConversion.get(aspect).stream()
                    .map(pair -> pair.first.copy(pair.second))
                    .collect(Collectors.toList());//TODO throw an exception on any attempt to copy template
            resourceList.forEach(resource -> {
                if (resource.resourceCore.genome instanceof GenomeTemplate) {//TODO links
                    resource.resourceCore = resource.resourceCore.instantiateTemplateCopy(this);
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
                meaningPostfix, newMaterials, genome, aspectConversion, meaning);//TODO dangerous shit for genome
    }

    public boolean hasApplicationForAspect(Aspect aspect) {
        if (aspect.getName().equals("Take") && !genome.willResist()) {
            return true;
        }
        if (getTags().containsAll(aspect.getWrapperRequirements()) && !aspect.getWrapperRequirements().isEmpty()) {
            return true;
        }
        return aspectConversion.containsKey(aspect) ||
                materials.stream().anyMatch(material -> material.hasApplicationForAspect(aspect));
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
