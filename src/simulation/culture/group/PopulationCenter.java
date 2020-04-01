package simulation.culture.group;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
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

    PopulationCenter(int population, int maxPopulation, int minPopulation) {
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
        return population - strata.stream()
                .map(Stratum::getAmount)
                .reduce(Integer::sum)
                .orElse(0);
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

    public void setPopulation(int population) {
        this.population = population;
    }

    public int changeStratumAmountByAspect(Aspect aspect, int amount) {
        Stratum stratum = getStratumByAspect(aspect);
        try {
            if (stratum.getAmount() < amount) {
                amount = Math.min(amount, getFreePopulation());
            }
            stratum.useAmount(amount);
            return amount;
        } catch (NullPointerException e) {
            throw new RuntimeException("No stratum for Aspect");
        }
    }

    void die() {
        population = 0;
    }

    void goodConditionsGrow(double fraction) {//TODO check boundaries
        population += ((int) (fraction * population)) / 10 + 1;
    }

    void decreasePopulation(int amount) {
        if (getFreePopulation() < 0) {
            int i = 0; //TODO still happens
        }
        amount = Math.min(population, amount);
        int delta = amount - getFreePopulation();
        if (delta > 0) {
            for (Stratum stratum : strata) {
                int part = (int) Math.min(amount * (((double) stratum.getAmount()) / population) + 1, stratum.getAmount());
                stratum.freeAmount(part);
            }
        }
        population -= amount;
        if (getFreePopulation() < 0) {
            int i = 0; //TODO still happens
        }
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
                if (amount >= request.ceiling) {
                    break;
                }
                turnResources.addAll(stratum.use(new AspectController(
                        request.ceiling - amount,
                        request.floor,
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
        population -= populationPart;
        PopulationCenter populationCenter = new PopulationCenter(
                populationPart,
                maxPopulation,
                minPopulation
        );
        for (Stratum stratum : getStrata()) {
            stratum.decreaseAmount((int) (stratum.getAmount() * fraction));
        }
        return populationCenter;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Stratum stratum : strata) {
            if (stratum.getAmount() != 0) {
                stringBuilder.append(stratum).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
