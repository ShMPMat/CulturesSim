package simulation.culture.group.centers;

import kotlin.Pair;
import simulation.Controller;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.GroupError;
import simulation.culture.group.AspectStratum;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.thinking.meaning.MemePool;
import simulation.space.Territory;
import simulation.space.resource.MutableResourcePack;

import java.util.*;
import java.util.stream.Collectors;

public class PopulationCenter {
    private int population;
    private List<AspectStratum> strata = new ArrayList<>();

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

    List<AspectStratum> getStrata() {
        return strata;
    }

    public AspectStratum getStratumByAspect(ConverseWrapper aspect) {
        return strata.stream().filter(stratum -> stratum.containsAspect(aspect)).findFirst().orElse(null);
    }

    public int getFreePopulation() {
        int freePopulation = population - strata.stream()
                .map(AspectStratum::getPopulation)
                .reduce(0, Integer::sum);
        return freePopulation;
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

    public int changeStratumAmountByAspect(ConverseWrapper aspect, int amount) {
        AspectStratum stratum = getStratumByAspect(aspect);
        try {
            if (stratum.getFreePopulation() < amount) {
                amount = Math.min(amount, getFreePopulation() + stratum.getFreePopulation());
            }
            if (amount == 0) {
                return 0;
            }
            if (getFreePopulation() < 0) {
                int j = 0;
            }
            if (stratum.getPopulation() < stratum.getWorkedAmount()) {
                int i = 0;
            }
            stratum.useAmount(amount);
            int delta = -getFreePopulation();
            if (delta > 0) {
                if (delta > amount) {
                    int y = 0;
                }
                stratum.decreaseAmount(delta);
                amount -= delta;
            }
            if (amount < 0) {
                int j = 0;
            }
            return amount;
        } catch (NullPointerException e) {
            throw new RuntimeException("No stratum for Aspect");
        }
    }

    public void freeStratumAmountByAspect(ConverseWrapper aspect, int amount) {
        AspectStratum stratum = getStratumByAspect(aspect);
        try {
            stratum.decreaseAmount(amount);
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
            for (AspectStratum stratum : strata) {
                int part = (int) Math.min(amount * (((double) stratum.getPopulation()) / population) + 1, stratum.getPopulation());
                stratum.decreaseAmount(part);
            }
        }
        population -= amount;
    }

    void update(Territory accessibleTerritory) {
        strata.forEach(s -> s.update(turnResources, accessibleTerritory, this));
    }

    void executeRequests(Collection<Request> requests, Territory accessibleTerritory) {
        for (Request request : requests) {
            ResourceEvaluator evaluator = request.getEvaluator();
            List<AspectStratum> strataForRequest = getStrataForRequest(request);
            for (AspectStratum stratum : strataForRequest) {
                int amount = evaluator.evaluate(turnResources);
                if (amount >= request.getCeiling()) {
                    break;
                }
                turnResources.addAll(stratum.use(new AspectController(
                        1,
                        request.getCeiling() - amount,
                        request.getFloor() - amount,
                        evaluator,
                        this,
                        accessibleTerritory,
                        false,
                        null
                )));
            }
            request.end(turnResources);
        }
    }

    private List<AspectStratum> getStrataForRequest(Request request) {
        return getStrata().stream()
                .filter(s -> request.isAcceptable(s) != null)
                .sorted(Comparator.comparingInt(request::satisfactionLevel))
                .collect(Collectors.toList());
    }

    void manageNewAspects(Set<Aspect> aspects) {
        aspects.stream()
                .filter(a -> a instanceof ConverseWrapper)
                .map(a -> (ConverseWrapper) a)
                .forEach(a -> {
            if (strata.stream().noneMatch(s -> s.containsAspect(a))) {
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (AspectStratum stratum : strata) {
            if (stratum.getPopulation() != 0) {
                stringBuilder.append(stratum).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
