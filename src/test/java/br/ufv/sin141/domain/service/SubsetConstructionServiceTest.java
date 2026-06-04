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

class SubsetConstructionServiceTest {

    private final SubsetConstructionService service = new SubsetConstructionService();

    @Test
    void convertsTrivialNfaToEquivalentDfa() {
        State q0 = new State("q0", true, false);
        State q1 = new State("q1", false, true);
        Nfa nfa = new Nfa(
                Set.of(q0, q1),
                Set.of("a"),
                Set.of(new Transition(q0, "a", Set.of(q1))),
                q0);

        Dfa dfa = service.convertNfaToDfa(nfa);

        assertTrue(dfa.getInitialState().getId().contains("q0"));
        assertEquals(Set.of("a"), dfa.getAlphabet());
        assertFalse(dfa.getAcceptingStates().isEmpty());
    }

    @Test
    void epsilonTransitionsAreClosedAtStart() {
        State q0 = new State("q0", true, false);
        State q1 = new State("q1", false, true);
        Nfa nfa = new Nfa(
                Set.of(q0, q1),
                Set.of("a"),
                Set.of(new Transition(q0, Transition.EPSILON, Set.of(q1))),
                q0);

        Dfa dfa = service.convertNfaToDfa(nfa);

        assertTrue(dfa.getInitialState().isAccepting(),
                "initial subset must contain q1 via epsilon and therefore be accepting");
    }

    @Test
    void nondeterministicChoiceIsDeterminized() {
        State q0 = new State("q0", true, false);
        State q1 = new State("q1", false, false);
        State q2 = new State("q2", false, true);
        Nfa nfa = new Nfa(
                Set.of(q0, q1, q2),
                Set.of("a"),
                Set.of(
                        new Transition(q0, "a", Set.of(q1, q2))
                ),
                q0);

        Dfa dfa = service.convertNfaToDfa(nfa);

        for (Transition t : dfa.getTransitions()) {
            assertEquals(1, t.getTargets().size(),
                    "every DFA transition must have a single target");
        }
    }

    @Test
    void missingTransitionsRouteToDeadState() {
        State q0 = new State("q0", true, true);
        Nfa nfa = new Nfa(
                Set.of(q0),
                Set.of("a", "b"),
                Set.of(new Transition(q0, "a", Set.of(q0))),
                q0);

        Dfa dfa = service.convertNfaToDfa(nfa);

        boolean hasDead = dfa.getStates().stream().anyMatch(s -> s.getId().equals("{}"));
        assertTrue(hasDead, "DFA must include a dead state when transitions are missing");
    }
}
