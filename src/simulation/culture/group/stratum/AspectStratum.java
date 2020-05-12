package simulation.culture.group.stratum;

import simulation.culture.aspect.*;
import simulation.culture.group.Place;
import simulation.culture.group.centers.Group;
import simulation.culture.group.request.*;
import simulation.culture.thinking.meaning.ConstructMemeKt;
import simulation.space.Territory;
import simulation.space.resource.Resource;
import simulation.space.resource.tag.ResourceTag;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.MutableResourcePack;
import simulation.space.tile.Tile;
import simulation.space.tile.TileTag;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;
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
    private ConverseWrapper aspect;
    private Map<ResourceTag, MutableResourcePack> dependencies = new HashMap<>();
    private MutableResourcePack enhancements = new MutableResourcePack();
    private List<Place> places = new ArrayList<>();
    private List<Meme> popularMemes = new ArrayList<>();

    public AspectStratum(int population, ConverseWrapper aspect) {
        this.population = population;
        this.aspect = aspect;
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

    public ConverseWrapper getAspect() {
        return aspect;
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
            .map(r -> r.getAspectImprovement(aspect))
                    .reduce(0.0, Double::sum);
        }
        return _effectiveness;
    }

    @Override
    public WorkerBunch useAmount(int amount, int maxOverhead) {
        if (amount <= 0) {
            return new WorkerBunch(0, 0);
        }
        int actualAmount = (int) Math.ceil(amount / getEffectiveness());
        if (actualAmount <= getFreePopulation()) {
            workedAmount += actualAmount;
        } else {
            int additional = min(maxOverhead, actualAmount - getFreePopulation());
            actualAmount = additional + getFreePopulation();
            population += additional;
            workedAmount = population;
            if (additional > 0) {
                isRaisedAmount = true;
            }
        }
        return new WorkerBunch((int) (actualAmount * getEffectiveness()), actualAmount);
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
        ResourceEvaluator evaluator = EvaluatorsKt.getPassingEvaluator();
        double overhead = aspect.calculateNeededWorkers(evaluator, getFreePopulation());
        int amount = (int) aspect.calculateProducedValue(evaluator, getFreePopulation());
        MutableResourcePack pack = use(new AspectController(
                1,
                amount,
                amount,
                evaluator,
                group.getPopulationCenter(),
                accessibleTerritory,
                false,
                group,
                group.getCultureCenter().getMeaning()
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
        AspectResult result = aspect.use(controller);
        if (result.resources.isNotEmpty()) {
            popularMemes.add(ConstructMemeKt.constructMeme(aspect));
            result.resources.getResources().forEach(r -> popularMemes.add(ConstructMemeKt.constructMeme(r)));
        }
        if (result.isFinished) {
            resourcePack.addAll(result.resources);
        }
        result.pushNeeds(controller.getGroup());
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
            //TODO choose the best
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
                                    group,
                                    group.getCultureCenter().getMeaning()
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

    private void updatePlaces(Group group) {
        if (!group.getTerritoryCenter().getSettled()) {
            return;
        }
        Request request = new AspectImprovementRequest(
                group,
                aspect,
                1,
                1,
                getPassingReward(),
                getPassingReward()
        );
        ExecutedRequestResult result = group.getPopulationCenter().executeRequest(request);
        result.getUsedAspects().forEach(a -> a.gainUsefulness(session.stratumTurnsBeforeInstrumentRenewal * 2));
        enhancements.addAll(result.getPack().getResources(r ->
                r.getGenome().isMovable())
        );
        result.getPack().getResources(r -> !r.getGenome().isMovable()).getResources()
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
                String tagType = "(Stratum " + aspect.getName() + " of " + group.getName() + ")";
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
        workedAmount = 0;
        _effectiveness = -1.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AspectStratum stratum = (AspectStratum) o;
        return Objects.equals(aspect, stratum.aspect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspect);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Stratum with population ");
        stringBuilder.append(population).append(", effectiveness ").append(getEffectiveness()).append(", ")
                .append(aspect.getName()).append(" ");

        stringBuilder.append(", Places:");
        places.forEach(p -> stringBuilder.append(p).append(" "));
        return stringBuilder.toString();
    }

    @Override
    public void die() {
        population = 0;
        workedAmount = 0;
        places.forEach(session.world.getStrayPlacesManager()::addPlace);
    }
}