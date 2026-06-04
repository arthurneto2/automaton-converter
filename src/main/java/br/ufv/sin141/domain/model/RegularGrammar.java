package br.ufv.sin141.domain.model;

import java.util.Objects;
import java.util.Set;

public final class RegularGrammar {

    private final Set<String> nonTerminals;
    private final Set<String> terminals;
    private final Set<ProductionRule> rules;
    private final String startSymbol;

    public RegularGrammar(Set<String> nonTerminals,
                          Set<String> terminals,
                          Set<ProductionRule> rules,
                          String startSymbol) {
        this.nonTerminals = Set.copyOf(Objects.requireNonNull(nonTerminals, "nonTerminals"));
        this.terminals = Set.copyOf(Objects.requireNonNull(terminals, "terminals"));
        this.rules = Set.copyOf(Objects.requireNonNull(rules, "rules"));
        this.startSymbol = Objects.requireNonNull(startSymbol, "startSymbol");
    }

    public Set<String> getNonTerminals() {
        return nonTerminals;
    }

    public Set<String> getTerminals() {
        return terminals;
    }

    public Set<ProductionRule> getRules() {
        return rules;
    }

    public String getStartSymbol() {
        return startSymbol;
    }
}
