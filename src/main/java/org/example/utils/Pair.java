package org.example.utils;

// A simple generic Pair class for grouping two values.
public final class Pair<L, R> {

    // Using 'final' fields makes the class immutable
    private final L left;
    private final R right;

    // The constructor takes the two values
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    // Getter for the left value
    public L getLeft() {
        return left;
    }

    // Getter for the right value
    public R getRight() {
        return right;
    }

    // It's good practice to override toString() for easy printing
    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }

    // Also, override equals() and hashCode() for proper comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return left.equals(pair.left) && right.equals(pair.right);
    }

    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode();
    }
}
