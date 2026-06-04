package br.ufv.sin141.application.controller;

import br.ufv.sin141.application.dto.SimulationTrace;
import br.ufv.sin141.application.dto.TransitionTableRow;
import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.domain.model.RegularGrammar;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TheoryAppControllerTest {

    private final TheoryAppController app = new TheoryAppController();

    private Nfa endsWithA() {
        State q0 = new State("q0", true, false);
        State q1 = new State("q1", false, true);
        return new Nfa(
                Set.of(q0, q1),
                Set.of("a", "b"),
                Set.of(
                        new Transition(q0, "a", Set.of(q0, q1)),
                        new Transition(q0, "b", Set.of(q0))
                ),
                q0);
    }

    @Test
    void fullFlowConvertMinimizeSimulate() {
        app.setCurrentAutomaton(endsWithA());

        Dfa dfa = app.convertCurrentToDfa();
        assertNotNull(dfa);
        assertEquals(dfa, app.getCurrentDfa());

        Dfa minimized = app.minimizeCurrentDfa();
        assertNotNull(minimized);

        SimulationTrace accepted = app.simulateWord("bba");
        assertTrue(accepted.isAccepted());

        SimulationTrace rejected = app.simulateWord("bb");
        assertFalse(rejected.isAccepted());
    }

    @Test
    void tableRowsContainOnePerStateAndExposeAlphabetColumns() {
        app.setCurrentAutomaton(endsWithA());
        Nfa nfa = app.getCurrentAutomaton();

        List<TransitionTableRow> rows = app.asTableRows(nfa);

        assertEquals(nfa.getStates().size(), rows.size());
        for (TransitionTableRow row : rows) {
            assertTrue(row.getTransitionsBySymbol().containsKey("a"));
            assertTrue(row.getTransitionsBySymbol().containsKey("b"));
        }
    }

    @Test
    void grammarParsingAndFormattingRoundTrip() {
        RegularGrammar grammar = app.parseGrammar("S -> aS | a");

        assertEquals("S", grammar.getStartSymbol());
        String formatted = app.formatGrammar(grammar);
        assertTrue(formatted.startsWith("S -> "));
        assertTrue(formatted.contains("aS"));
    }

    @Test
    void grammarToAutomatonSetsSessionState() {
        RegularGrammar grammar = app.parseGrammar("S -> aS | a");
        Nfa nfa = app.grammarToAutomaton(grammar);

        assertNotNull(nfa);
        assertEquals(nfa, app.getCurrentAutomaton());
    }
}
