package simulation.culture.group;

import kotlin.Pair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.Territory;
import simulation.space.resource.ResourcePack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static simulation.Controller.session;

public class PopulationCenter {
    private int population;
    private List<Stratum> strata = new ArrayList<>();

    private int maxPopulation;
    private int minPopulation;

    private ResourcePack turnResources = new ResourcePack();

    PopulationCenter(int population, int maxPopulation, int minPopulation) {
        this.population = population;
        this.maxPopulation = maxPopulation;
        this.minPopulation = minPopulation;
    }

    public int getPopulation() {
        return population;
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
            throw new RuntimeException("No stratum for Aspect");//TODO it happens
        }
    }

    void strataUpdate() {
        strata.forEach(Stratum::update);
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

    void executeRequests(Collection<Request> requests) {
        for (Request request : requests) { //TODO do smth about getting A LOT MORE resources than planned due to one to many resource conversion
            List<Pair<Stratum, ResourceEvaluator>> pairs = getStrata().stream()
                    .map(stratum -> new Pair<>(stratum, request.isAcceptable(stratum)))
                    .filter(pair -> pair.getSecond() != null)
                    .sorted(Comparator.comparingInt(pair -> request.satisfactionLevel(pair.getFirst())))
                    .collect(Collectors.toList());
            for (Pair<Stratum, ResourceEvaluator> pair : pairs) {
                int amount = pair.getSecond().evaluate(turnResources);
                if (amount >= request.ceiling) {
                    break;
                }
                turnResources.add(pair.getFirst().use(new AspectController(
                        request.ceiling - amount,
                        request.floor,
                        pair.getSecond(),
                        this,
                        false
                )));
            }
            request.end(turnResources);
        }
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
}
