package simulation.space.resource;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectMatcher;
import simulation.culture.aspect.AspectResult;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.SpaceError;
import simulation.space.resource.material.Material;

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
    private List<ResourceTag> tags;
    private Meme meaning;

    ResourceCore(
            String name,
            String meaningPostfix,
            List<Material> materials,
            Genome genome,
            Map<Aspect, List<ShnyPair<Resource, Integer>>> aspectConversion,
            Meme meaning
    ) {
        this.meaning = meaning;
        this.aspectConversion = new HashMap<>(aspectConversion);
        this._aspectConversion = new HashMap<>();
        this.tags = new ArrayList<>();
        this.materials = materials;
        this.genome = genome;
        setName(name + meaningPostfix);
        computeTagsFromMaterials();
    }

    private void replaceLinks() {
        for (List<ShnyPair<Resource, Integer>> resources : aspectConversion.values()) {
            for (ShnyPair<Resource, Integer> resource : resources) {
                try {
                    if (resource.first == null) {
                        resource.first = genome.getLegacy().copy();
                    } else if (resource.first.getSimpleName().equals(genome.getName())) {
                        resource.first = this.copy();
                    }
                } catch (NullPointerException r) {
                    int i = 0;
                }
            }
        }
    }

    void addAspectConversion(String aspectName, List<ShnyPair<Resource, Integer>> resourceList) {
        aspectConversion.put(session.world.getAspectPool().get(aspectName), resourceList);
    }

    ShnyPair<Resource, Integer> readConversion(String s, ResourcePool resourcePool) {
        if (s.split(":")[0].equals("LEGACY")) {
            if (genome.getLegacy() == null) {
                //System.err.println("No legacy for LEGACY conversion in genome " + genome.getName());
                return new ShnyPair<>(null, Integer.parseInt(s.split(":")[1]));//TODO insert legacy in another place
            }
            Resource resource = genome.getLegacy().copy();
            return new ShnyPair<>(
                    resource.resourceCore.genome.hasLegacy()
                            ? resource.resourceCore.copyWithLegacyInsertion(this, resourcePool)
                            : resource,
                    Integer.parseInt(s.split(":")[1]));
        }
        Resource resource = resourcePool.get(s.split(":")[0]);
        ShnyPair<Resource, Integer> pair = new ShnyPair<>(resource.resourceCore.genome.hasLegacy() ?
                resource.resourceCore.copyWithLegacyInsertion(this, resourcePool) : resource,
                Integer.parseInt(s.split(":")[1]));//TODO insert amount in Resource amount;
        pair.first.resourceCore._aspectConversion = new HashMap<>(resource.resourceCore._aspectConversion);
        pair.first.resourceCore.actualizeLinks(resourcePool);
        return pair;
    }

    void actualizeLinks(ResourcePool resourcePool) {
        for (Aspect aspect : _aspectConversion.keySet()) {
            aspectConversion.put(aspect, Arrays.stream(_aspectConversion.get(aspect))
                    .map(s -> readConversion(s, resourcePool)).collect(Collectors.toList()));
        }
        if (materials.isEmpty()) {
            return;
        }
        for (Aspect aspect : session.world.getAspectPool().getAll()) {
            for (AspectMatcher matcher : aspect.getMatchers()) {
                if (matcher.match(this)) {
                    addAspectConversion(aspect.getName(), matcher.getResults(copy(), resourcePool));
                }
            }
        }
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

    private void computeTagsFromMaterials() {
        if (materials.isEmpty() && !(genome instanceof GenomeTemplate)) {
            System.err.println("Resource " + genome.getName() + " has no materials.");
//            throw new ExceptionInInitializerError("Resource " + genome.getName() + " has no materials.");
        } else if (!materials.isEmpty()) {
            tags.addAll(genome.getTags());
            tags.addAll(computeTags());
        }
    }

    private List<ResourceTag> computeTags() {
        List<ResourceTag> _t = new ArrayList<>(materials.get(0).getTags());
        if (!containsTag(new ResourceTag("goodForClothes")) &&
                _t.contains(new ResourceTag("flexible")) &&
                _t.contains(new ResourceTag("solid")) &&
                _t.contains(new ResourceTag("soft"))) {
            _t.add(new ResourceTag("goodForClothes"));
        }
        if (!containsTag(new ResourceTag("weapon")) &&
                _t.contains(new ResourceTag("hard")) &&
                _t.contains(new ResourceTag("sturdy")) &&
                genome.getSize() >= 0.05 && getGenome().isMovable()) {
            _t.add(new ResourceTag("weapon"));
        }
        if (!containsTag(new ResourceTag("goodForEngraving")) &&
                _t.contains(new ResourceTag("hard")) &&
                _t.contains(new ResourceTag("sturdy"))) {
            _t.add(new ResourceTag("goodForEngraving"));
        }
        return _t;
    }

    public List<ResourceTag> getTags() {
        return tags;
    }

    public Material getMainMaterial() {
        return materials.get(0);
    }

    public Genome getGenome() {
        return genome;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    String getMeaningPostfix() {
        return meaningPostfix;
    }

    public String getBaseName() {
        return genome.hasLegacy() ? genome.getName() + getGenome().getLegacyPostfix() : genome.getName();
    }

    boolean isHasMeaning() {
        return hasMeaning;
    }

    public boolean containsTag(ResourceTag tag) {
        return tags.contains(tag);
    }

    void setHasMeaning(boolean b) {
        hasMeaning = b;
    }

    Resource copy() {
        return new Resource(this);
    }

    ResourceIdeal copyWithLegacyInsertion(ResourceCore creator, ResourcePool resourcePool) {
        ResourceIdeal resource = new ResourceIdeal(new ResourceCore(genome.getName(), meaningPostfix, new ArrayList<>(materials),
                new Genome(genome), aspectConversion, meaning));
        resource.resourceCore._aspectConversion = _aspectConversion;
        resource.resourceCore.setLegacy(creator, resourcePool);
        return resource;//TODO is legacy passed to parts in genome?
    }

    void setLegacy(ResourceCore legacy, ResourcePool resourcePool) {
        genome.setLegacy(legacy);

        if (_aspectConversion != null) {
            for (Aspect aspect : _aspectConversion.keySet()) {
                if (Arrays.stream(_aspectConversion.get(aspect)).anyMatch(s -> s.split(":")[0].equals("LEGACY"))) {
                    aspectConversion.put(aspect, Arrays.stream(_aspectConversion.get(aspect))
                            .map(s -> readConversion(s, resourcePool)).collect(Collectors.toList()));
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

    Resource copy(int amount) {
        return new Resource(this, amount);
    }

    Resource fullCopy() {
        if (genome instanceof GenomeTemplate) {
            throw new SpaceError("Cant make a full copy of a template");
        }
        return new Resource(new ResourceCore(genome.getName(), meaningPostfix, new ArrayList<>(materials),
                new Genome(genome), aspectConversion, meaning));
    }

    private ResourceCore instantiateTemplateCopy(ResourceCore legacy) {
        if (!(genome instanceof GenomeTemplate)) {
            throw new SpaceError("Cant make a instantiated copy not from a template");
        }
        ResourceCore resourceCore = new ResourceCore(genome.getName(), meaningPostfix, new ArrayList<>(legacy.materials),
                ((GenomeTemplate) genome).getInstantiatedGenome(legacy), aspectConversion, meaning);
        resourceCore.materials.addAll(legacy.materials);
        resourceCore.computeTagsFromMaterials();
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
            for (String name : names) {
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
