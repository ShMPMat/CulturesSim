package extra;

import java.util.Objects;

/**
 * Custom pair class.
 * @param <T> - type of the first element of a pair.
 * @param <V> - type of the second element of a pair.
 */
public class ShnyPair<T, V> {
    public T first;
    public V second;

    /**
     * Base constructor.
     * @param first first element of a pair.
     * @param second second element of a pair.
     */
    public ShnyPair(T first, V second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShnyPair<?, ?> that = (ShnyPair<?, ?>) o;
        return Objects.equals(first, that.first) &&
                Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return first + " " + second;
    }
}
