package br.ufv.sin141.domain.model;

import java.util.Objects;

public final class State {

    private final String id;
    private final boolean initial;
    private final boolean accepting;

    public State(String id, boolean initial, boolean accepting) {
        this.id = Objects.requireNonNull(id, "id");
        this.initial = initial;
        this.accepting = accepting;
    }

    public String getId() {
        return id;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isAccepting() {
        return accepting;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof State that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
