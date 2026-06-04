package br.ufv.sin141.application.controller;

import br.ufv.sin141.application.dto.SimulationTrace;
import br.ufv.sin141.application.dto.TransitionTableRow;
import br.ufv.sin141.application.parser.GrammarTextParser;
import br.ufv.sin141.domain.model.Automaton;
import br.ufv.sin141.domain.model.Dfa;
import br.ufv.sin141.domain.model.Nfa;
import br.ufv.sin141.domain.model.ProductionRule;
import br.ufv.sin141.domain.model.RegularGrammar;
import br.ufv.sin141.domain.model.State;
import br.ufv.sin141.domain.model.Transition;
import br.ufv.sin141.domain.service.GrammarConverterService;
import br.ufv.sin141.domain.service.HopcroftMinimizationService;
import br.ufv.sin141.domain.service.SimulationService;
import br.ufv.sin141.domain.service.SubsetConstructionService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TheoryAppController {

    private final SubsetConstructionService subsetConstructionService;
    private final HopcroftMinimizationService hopcroftMinimizationService;
    private final GrammarConverterService grammarConverterService;
    private final SimulationService simulationService;
    private final GrammarTextParser grammarTextParser;

    private Nfa currentAutomaton;
    private RegularGrammar currentGrammar;
    private Dfa currentDfa;
    private Dfa minimizedDfa;

    public TheoryAppController() {
        this(new SubsetConstructionService(),
             new HopcroftMinimizationService(),
             new GrammarConverterService(),
             new SimulationService(),
             new GrammarTextParser());
    }

    public TheoryAppController(SubsetConstructionService subsetConstructionService,
                               HopcroftMinimizationService hopcroftMinimizationService,
                               GrammarConverterService grammarConverterService,
                               SimulationService simulationService,
                               GrammarTextParser grammarTextParser) {
        this.subsetConstructionService = subsetConstructionService;
        this.hopcroftMinimizationService = hopcroftMinimizationService;
        this.grammarConverterService = grammarConverterService;
        this.simulationService = simulationService;
        this.grammarTextParser = grammarTextParser;
    }

    public Nfa getCurrentAutomaton() {
        return currentAutomaton;
    }

    public void setCurrentAutomaton(Nfa automaton) {
        this.currentAutomaton = automaton;
        this.currentDfa = null;
        this.minimizedDfa = null;
    }

    public RegularGrammar getCurrentGrammar() {
        return currentGrammar;
    }

    public Dfa getCurrentDfa() {
        return currentDfa;
    }

    public Dfa getMinimizedDfa() {
        return minimizedDfa;
    }

    public Dfa convertCurrentToDfa() {
        requireCurrentAutomaton();
        currentDfa = subsetConstructionService.convertNfaToDfa(currentAutomaton);
        return currentDfa;
    }

    public Dfa minimizeCurrentDfa() {
        if (currentDfa == null) {
            convertCurrentToDfa();
        }
        minimizedDfa = hopcroftMinimizationService.minimizeDfa(currentDfa);
        return minimizedDfa;
    }

    public SimulationTrace simulateWord(String word) {
        Dfa dfa = minimizedDfa != null ? minimizedDfa
                : currentDfa != null ? currentDfa
                : convertCurrentToDfa();
        return simulationService.simulateWithTrace(dfa, word);
    }

    public RegularGrammar parseGrammar(String text) {
        currentGrammar = grammarTextParser.parse(text);
        return currentGrammar;
    }

    public Nfa grammarToAutomaton(RegularGrammar grammar) {
        Automaton automaton = grammarConverterService.toAutomaton(grammar);
        Nfa nfa = (Nfa) automaton;
        setCurrentAutomaton(nfa);
        currentGrammar = grammar;
        return nfa;
    }

    public RegularGrammar automatonToGrammar(Automaton automaton) {
        RegularGrammar grammar = grammarConverterService.toGrammar(automaton);
        currentGrammar = grammar;
        return grammar;
    }

    public List<TransitionTableRow> asTableRows(Automaton automaton) {
        List<String> orderedSymbols = orderedSymbols(automaton);
        List<TransitionTableRow> rows = new ArrayList<>();
        for (State state : orderedStates(automaton)) {
            Map<String, String> bySymbol = new LinkedHashMap<>();
            for (String symbol : orderedSymbols) {
                String label = labelFor(automaton, state, symbol);
                bySymbol.put(symbol.isEmpty() ? "ε" : symbol, label);
            }
            rows.add(new TransitionTableRow(state.getId(), state.isInitial(), state.isAccepting(), bySymbol));
        }
        return rows;
    }

    public List<String> orderedSymbols(Automaton automaton) {
        List<String> symbols = new ArrayList<>(automaton.getAlphabet());
        symbols.sort((a, b) -> {
            if (a.equals(Transition.EPSILON)) return 1;
            if (b.equals(Transition.EPSILON)) return -1;
            return a.compareTo(b);
        });
        boolean hasEpsilonTransition = automaton.getTransitions().stream().anyMatch(Transition::isEpsilon);
        if (hasEpsilonTransition && !symbols.contains(Transition.EPSILON)) {
            symbols.add(Transition.EPSILON);
        }
        return symbols;
    }

    public List<State> orderedStates(Automaton automaton) {
        List<State> states = new ArrayList<>(automaton.getStates());
        states.sort((a, b) -> {
            if (a.equals(automaton.getInitialState())) return -1;
            if (b.equals(automaton.getInitialState())) return 1;
            return a.getId().compareTo(b.getId());
        });
        return states;
    }

    public String formatGrammar(RegularGrammar grammar) {
        Map<String, List<String>> alternativesByLhs = new TreeMap<>();
        for (ProductionRule rule : grammar.getRules()) {
            String body;
            if (rule.isEpsilon()) {
                body = "ε";
            } else if (rule.isTerminalOnly()) {
                body = rule.getRightHandSideTerminal();
            } else {
                body = rule.getRightHandSideTerminal() + rule.getRightHandSideNonTerminal();
            }
            alternativesByLhs.computeIfAbsent(rule.getLeftHandSide(), k -> new ArrayList<>()).add(body);
        }

        StringBuilder sb = new StringBuilder();
        String start = grammar.getStartSymbol();
        if (alternativesByLhs.containsKey(start)) {
            sb.append(start).append(" -> ")
              .append(String.join(" | ", alternativesByLhs.get(start)))
              .append("\n");
        }
        for (Map.Entry<String, List<String>> entry : alternativesByLhs.entrySet()) {
            if (entry.getKey().equals(start)) continue;
            sb.append(entry.getKey()).append(" -> ")
              .append(String.join(" | ", entry.getValue()))
              .append("\n");
        }
        return sb.toString().trim();
    }

    private String labelFor(Automaton automaton, State state, String symbol) {
        var targets = automaton.getTransitionsFrom(state, symbol);
        if (targets.isEmpty()) return "—";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (State t : targets) {
            if (!first) sb.append(", ");
            sb.append(t.getId());
            first = false;
        }
        return sb.toString();
    }

    private void requireCurrentAutomaton() {
        if (currentAutomaton == null) {
            throw new IllegalStateException("No automaton has been defined yet");
        }
    }

    public SubsetConstructionService getSubsetConstructionService() {
        return subsetConstructionService;
    }

    public HopcroftMinimizationService getHopcroftMinimizationService() {
        return hopcroftMinimizationService;
    }

    public GrammarConverterService getGrammarConverterService() {
        return grammarConverterService;
    }

    public SimulationService getSimulationService() {
        return simulationService;
    }
}
