package simulation.culture.group.centers;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.group.GroupError;
import simulation.culture.group.Stratum;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.thinking.meaning.MemePool;
import simulation.space.Territory;
import simulation.space.resource.MutableResourcePack;

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

    List<Stratum> getStrata() {
        return strata;
    }

    public Stratum getStratumByAspect(Aspect aspect) {
        return strata.stream().filter(stratum -> stratum.containsAspect(aspect)).findFirst().orElse(null);
    }

    public int getFreePopulation() {
        int freePopulation = population - strata.stream()
                .map(Stratum::getPopulation)
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

    public int changeStratumAmountByAspect(Aspect aspect, int amount) {
        Stratum stratum = getStratumByAspect(aspect);
        try {
            if (stratum.getPopulation() < amount) {
                amount = Math.min(amount, getFreePopulation());
            }
            if (amount == 0) {
                return 0;
            }
            stratum.useAmount(amount);
            int delta = -getFreePopulation();
            if (delta > 0) {
                stratum.decreaseAmount(delta);
                amount -= delta;
            }
            return amount;
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

    void update(Territory accessibleTerritory) {
        strata.forEach(s -> s.update(turnResources, accessibleTerritory, this));
    }

    void executeRequests(Collection<Request> requests, Territory accessibleTerritory) {
        for (Request request : requests) {
            ResourceEvaluator evaluator = request.getEvaluator();
            List<Stratum> strataForRequest = getStrataForRequest(request);
            for (Stratum stratum : strataForRequest) {
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

    private List<Stratum> getStrataForRequest(Request request) {
        return getStrata().stream()
                .filter(s -> request.isAcceptable(s) != null)
                .sorted(Comparator.comparingInt(request::satisfactionLevel))
                .collect(Collectors.toList());
    }

    void manageNewAspects(Set<Aspect> aspects) {
        aspects.forEach(a -> {
            if (strata.stream().noneMatch(s -> s.containsAspect(a))) {
                strata.add(new Stratum(0, a));
            }
        });
    }

    void finishUpdate(MemePool pool) {
        strata.forEach(s -> s.finishUpdate(pool));
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
        for (Stratum stratum : strata) {
            if (stratum.getPopulation() != 0) {
                stringBuilder.append(stratum).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
