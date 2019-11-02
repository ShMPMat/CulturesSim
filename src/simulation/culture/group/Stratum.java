package simulation.culture.group;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Stratum {
    private int amount;
    private List<Aspect> aspects;

    public Stratum(int amount) {
        this.amount = amount;
        aspects = new ArrayList<>();
    }

    public Stratum(int amount, Aspect aspect) {
        this(amount);
        aspects.add(aspect);
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

    public void setAmount(int amount) {
        this.amount = amount;
        if (amount < 0) {
            int i = 0; //TODO FUCKING NEGATIVE AMOUNTS
        }
    }

    public void addAspect(Aspect aspect) {
        aspects.add(aspect);
    }

    public ResourcePack use(int ceiling, Function<ResourcePack, Integer> amount) {
        ResourcePack resourcePack = new ResourcePack();
        for (Aspect aspect: aspects) {
            ShnyPair<Boolean, ResourcePack> result = aspect.use(ceiling, amount);
            if (result.first) {
                resourcePack.add(result.second);
            }
        }
        return resourcePack;
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
