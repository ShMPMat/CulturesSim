package extra;

import kotlin.Pair;
import simulation.culture.aspect.Aspect;
import simulation.culture.group.Group;
import simulation.space.Territory;
import simulation.space.WorldMap;
import simulation.space.Tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Functions related to random and probability.
 */
public class ProbabilityFuncs {
    /**
     * Returns boolean with Probability.
     * @param probability double from 0 to 1 inclusively.
     * @return true with Probability, otherwise returns false.
     */
    public static boolean testProbability(double probability) {
        return Math.random() <= probability;
    }

    /**
     * Returns random integer lower then ceiling.
     * @param ceiling non-negative number.
     * @return random int from 0 to ceiling.
     */
    public static int randomInt(int ceiling) {
        return ThreadLocalRandom.current().nextInt(ceiling);
    }

    /**
     * @param list a List from which a random element will be chosen.
     * @param <E> Type of the List
     * @return a random element from the List.
     */
    public static <E> E randomElement(List<E> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(randomInt(list.size()));
    }

    public static <E> E randomElement(List<E> list, Predicate<E> predicate) {
        return randomElement(list.stream().filter(predicate).collect(Collectors.toList()));
    }

    /**
     * @param array An Array from which a random element will be chosen.
     * @param <E> Type of the Array.
     * @return a random element from the Array.
     */
    public static <E> E randomElement(E[] array) {
        return array[randomInt(array.length)];
    }

    public static <E> E randomElementWithProbability(List<E> list, Function<E, Double> mapper) {
        List<Double> probabilities = list.stream().map(mapper).collect(Collectors.toList());
        double result = Math.random() * probabilities.stream().reduce((double) 0, Double::sum);
        for (int i = 0; i < probabilities.size(); i++) {
            double probability = probabilities.get(i);
            if (result <= probability) {
                return list.get(i);
            }
            result -= probability;
        }
        return null;
    }
}
