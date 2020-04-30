package simulation.culture.group.stratum;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Place;
import simulation.culture.group.centers.Group;
import simulation.culture.group.request.*;
import simulation.culture.thinking.meaning.ConstructMemeKt;
import simulation.space.Territory;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.tag.ResourceTag;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.MutableResourcePack;
import simulation.space.tile.Tile;
import simulation.space.tile.TileTag;

import java.util.*;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.randomElement;
import static simulation.Controller.session;
import static simulation.culture.group.GroupsKt.getPassingReward;

public class AspectStratum implements Stratum {
    private double _effectiveness = -1.0;
    private int population;
    /**
     * How many people have already worked on this turn;
     */
    private int workedAmount = 0;
    private boolean isRaisedAmount = false;
    private List<ConverseWrapper> aspects = new ArrayList<>();
    private Map<ResourceTag, MutableResourcePack> dependencies = new HashMap<>();
    private MutableResourcePack enhancements = new MutableResourcePack();
    private List<Place> places = new ArrayList<>();
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

    public void decreaseAmount(int amount) {
        population -= amount;
        if (workedAmount > population) {
            workedAmount = population;
        }
    }

    public void decreaseWorkedAmount(int amount) {
        workedAmount -= amount;
    }

    private double getEffectiveness() {
        if (_effectiveness == -1) {
            _effectiveness = 1.0 + places.stream()
                    .flatMap(p -> p.getOwned().getResources().stream())
            .map(r -> r.getAspectImprovement(aspects.get(0)))
                    .reduce(0.0, Double::sum);
        }
        return _effectiveness;
    }

    public WorkerBunch useCumulativeAmount(int amount) {
        return useActualAmount((int) Math.ceil(amount / getEffectiveness()));
    }

    public WorkerBunch useActualAmount(int amount) {
        if (amount <= 0) {
            return new WorkerBunch(0, 0);
        }
        this.workedAmount += amount;
        if (workedAmount > population) {
            population = workedAmount;
            isRaisedAmount = true;
        }
        return new WorkerBunch(amount, amount);
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
        if (population < oldPopulation) {
            population = oldPopulation;
        }
        accessibleResources.addAll(pack);
        updateInfrastructure(accessibleTerritory, group);
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

    private void updateInfrastructure(Territory accessibleTerritory, Group group) {
        if (!session.isTime(session.stratumTurnsBeforeInstrumentRenewal)) {
            return;
        }
        updateTools(accessibleTerritory, group);
        updatePlaces(group);
    }

    private void updateTools(Territory accessibleTerritory, Group group) {
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

    private void updatePlaces(Group group) {
        if (!group.getTerritoryCenter().getSettled()) {
            return;
        }
        Request request = new AspectImprovementRequest(
                group,
                aspects.get(0),
                1,
                1,
                getPassingReward(),
                getPassingReward()
        );
        ResourcePack pack = group.getPopulationCenter().executeRequest(
                request,
                group
        );
        enhancements.addAll(pack.getResources(r ->
                r.getGenome().isMovable())
        );
        pack.getResources(r -> !r.getGenome().isMovable()).getResources()
                .forEach(resource -> addUnmovableEnhancement(resource, group));
    }

    private void addUnmovableEnhancement(Resource resource, Group group) {
        List<Place> goodPlaces = places.stream()
                .filter(p -> resource.getGenome().isAcceptable(p.getTile()))
                .collect(Collectors.toList());
        Place place = null;
        if (goodPlaces.isEmpty()) {
            List<Tile> goodTiles = group.getTerritoryCenter().getTerritory()
                    .getTiles(t -> resource.getGenome().isAcceptable(t));
            if (!goodTiles.isEmpty()) {
                String tagType = "(Stratum " + aspects.get(0).getName() + " of " + group.name + ")";
                place = new Place(
                        randomElement(goodTiles, session.random),
                        new TileTag(tagType + "_" + places.size(), tagType)
                );
                places.add(place);
            }
        } else {
            place = randomElement(places, session.random);
        }
        if (place == null) {
            return;
        }
        place.addResource(resource);
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
        _effectiveness = -1.0;
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
        stringBuilder.append(population).append(", effectiveness -").append(getEffectiveness()).append(", aspects: ");
        for (Aspect aspect : aspects) {
            stringBuilder.append(aspect.getName()).append(" ");
        }
        return stringBuilder.toString();
    }

    @Override
    public void die() {
        population = 0;
        workedAmount = 0;
        places.forEach(Place::delete);
    }
}