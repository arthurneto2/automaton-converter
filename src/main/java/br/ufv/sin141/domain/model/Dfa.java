package br.ufv.sin141.domain.model;

import java.util.Set;

public final class Dfa extends Automaton {

    public Dfa(Set<State> states,
               Set<String> alphabet,
               Set<Transition> transitions,
               State initialState) {
        super(states, alphabet, transitions, initialState);
        for (Transition transition : transitions) {
            if (transition.isEpsilon()) {
                throw new IllegalArgumentException(
                        "DFA cannot contain epsilon transitions: " + transition.getOrigin());
            }
            if (transition.getTargets().size() > 1) {
                throw new IllegalArgumentException(
                        "DFA transition must have a single target: " + transition.getOrigin()
                                + " --" + transition.getSymbol() + "--> " + transition.getTargets());
            }
        }
    }
}
