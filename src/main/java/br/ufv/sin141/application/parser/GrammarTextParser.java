package br.ufv.sin141.application.parser;

import br.ufv.sin141.application.exception.MalformedGrammarException;
import br.ufv.sin141.domain.model.ProductionRule;
import br.ufv.sin141.domain.model.RegularGrammar;

import java.util.LinkedHashSet;
import java.util.Set;

public class GrammarTextParser {

    public RegularGrammar parse(String text) {
        if (text == null || text.isBlank()) {
            throw new MalformedGrammarException("Grammar text is empty");
        }

        Set<String> nonTerminals = new LinkedHashSet<>();
        Set<String> terminals = new LinkedHashSet<>();
        Set<ProductionRule> rules = new LinkedHashSet<>();
        String startSymbol = null;

        String[] lines = text.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            String[] sides = line.split("->|→", 2);
            if (sides.length != 2) {
                throw new MalformedGrammarException("Missing '->' in line: " + rawLine);
            }
            String lhs = sides[0].trim();
            if (!isNonTerminal(lhs)) {
                throw new MalformedGrammarException(
                        "Left-hand side must be a single uppercase letter: '" + lhs + "'");
            }
            nonTerminals.add(lhs);
            if (startSymbol == null) startSymbol = lhs;

            String[] alternatives = sides[1].split("\\|");
            for (String alt : alternatives) {
                String body = alt.trim();
                if (body.isEmpty()) {
                    throw new MalformedGrammarException("Empty alternative in line: " + rawLine);
                }
                rules.add(parseAlternative(lhs, body, nonTerminals, terminals, rawLine));
            }
        }

        return new RegularGrammar(nonTerminals, terminals, rules, startSymbol);
    }

    private ProductionRule parseAlternative(String lhs,
                                            String body,
                                            Set<String> nonTerminals,
                                            Set<String> terminals,
                                            String rawLine) {
        if (isEpsilon(body)) {
            return new ProductionRule(lhs, null, null);
        }

        if (body.length() == 1) {
            String terminal = body;
            if (!isTerminal(terminal)) {
                throw new MalformedGrammarException(
                        "Terminal must be lowercase or digit: '" + terminal + "' in '" + rawLine + "'");
            }
            terminals.add(terminal);
            return new ProductionRule(lhs, terminal, null);
        }

        if (body.length() == 2) {
            String terminal = body.substring(0, 1);
            String nonTerminal = body.substring(1, 2);
            if (!isTerminal(terminal)) {
                throw new MalformedGrammarException(
                        "Terminal must come first and be lowercase/digit: '" + body + "' in '" + rawLine + "'");
            }
            if (!isNonTerminal(nonTerminal)) {
                throw new MalformedGrammarException(
                        "Non-terminal must be uppercase: '" + body + "' in '" + rawLine + "'");
            }
            terminals.add(terminal);
            nonTerminals.add(nonTerminal);
            return new ProductionRule(lhs, terminal, nonTerminal);
        }

        throw new MalformedGrammarException(
                "Production '" + body + "' is not a regular form (expected a, aB, or epsilon)");
    }

    private boolean isEpsilon(String token) {
        return token.equals("e") || token.equals("ε") || token.equals("&") || token.equals("E");
    }

    private boolean isNonTerminal(String token) {
        return token.length() == 1 && Character.isUpperCase(token.charAt(0));
    }

    private boolean isTerminal(String token) {
        if (token.length() != 1) return false;
        char c = token.charAt(0);
        return Character.isLowerCase(c) || Character.isDigit(c);
    }
}
