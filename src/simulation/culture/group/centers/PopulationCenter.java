package simulation.culture.group.centers;

import kotlin.Pair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectLabeler;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.GroupError;
import simulation.culture.group.AspectStratum;
import simulation.culture.group.Stratum;
import simulation.culture.group.WorkerBunch;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.RequestPool;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.Territory;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.tag.labeler.ResourceLabeler;

import java.util.*;
import java.util.stream.Collectors;

public class PopulationCenter {
    private int population;
    private List<Stratum> strata = new ArrayList<>();

    private int maxPopulation;
    private int minPopulation;

    private MutableResourcePack turnResources = new MutableResourcePack();

    public PopulationCenter(int population, int maxPopulation, int minPopulation) {
        this.population = population;
        this.maxPopulation = maxPopulation;
        this.minPopulation = minPopulation;
    }

    public int getPopulation() {
        return population;
    }

    public MutableResourcePack getTurnResources() {
        return turnResources;
    }

    public List<Stratum> getStrata() {
        return strata;
    }

    public AspectStratum getStratumByAspect(ConverseWrapper aspect) {
        return strata.stream()
                .filter(s -> s instanceof AspectStratum)
                .map(s -> ((AspectStratum) s))
                .filter(s -> s.containsAspect(aspect))
                .findFirst()
                .orElse(null);
    }

    public int getFreePopulation() {
        return population - strata.stream()
                .map(Stratum::getPopulation)
                .reduce(0, Integer::sum);
    }

    int getMaxPopulation(Territory controlledTerritory) {
        return controlledTerritory.size() * maxPopulation;
    }

    public boolean isMaxReached(Territory controlledTerritory) {
        return getMaxPopulation(controlledTerritory) <= population;
    }

    int getMinPopulation(Territory controlledTerritory) {
        return controlledTerritory.size() * minPopulation;
    }

    public boolean isMinPassed(Territory controlledTerritory) {
        return getMinPopulation(controlledTerritory) <= population;
    }

    public WorkerBunch changeStratumAmountByAspect(ConverseWrapper aspect, int amount) {
        AspectStratum stratum = getStratumByAspect(aspect);
        try {
            if (stratum.getFreePopulation() < amount) {
                amount = Math.min(amount, getFreePopulation() + stratum.getFreePopulation());
            }
            if (amount == 0) {
                return new WorkerBunch(0, 0);
            }
            WorkerBunch bunch = stratum.useCumulativeAmount(amount);
            int delta = -getFreePopulation();
            if (delta > 0) {
                stratum.decreaseAmount(bunch.getActualWorkers());
                amount -= delta;
                bunch = stratum.useActualAmount(amount);
            }
            if (getFreePopulation() < 0) {
                int j = 0;
            }
            return bunch;
        } catch (NullPointerException e) {
            throw new RuntimeException("No stratum for Aspect");
        }
    }

    public void freeStratumAmountByAspect(ConverseWrapper aspect, WorkerBunch bunch) {
        try {
            getStratumByAspect(aspect).decreaseWorkedAmount(bunch.getActualWorkers());
        } catch (NullPointerException e) {
            throw new RuntimeException("No stratum for Aspect");
        }
    }

    void die() {
        population = 0;
    }

    void goodConditionsGrow(double fraction, Territory territory) {
        population += ((int) (fraction * population)) / 10 + 1;
        if (isMaxReached(territory)) {
            decreasePopulation(population - getMaxPopulation(territory));
        }
    }

    void decreasePopulation(int amount) {
        if (getFreePopulation() < 0) {
            throw new GroupError("Negative population in a PopulationCenter");
        }
        amount = Math.min(population, amount);
        int delta = amount - getFreePopulation();
        if (delta > 0) {
            for (Stratum stratum : strata) {
                int part = (int) Math.min(amount * (((double) stratum.getPopulation()) / population) + 1, stratum.getPopulation());
                stratum.decreaseAmount(part);
            }
        }
        population -= amount;
    }

    void update(Territory accessibleTerritory, Group group) {
        strata.forEach(s -> s.update(turnResources, accessibleTerritory, group));
    }

    void executeRequests(RequestPool requests, Territory accessibleTerritory, Group group) {
        for (Request request : requests.getRequests().keySet()) {
            requests.getRequests().get(request).addAll(executeRequest(request, accessibleTerritory, group));
        }
    }

    ResourcePack executeRequest(Request request, Territory accessibleTerritory, Group group) {
        ResourceEvaluator evaluator = request.getEvaluator();
        List<AspectStratum> strataForRequest = getStrataForRequest(request);
        MutableResourcePack pack = new MutableResourcePack(evaluator.pick(
                request.getCeiling(),
                turnResources.getResources(),
                r -> Collections.singletonList(r.copy(1)),
                (r, p) -> Collections.singletonList(r.getCleanPart(p))
        ).getResources());
        for (AspectStratum stratum : strataForRequest) {
            int amount = evaluator.evaluate(pack);
            if (amount >= request.getCeiling()) {
                break;
            }
            pack.addAll(stratum.use(new AspectController(
                    1,
                    request.getCeiling() - amount,
                    request.getFloor() - amount,
                    evaluator,
                    this,
                    accessibleTerritory,
                    false,
                    null,
                    group
            )));
        }
        return pack;
    }

    private List<AspectStratum> getStrataForRequest(Request request) {
        return getStrata().stream()
                .filter(s -> request.isAcceptable(s) != null)
                .sorted(Comparator.comparingInt(request::satisfactionLevel))
                .filter(s -> s instanceof AspectStratum)
                .map(s -> (AspectStratum) s)
                .collect(Collectors.toList());
    }

    void manageNewAspects(Set<Aspect> aspects) {
        aspects.stream()
                .filter(a -> a instanceof ConverseWrapper)
                .map(a -> (ConverseWrapper) a)
                .forEach(a -> {
            if (strata.stream()
                    .filter(s -> s instanceof AspectStratum)
                    .map(s -> (AspectStratum) s)
                    .noneMatch(s -> s.containsAspect(a))) {
                strata.add(new AspectStratum(0, a));
            }
        });
    }

    void finishUpdate(Group group) {
        strata.forEach(s -> s.finishUpdate(group));
    }

    PopulationCenter getPart(double fraction) {
        int populationPart = (int) (fraction * population);
        decreasePopulation(populationPart);
        return new PopulationCenter(
                populationPart,
                maxPopulation,
                minPopulation
        );
    }

    void wakeNeedStrata(Pair<ResourceLabeler, ResourceNeed> need) {
        AspectLabeler labeler = new AspectLabeler(need.getFirst());
        List<AspectStratum> options = getStrata().stream()
                .filter(s -> s.getPopulation() == 0)
                .filter(s -> s instanceof AspectStratum)
                .map(s -> (AspectStratum) s)
                .filter(s -> s.getAspects().stream().anyMatch(labeler::isSuitable))
                .collect(Collectors.toList());
        //TODO shuffle
        int population = getFreePopulation();
        if (population < options.size()) {
            options = options.subList(0, population);
        }
        options.forEach(s ->
                s.useCumulativeAmount(1)
        );
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Stratum stratum : strata) {
            if (stratum.getPopulation() != 0) {
                stringBuilder.append(stratum).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public void addStratum(Stratum stratum) {
        strata.add(stratum);
    }
}
