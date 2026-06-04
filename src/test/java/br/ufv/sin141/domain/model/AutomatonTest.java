package br.ufv.sin141.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutomatonTest {

    @Test
    void transitionsFromUnknownKeyReturnsEmptySetNotNull() {
        State q0 = new State("q0", true, true);
        Nfa nfa = new Nfa(Set.of(q0), Set.of("a"), Set.of(), q0);

        Set<State> targets = nfa.getTransitionsFrom(q0, "a");

        assertNotNull(targets);
        assertTrue(targets.isEmpty());
    }

    @Test
    void acceptingStatesIncludesOnlyAcceptingStates() {
        State q0 = new State("q0", true, false);
        State q1 = new State("q1", false, true);
        Nfa nfa = new Nfa(Set.of(q0, q1), Set.of("a"), Set.of(), q0);

        assertTrue(nfa.getAcceptingStates().contains(q1));
        assertTrue(nfa.getAcceptingStates().size() == 1);
    }
}
