package br.ufv.sin141.application.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TransitionTableRow {

    private final String stateId;
    private final boolean initial;
    private final boolean accepting;
    private final Map<String, String> transitionsBySymbol;

    public TransitionTableRow(String stateId,
                              boolean initial,
                              boolean accepting,
                              Map<String, String> transitionsBySymbol) {
        this.stateId = stateId;
        this.initial = initial;
        this.accepting = accepting;
        this.transitionsBySymbol = Collections.unmodifiableMap(
                new LinkedHashMap<>(transitionsBySymbol));
    }

    public String getStateId() {
        return stateId;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public Map<String, String> getTransitionsBySymbol() {
        return transitionsBySymbol;
    }

    public String getDisplayId() {
        StringBuilder sb = new StringBuilder();
        if (initial) sb.append("→ ");
        if (accepting) sb.append("* ");
        sb.append(stateId);
        return sb.toString();
    }
}
