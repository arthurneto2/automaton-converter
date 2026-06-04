package br.ufv.sin141.application.parser;

import br.ufv.sin141.application.exception.MalformedGrammarException;
import br.ufv.sin141.domain.model.RegularGrammar;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrammarTextParserTest {

    private final GrammarTextParser parser = new GrammarTextParser();

    @Test
    void parsesSimpleRightLinearGrammar() {
        RegularGrammar g = parser.parse("S -> aS | a");

        assertEquals("S", g.getStartSymbol());
        assertTrue(g.getNonTerminals().contains("S"));
        assertTrue(g.getTerminals().contains("a"));
        assertEquals(2, g.getRules().size());
    }

    @Test
    void acceptsAllThreeEpsilonSpellings() {
        for (String eps : new String[]{"e", "ε", "&"}) {
            RegularGrammar g = parser.parse("S -> aS | " + eps);
            boolean hasEpsilon = g.getRules().stream().anyMatch(r -> r.isEpsilon());
            assertTrue(hasEpsilon, "should parse epsilon spelled as '" + eps + "'");
        }
    }

    @Test
    void multipleNonTerminalsAcrossLines() {
        RegularGrammar g = parser.parse("S -> aA | bB\nA -> a\nB -> b");

        assertEquals("S", g.getStartSymbol());
        assertTrue(g.getNonTerminals().containsAll(java.util.Set.of("S", "A", "B")));
        assertTrue(g.getTerminals().containsAll(java.util.Set.of("a", "b")));
    }

    @Test
    void rejectsMissingArrow() {
        assertThrows(MalformedGrammarException.class, () -> parser.parse("S aS"));
    }

    @Test
    void rejectsRhsLongerThanTerminalPlusNonTerminal() {
        assertThrows(MalformedGrammarException.class, () -> parser.parse("S -> abS"));
    }

    @Test
    void rejectsLeadingNonTerminalInRhs() {
        assertThrows(MalformedGrammarException.class, () -> parser.parse("S -> Sa"));
    }
}
