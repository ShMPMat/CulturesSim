package simulation.culture.group;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.centers.Group;
import simulation.culture.group.centers.PopulationCenter;
import simulation.culture.group.request.EvaluatorsKt;
import simulation.culture.thinking.meaning.ConstructMemeKt;
import simulation.culture.thinking.meaning.MemePool;
import simulation.space.Territory;
import simulation.space.resource.tag.ResourceTag;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.MutableResourcePack;

import java.util.*;

import static simulation.Controller.session;

public class AspectStratum implements Stratum {
    private int population;
    /**
     * How many people have already worked on this turn;
     */
    private int workedAmount = 0;
    private boolean isRaisedAmount = false;
    private List<ConverseWrapper> aspects = new ArrayList<>();
    private Map<ResourceTag, MutableResourcePack> dependencies = new HashMap<>();
    private List<Meme> popularMemes = new ArrayList<>();

    public AspectStratum(int population, ConverseWrapper aspect) {
        this.population = population;
        aspects.add(aspect);
        aspect.getDependencies().getMap().keySet().forEach(tag -> {
            if (tag.isInstrumental() && !tag.name.equals("phony")) {
                dependencies.put(tag.copy(), new MutableResourcePack());
            }
        });
    }

    public int getWorkedAmount() {
        return workedAmount;
    }

    public MutableResourcePack getInstrumentByTag(ResourceTag tag) {
        MutableResourcePack resourcePack = dependencies.get(tag);
        return resourcePack == null ? new MutableResourcePack() : resourcePack;
    }

    public int getPopulation() {
        return population;
    }

    public int getFreePopulation() {
        return population - workedAmount;
    }

    public List<ConverseWrapper> getAspects() {
        return aspects;
    }

    public boolean containsAspect(ConverseWrapper aspect) {
        return aspects.contains(aspect);
    }

    public void decreaseAmount(int delta) {
        population -= delta;
        if (workedAmount > population) {
            workedAmount = population;
        }
    }

    public void decreaseWorkedAmount(int delta) {
        workedAmount -= delta;
    }

    public void useAmount(int amount) {
        if (amount <= 0) {
            return;
        }
        this.workedAmount += amount;
        if (workedAmount > population) {
            this.population = workedAmount;
            isRaisedAmount = true;
        }
    }

    public void update(
            MutableResourcePack accessibleResources,
            Territory accessibleTerritory,
            Group group
    ) {
        if (population == 0) {
            return;
        }
        int oldPopulation = population;
        MutableResourcePack pack = use(new AspectController(
                1,
                getFreePopulation(),
                getFreePopulation(),
                EvaluatorsKt.getPassingEvaluator(),
                group.getPopulationCenter(),
                accessibleTerritory,
                false,
                null,
                group
        ));
        population = oldPopulation;
        accessibleResources.addAll(pack);
        updateTools(accessibleTerritory, group);
        isRaisedAmount = false;
    }

    public MutableResourcePack use(AspectController controller) {
        MutableResourcePack resourcePack = new MutableResourcePack();
        for (Aspect aspect : aspects) {
            AspectResult result = aspect.use(controller);
            if (result.resources.isNotEmpty()) {
                popularMemes.add(ConstructMemeKt.constructMeme(aspect));
                result.resources.getResources().forEach(r -> popularMemes.add(ConstructMemeKt.constructMeme(r)));
            }
            if (result.isFinished) {
                resourcePack.addAll(result.resources);
            }
            result.pushNeeds(controller.getGroup());
        }
        return resourcePack;
    }

    private void updateTools(Territory accessibleTerritory, Group group) {
        if (!session.isTime(session.stratumTurnsBeforeInstrumentRenewal)) {
            return;
        }
        for (Map.Entry<ResourceTag, MutableResourcePack> entry : dependencies.entrySet()) {
            int currentAmount = entry.getValue().getAmount();
            if (currentAmount >= population) {
                continue;
            }
            if (!entry.getKey().isInstrumental()) {
                continue;
            }
            ResourceEvaluator evaluator = EvaluatorsKt.tagEvaluator(entry.getKey());
            for (Aspect aspect : aspects) {//TODO choose the best
                if (currentAmount >= population) {
                    break;
                }
                Set<Dependency> deps = aspect.getDependencies().getMap().get(entry.getKey());
                if (deps != null) {
                    for (Dependency dependency : deps) {
                        AspectResult result = dependency.useDependency(
                                new AspectController(
                                        1,
                                        population - currentAmount,
                                        1,
                                        evaluator,
                                        group.getPopulationCenter(),
                                        accessibleTerritory,
                                        false,
                                        null,
                                        group
                                ));
                        if (result.isFinished) {
                            currentAmount += evaluator.evaluate(result.resources);
                            entry.getValue().addAll(evaluator.pick(result.resources));//TODO disband
                            if (currentAmount >= population) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void finishUpdate(Group group) {
        popularMemes.forEach(m -> group.getCultureCenter().getMemePool().strengthenMeme(m));
        popularMemes.clear();
        if (workedAmount < population) {
            population = workedAmount;
        }
        if (aspects.get(0).getName().equals("TakeOnHerbivore")) {
            int i = 0;
        }
        workedAmount = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AspectStratum stratum = (AspectStratum) o;
        return Objects.equals(aspects, stratum.aspects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspects);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Stratum with population ");
        stringBuilder.append(population).append(", aspects: ");
        for (Aspect aspect : aspects) {
            stringBuilder.append(aspect.getName()).append(" ");
        }
        return stringBuilder.toString();
    }
}