package br.ufv.sin141.domain.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class Automaton {

    private final Set<State> states;
    private final Set<String> alphabet;
    private final Set<Transition> transitions;
    private final State initialState;

    protected Automaton(Set<State> states,
                        Set<String> alphabet,
                        Set<Transition> transitions,
                        State initialState) {
        this.states = Set.copyOf(Objects.requireNonNull(states, "states"));
        this.alphabet = Set.copyOf(Objects.requireNonNull(alphabet, "alphabet"));
        this.transitions = Set.copyOf(Objects.requireNonNull(transitions, "transitions"));
        this.initialState = Objects.requireNonNull(initialState, "initialState");
    }

    public Set<State> getStates() {
        return states;
    }

    public Set<String> getAlphabet() {
        return alphabet;
    }

    public Set<Transition> getTransitions() {
        return transitions;
    }

    public State getInitialState() {
        return initialState;
    }

    public Set<State> getAcceptingStates() {
        Set<State> accepting = new HashSet<>();
        for (State state : states) {
            if (state.isAccepting()) {
                accepting.add(state);
            }
        }
        return Collections.unmodifiableSet(accepting);
    }

    public Set<State> getTransitionsFrom(State origin, String symbol) {
        Set<State> targets = new HashSet<>();
        for (Transition transition : transitions) {
            if (transition.getOrigin().equals(origin) && transition.getSymbol().equals(symbol)) {
                targets.addAll(transition.getTargets());
            }
        }
        return Collections.unmodifiableSet(targets);
    }
}
