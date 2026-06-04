package br.ufv.sin141.domain.service;

import br.ufv.sin141.application.dto.SimulationTrace;
import br.ufv.sin141.application.exception.AlphabetViolationException;
import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationServiceTraceTest {

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
    void traceHasOneStepPerSymbol() {
        SimulationTrace trace = simulator.simulateWithTrace(endsWithA(), "bba");

        assertTrue(trace.isAccepted());
        assertEquals(3, trace.getSteps().size());
        assertNull(trace.getRejectionReason());
    }

    @Test
    void rejectionByNonAcceptingFinalStateHasReason() {
        SimulationTrace trace = simulator.simulateWithTrace(endsWithA(), "bb");

        assertFalse(trace.isAccepted());
        assertEquals(2, trace.getSteps().size());
        assertNotNull(trace.getRejectionReason());
    }

    @Test
    void symbolOutsideAlphabetThrows() {
        assertThrows(AlphabetViolationException.class,
                () -> simulator.simulateWithTrace(endsWithA(), "abc"));
    }
}
