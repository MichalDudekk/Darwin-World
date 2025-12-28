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
        assertEquals("(1, 2)",v.toString());
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

    @Test
    void calculateDistanceReturnsCorrectEuclideanDistance() {
        // given
        Vector2d v1 = new Vector2d(0, 0);
        Vector2d v2 = new Vector2d(3, 4); // pythagorean triangle 3-4-5

        // when
        double distance = v1.calculateDistance(v2);

        // then
        assertEquals(5.0, distance, 0.001);
    }

    @Test
    void followTargetReturnsCorrectUnitVectors() {
        // given
        Vector2d current = new Vector2d(5, 5);

        // then
        assertEquals(new Vector2d(1, 1), current.followTarget(new Vector2d(10, 10)));

        assertEquals(new Vector2d(0, -1), current.followTarget(new Vector2d(5, 0)));

        assertEquals(new Vector2d(-1, 0), current.followTarget(new Vector2d(0, 5)));

        assertEquals(new Vector2d(0, 0), current.followTarget(new Vector2d(5, 5)));
    }

    @Test
    void hashCodeIsConsistentWithEquals() {
        // given
        Vector2d v1 = new Vector2d(10, 20);
        Vector2d v2 = new Vector2d(10, 20);

        // then
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode(), "Same vectors must have same hash codes");
    }

    @Test
    void addAndSubtractWorkWithNegativeValues() {
        // given
        Vector2d v = new Vector2d(-1, -1);
        Vector2d u = new Vector2d(5, -2);

        // when
        Vector2d added = v.add(u);
        Vector2d subtracted = v.subtract(u);

        // then
        assertEquals(new Vector2d(4, -3), added);
        assertEquals(new Vector2d(-6, 1), subtracted);
    }
}