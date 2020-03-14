package simulation.culture.group;

import simulation.culture.aspect.Aspect;
import simulation.space.Territory;

import java.util.ArrayList;
import java.util.List;

public class PopulationCenter {
    private int population;
    private List<Stratum> strata = new ArrayList<>();

    private int maxPopulation;
    private int minPopulation;

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

    void update() {
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
}
