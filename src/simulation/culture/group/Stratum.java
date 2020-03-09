package simulation.culture.group;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.space.resource.tag.ResourceTag;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;

import static simulation.Controller.session;

/**
 * Represents certain people who do particular work in Group
 */
public class Stratum {
    /**
     * Overall amount of people in Stratum
     */
    private int amount;
    /**
     * How many people already worked on this turn;
     */
    private int usedAmount = 0;
    private List<Aspect> aspects = new ArrayList<>();
    private Map<ResourceTag, ResourcePack> dependencies = new HashMap<>();
    private Group group;

    public Stratum(int amount, Group group) {
        this.amount = amount;
        this.group = group;
    }

    public Stratum(int amount, Aspect aspect, Group group) {
        this(amount, group);
        aspects.add(aspect);
        aspect.getDependencies().keySet().forEach(tag -> {
            if (tag.isInstrumental() && !tag.name.equals("phony")) {
                dependencies.put(tag.copy(), new ResourcePack());
            }
        });
    }

    public ResourcePack getInstrumentByTag(ResourceTag tag) {
        ResourcePack resourcePack = dependencies.get(tag);
        return resourcePack == null ? new ResourcePack() : resourcePack;
    }

    public int getAmount() {
        return amount;
    }

    public List<Aspect> getAspects() {
        return aspects;
    }

    public boolean containsAspect(Aspect aspect) {
        return aspects.contains(aspect);
    }

    public void useAmount(int amount) {
        this.usedAmount = amount;
        this.amount = Math.max(amount, this.amount);
        if (amount < 0) {
            int i = 0; //TODO still happens
        }
    }

    public void freeAmount(int delta) {
        amount -= delta;
    }

    public void addAspect(Aspect aspect) {
        aspects.add(aspect);
    }

    public ResourcePack use(AspectController controller) {
        ResourcePack resourcePack = new ResourcePack();
        for (Aspect aspect: aspects) {
            AspectResult result = aspect.use(controller);
            if (!result.resources.isEmpty()) {
                group.getCulturalCenter().getMemePool().strengthenMeme(Meme.getMeme(aspect));
                result.resources.getResources().forEach(resource ->
                        group.getCulturalCenter().getMemePool().strengthenMeme(Meme.getMeme(resource)));
            }
            if (result.isFinished) {
                resourcePack.add(result.resources);
            }
        }
        return resourcePack;
    }

    void update() {
        if (session.isTime(session.stratumTurnsBeforeInstrumentRenewal)) {
            return;
        }
        for (Map.Entry<ResourceTag, ResourcePack> entry: dependencies.entrySet()) {
            int currentAmount = entry.getValue().getAmount();
            if (currentAmount >= amount) {
                continue;
            }
            if (!entry.getKey().isInstrumental()) {
                continue;
            }
            ResourceEvaluator evaluator = new ResourceEvaluator(
                    resourcePack -> resourcePack.getAllResourcesWithTag(entry.getKey()),
                    resourcePack -> resourcePack.getAllResourcesWithTag(entry.getKey()).getResources().stream()
                            .map(Resource::getAmount).reduce(0, Integer::sum));
            for (Aspect aspect: aspects) {//TODO choose the best
                if (currentAmount >= amount) {
                    break;
                }
                Set<Dependency> deps = aspect.getDependencies().get(entry.getKey());
                if (deps != null) {
                    for (Dependency dependency: deps) {
                        AspectResult result = dependency.useDependency(
                                new AspectController(amount - currentAmount, 1, evaluator));
                        if (result.isFinished) {
                            currentAmount += evaluator.evaluate(result.resources);
                            entry.getValue().add(evaluator.pick(result.resources));//TODO disband
                            if (currentAmount >= amount) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        amount -= amount > usedAmount ? 1 : 0;
        usedAmount = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stratum stratum = (Stratum) o;
        return Objects.equals(aspects, stratum.aspects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspects);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Stratum with population ");
        stringBuilder.append(amount).append(", aspects: ");
        for (Aspect aspect: aspects) {
            stringBuilder.append(aspect.getName()).append(" ");
        }
        return stringBuilder.toString();
    }
}
