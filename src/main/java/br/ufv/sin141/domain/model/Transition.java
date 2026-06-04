package br.ufv.sin141.domain.model;

import java.util.Objects;
import java.util.Set;

public final class Transition {

    public static final String EPSILON = "";

    private final State origin;
    private final String symbol;
    private final Set<State> targets;

    public Transition(State origin, String symbol, Set<State> targets) {
        this.origin = Objects.requireNonNull(origin, "origin");
        this.symbol = Objects.requireNonNull(symbol, "symbol");
        this.targets = Set.copyOf(Objects.requireNonNull(targets, "targets"));
    }

    public State getOrigin() {
        return origin;
    }

    public String getSymbol() {
        return symbol;
    }

    public Set<State> getTargets() {
        return targets;
    }

    public boolean isEpsilon() {
        return symbol.equals(EPSILON);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Transition that)) return false;
        return origin.equals(that.origin)
                && symbol.equals(that.symbol)
                && targets.equals(that.targets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, symbol, targets);
    }
}
