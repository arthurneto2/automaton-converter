package br.ufv.sin141.domain.service;

import br.ufv.sin141.application.exception.AlphabetViolationException;
import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationServiceTest {

    private final SubsetConstructionService subset = new SubsetConstructionService();
    private final SimulationService simulator = new SimulationService();

    private Dfa endsWithA() {
        State q0 = new State("q0", true, false);
        State q1 = new State("q1", false, true);
        Nfa nfa = new Nfa(
                Set.of(q0, q1),
                Set.of("a", "b"),
                Set.of(
                        new Transition(q0, "a", Set.of(q0, q1)),
                        new Transition(q0, "b", Set.of(q0))
                ),
                q0);
        return subset.convertNfaToDfa(nfa);
    }

    @Test
    void acceptsWordEndingInA() {
        assertTrue(simulator.simulate(endsWithA(), "bba"));
        assertTrue(simulator.simulate(endsWithA(), "a"));
        assertTrue(simulator.simulate(endsWithA(), "ababbba"));
    }

    @Test
    void rejectsWordNotEndingInA() {
        assertFalse(simulator.simulate(endsWithA(), "bb"));
        assertFalse(simulator.simulate(endsWithA(), "ab"));
    }

    @Test
    void emptyWordAcceptedOnlyIfInitialIsAccepting() {
        State q0 = new State("q0", true, true);
        Nfa nfa = new Nfa(Set.of(q0), Set.of("a"), Set.of(), q0);
        Dfa dfa = subset.convertNfaToDfa(nfa);

        assertTrue(simulator.simulate(dfa, ""));
    }

    @Test
    void rejectsWordWithSymbolOutsideAlphabet() {
        assertThrows(AlphabetViolationException.class,
                () -> simulator.simulate(endsWithA(), "abc"));
    }
}
