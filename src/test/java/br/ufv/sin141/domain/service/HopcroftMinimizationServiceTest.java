package br.ufv.sin141.domain.service;

import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HopcroftMinimizationServiceTest {

    private final SubsetConstructionService subset = new SubsetConstructionService();
    private final HopcroftMinimizationService minimizer = new HopcroftMinimizationService();
    private final SimulationService simulator = new SimulationService();

    @Test
    void mergesEquivalentStates() {
        State q0 = new State("q0", true, false);
        State q1 = new State("q1", false, false);
        State q2 = new State("q2", false, true);
        State q3 = new State("q3", false, true);
        Dfa dfa = new Dfa(
                Set.of(q0, q1, q2, q3),
                Set.of("a"),
                Set.of(
                        new Transition(q0, "a", Set.of(q1)),
                        new Transition(q1, "a", Set.of(q2)),
                        new Transition(q2, "a", Set.of(q3)),
                        new Transition(q3, "a", Set.of(q2))
                ),
                q0);

        Dfa minimized = minimizer.minimizeDfa(dfa);

        assertTrue(minimized.getStates().size() < dfa.getStates().size(),
                "equivalent accepting states q2 and q3 must merge");
    }

    @Test
    void minimizedDfaAcceptsSameLanguage() {
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

        Dfa original = subset.convertNfaToDfa(nfa);
        Dfa minimized = minimizer.minimizeDfa(original);

        for (String word : new String[]{"", "a", "b", "ba", "ab", "aba", "bbb", "babba"}) {
            assertEquals(simulator.simulate(original, word),
                         simulator.simulate(minimized, word),
                         "language must be preserved for word: '" + word + "'");
        }
    }

    @Test
    void dropsUnreachableStates() {
        State q0 = new State("q0", true, true);
        State unreachable = new State("ghost", false, true);
        Dfa dfa = new Dfa(
                Set.of(q0, unreachable),
                Set.of("a"),
                Set.of(new Transition(q0, "a", Set.of(q0))),
                q0);

        Dfa minimized = minimizer.minimizeDfa(dfa);

        assertFalse(minimized.getStates().stream().anyMatch(s -> s.getId().contains("ghost")));
    }
}
