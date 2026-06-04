package br.ufv.sin141.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class StateTest {

    @Test
    void statesWithSameIdAreEqual() {
        State a = new State("q0", true, false);
        State b = new State("q0", false, true);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void statesWithDifferentIdsAreNotEqual() {
        State a = new State("q0", true, false);
        State b = new State("q1", true, false);

        assertNotEquals(a, b);
    }
}
