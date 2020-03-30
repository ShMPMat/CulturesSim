package simulation.culture.group;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.culture.group.request.EvaluatorsKt;
import simulation.culture.thinking.meaning.ConstructMemeKt;
import simulation.culture.thinking.meaning.MemePool;
import simulation.space.Territory;
import simulation.space.resource.tag.ResourceTag;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.Resource;
import simulation.space.resource.MutableResourcePack;

import java.util.*;

import static simulation.Controller.session;

public class Stratum {
    private int amount;
    /**
     * How many people already worked on this turn;
     */
    private int workedAmount = 0;
    private boolean raisedAmount = false;
    private List<Aspect> aspects = new ArrayList<>();
    private Map<ResourceTag, MutableResourcePack> dependencies = new HashMap<>();
    private List<Meme> popularMemes = new ArrayList<>();

    public Stratum(int amount, Aspect aspect) {
        this.amount = amount;
        aspects.add(aspect);
        aspect.getDependencies().getMap().keySet().forEach(tag -> {
            if (tag.isInstrumental() && !tag.name.equals("phony")) {
                dependencies.put(tag.copy(), new MutableResourcePack());
            }
        });
    }

    public MutableResourcePack getInstrumentByTag(ResourceTag tag) {
        MutableResourcePack resourcePack = dependencies.get(tag);
        return resourcePack == null ? new MutableResourcePack() : resourcePack;
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

    public void decreaseAmount(int delta) {
        this.amount -= delta;
    }

    public void useAmount(int amount) {
        this.workedAmount += amount;
        if (workedAmount > this.amount) {
            this.amount = workedAmount;
            raisedAmount = true;
        }
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

    void update(MutableResourcePack turnResources, Territory accessibleTerritory, PopulationCenter populationCenter) {
        if (getAmount() == 0) {
            return;
        }
        MutableResourcePack pack = use(new AspectController(
                getAmount(),
                getAmount(),
                EvaluatorsKt.getPassingEvaluator(),
                populationCenter,
                accessibleTerritory,
                false,
                null
        ));
        if (!pack.isEmpty()) {
            int i = 0;
        }
        turnResources.addAll(pack);
        updateTools(accessibleTerritory, populationCenter);
        raisedAmount = false;
    }

    public MutableResourcePack use(AspectController controller) {
        MutableResourcePack resourcePack = new MutableResourcePack();
        for (Aspect aspect: aspects) {
            AspectResult result = aspect.use(controller);
            if (!result.resources.isEmpty()) {
                popularMemes.add(ConstructMemeKt.constructMeme(aspect));
                result.resources.getResources().forEach(r -> popularMemes.add(ConstructMemeKt.constructMeme(r)));
            }
            if (result.isFinished) {
                resourcePack.addAll(result.resources);
            }
        }
        return resourcePack;
    }

    void updateTools(Territory accessibleTerritory, PopulationCenter populationCenter) {
        if (session.isTime(session.stratumTurnsBeforeInstrumentRenewal)) {
            return;
        }
        for (Map.Entry<ResourceTag, MutableResourcePack> entry: dependencies.entrySet()) {
            int currentAmount = entry.getValue().getAmount();
            if (currentAmount >= amount) {
                continue;
            }
            if (!entry.getKey().isInstrumental()) {
                continue;
            }
            ResourceEvaluator evaluator = EvaluatorsKt.tagEvaluator(entry.getKey());
            for (Aspect aspect: aspects) {//TODO choose the best
                if (currentAmount >= amount) {
                    break;
                }
                Set<Dependency> deps = aspect.getDependencies().getMap().get(entry.getKey());
                if (deps != null) {
                    for (Dependency dependency: deps) {
                        AspectResult result = dependency.useDependency(
                                new AspectController(
                                        amount - currentAmount,
                                        1,
                                        evaluator,
                                        populationCenter,
                                        accessibleTerritory,
                                        false,
                                        null
                                ));
                        if (result.isFinished) {
                            currentAmount += evaluator.evaluate(result.resources);
                            entry.getValue().addAll(evaluator.pick(result.resources));//TODO disband
                            if (currentAmount >= amount) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        amount -= amount > workedAmount ? 1 : 0;
        workedAmount = 0;
    }

    void finishUpdate(MemePool pool) {
        popularMemes.forEach(pool::strengthenMeme);
        popularMemes.clear();
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
