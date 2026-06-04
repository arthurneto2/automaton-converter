package br.ufv.sin141.domain.model;

import java.util.Set;

public final class Nfa extends Automaton {

    public Nfa(Set<State> states,
               Set<String> alphabet,
               Set<Transition> transitions,
               State initialState) {
        super(states, alphabet, transitions, initialState);
    }
}
