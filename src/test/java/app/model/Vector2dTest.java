package app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector2dTest {

    @Test
    void equalsReturnsFalseIfOtherIsNotAnInstanceOfVector2d () {
        // when
        Vector2d v = new Vector2d(1,2);
        Vector2d u = new Vector2d(1,2);
        Vector2d w = new Vector2d(1,-2);
        int x = 1;
        String y = "not a vector";

        // then
        assertEquals(v, u);
        assertFalse(v.equals(w));
        assertFalse(v.equals(x));
        assertFalse(v.equals(y));
    }

    @Test
    void toStringReturnsCorrectString () {
        // when
        Vector2d v = new Vector2d(1,2);

        // then
        assertEquals("(1,2)",v.toString());
    }

    @Test
    void vectorFollowsNewVectorAndFollowsSelf () {
        // when
        Vector2d v = new Vector2d(1,2);
        Vector2d u = new Vector2d(2,3);
        Vector2d w = new Vector2d(1,1);

        // then
        assertTrue(v.follows(v));
        assertTrue(v.follows(w));
        assertFalse(v.follows(u));
    }

    @Test
    void vectorPrecedesNewVectorAndPrecedesSelf () {
        // when
        Vector2d v = new Vector2d(1,2);
        Vector2d u = new Vector2d(2,3);
        Vector2d w = new Vector2d(1,1);

        // then
        assertTrue(v.precedes(v));
        assertTrue(v.precedes(u));
        assertFalse(v.precedes(w));
    }

    @Test
    void AddAndSubtractOtherVector () {
        // given
        Vector2d v = new Vector2d(1,2);
        Vector2d u = new Vector2d(2,3);

        // when
        Vector2d w = v.add(u);
        Vector2d h = v.subtract(u);

        // then
        assertTrue(w.getX() == 3 && w.getY() == 5);
        assertTrue(h.getX() == -1 && h.getY() == -1);
    }
}