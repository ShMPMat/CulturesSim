package simulation.space.resource;

import kotlin.Pair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectResult;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.SpaceError;
import simulation.space.resource.material.Material;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class which contains all general information about all Resources with the same name.
 */
public class ResourceCore {
    private String meaningPostfix = "";
    private boolean hasMeaning = false;
    private Genome genome;
    private List<Material> materials;
    private Map<Aspect, List<Pair<Resource, Integer>>> aspectConversion;
    private List<ResourceTag> tags;
    private Meme meaning;

    ResourceCore(
            String name,
            String meaningPostfix,
            List<Material> materials,
            Genome genome,
            Map<Aspect, List<Pair<Resource, Integer>>> aspectConversion,
            Meme meaning
    ) {
        this.meaning = meaning;
        this.aspectConversion = new HashMap<>(aspectConversion);
        this.tags = new ArrayList<>();
        this.materials = materials;
        this.genome = genome;
        setName(name + meaningPostfix);
        computeTagsFromMaterials();
    }

    public void addAspectConversion(Aspect aspect, List<Pair<Resource, Integer>> resourceList) { //TODO must be package-private
        aspectConversion.put(aspect, resourceList);
    }

    public Map<Aspect, List<Pair<Resource, Integer>>> getAspectConversion() {
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
            throw new ExceptionInInitializerError("Resource " + genome.getName() + " has no materials.");
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
                    .flatMap(entry -> entry.getValue().getResources().stream().map(Resource::getFullName)).distinct()
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

    List<Resource> applyAspect(Aspect aspect) {//TODO throw an exception on any attempt to copy template
        if (aspectConversion.containsKey(aspect)) {
            return aspectConversion.get(aspect).stream()
                    .map(pair -> {
                        Resource resource = pair.getFirst();
                        if (resource.resourceCore.genome instanceof GenomeTemplate) {
                            resource = resource.copy(pair.getSecond());
                            resource.resourceCore = resource.resourceCore.instantiateTemplateCopy(this);
                            resource.computeHash();
                            return resource;
                        } else {
                            return resource.copy(pair.getSecond());
                        }
                    }).collect(Collectors.toList());
        }
        return Collections.singletonList(applyAspectToMaterials(aspect).copy(1));
    }

    private ResourceCore applyAspectToMaterials(Aspect aspect) {
        List<Material> newMaterials = materials.stream().map(material -> material.applyAspect(aspect))
                .collect(Collectors.toList());
        Genome genome = new Genome(this.genome);
        genome.setSpreadProbability(0);
        return new ResourceCore(genome.getName() + (newMaterials.equals(materials) ? "" : "_" + aspect.getName()),
                meaningPostfix, newMaterials, genome, aspectConversion, meaning);//TODO dangerous stuff for genome
    }

    public boolean hasApplicationForAspect(Aspect aspect) {
        return aspectConversion.containsKey(aspect) ||
                materials.stream().anyMatch(material -> material.hasApplicationForAspect(aspect));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        ResourceCore resourceCore = (ResourceCore) o;
        return getBaseName().equals(resourceCore.getBaseName());
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
