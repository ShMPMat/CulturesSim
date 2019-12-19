package simulation.culture.group;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.ResourcePack;

import java.util.*;

public class Stratum {
    private int amount;
    private int usedAmount = 0;
    private List<Aspect> aspects = new ArrayList<>();
    private Map<AspectTag, ResourcePack> dependencies = new HashMap<>();
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
                dependencies.put(new AspectTag(tag.name), new ResourcePack());
            }
        });
    }

    public ResourcePack getInstrumentByTag(AspectTag tag) {
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
            int i = 0; //TODO FUCKING NEGATIVE AMOUNTS
        }
    }

    public void addAspect(Aspect aspect) {
        aspects.add(aspect);
    }

    public ResourcePack use(int ceiling, ResourceEvaluator evaluator) {
        ResourcePack resourcePack = new ResourcePack();
        for (Aspect aspect: aspects) {
            ShnyPair<Boolean, ResourcePack> result = aspect.use(ceiling, evaluator);
            if (!result.second.isEmpty()) {
                group.getCulturalCenter().getMemePool().strengthenAspectMeme(aspect);
                result.second.getResources().forEach(resource ->
                        group.getCulturalCenter().getMemePool().strengthenMeme(resource.getFullName()));
            }
            if (result.first) {
                resourcePack.add(result.second);
            }
        }
        return resourcePack;
    }

    void update() {
        for (Map.Entry<AspectTag, ResourcePack> entry: dependencies.entrySet()) {
            //TODO
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
