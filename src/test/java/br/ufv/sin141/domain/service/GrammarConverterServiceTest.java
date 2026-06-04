package br.ufv.sin141.domain.service;

import br.ufv.sin141.application.exception.MalformedGrammarException;
import br.ufv.sin141.domain.model.Automaton;
import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.domain.model.ProductionRule;
import br.ufv.sin141.domain.model.RegularGrammar;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrammarConverterServiceTest {

    private final GrammarConverterService converter = new GrammarConverterService();
    private final SubsetConstructionService subset = new SubsetConstructionService();
    private final SimulationService simulator = new SimulationService();

    @Test
    void grammarToAutomatonRecognizesSameLanguage() {
        // S -> aS | a   accepts a+
        RegularGrammar grammar = new RegularGrammar(
                Set.of("S"),
                Set.of("a"),
                Set.of(
                        new ProductionRule("S", "a", "S"),
                        new ProductionRule("S", "a", null)
                ),
                "S");

        Automaton automaton = converter.toAutomaton(grammar);
        Dfa dfa = subset.convertNfaToDfa((Nfa) automaton);

        assertTrue(simulator.simulate(dfa, "a"));
        assertTrue(simulator.simulate(dfa, "aaa"));
        assertFalse(simulator.simulate(dfa, ""));
    }

    @Test
    void epsilonProductionForStartSymbolMarksInitialAccepting() {
        // S -> aS | epsilon   accepts a*
        RegularGrammar grammar = new RegularGrammar(
                Set.of("S"),
                Set.of("a"),
                Set.of(
                        new ProductionRule("S", "a", "S"),
                        new ProductionRule("S", null, null)
                ),
                "S");

        Automaton automaton = converter.toAutomaton(grammar);
        Dfa dfa = subset.convertNfaToDfa((Nfa) automaton);

        assertTrue(simulator.simulate(dfa, ""));
        assertTrue(simulator.simulate(dfa, "aaaa"));
    }

    @Test
    void automatonToGrammarRoundTrip() {
        // simple NFA accepting a+ : q0 --a--> q1(accept), q1 --a--> q1
        State q0 = new State("q0", true, false);
        State q1 = new State("q1", false, true);
        Nfa nfa = new Nfa(
                Set.of(q0, q1),
                Set.of("a"),
                Set.of(
                        new Transition(q0, "a", Set.of(q1)),
                        new Transition(q1, "a", Set.of(q1))
                ),
                q0);

        RegularGrammar grammar = converter.toGrammar(nfa);

        assertTrue(grammar.getNonTerminals().contains("q0"));
        assertTrue(grammar.getNonTerminals().contains("q1"));
        boolean hasTerminalOnly = grammar.getRules().stream().anyMatch(ProductionRule::isTerminalOnly);
        assertTrue(hasTerminalOnly, "accepting target must produce a terminal-only rule");
    }

    @Test
    void malformedGrammarRejected() {
        // production uses non-declared terminal 'b'
        RegularGrammar grammar = new RegularGrammar(
                Set.of("S"),
                Set.of("a"),
                Set.of(new ProductionRule("S", "b", null)),
                "S");

        assertThrows(MalformedGrammarException.class, () -> converter.toAutomaton(grammar));
    }
}
