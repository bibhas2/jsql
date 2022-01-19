package com.webage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    @Test
    public void argSuccess() {
        String[] args = {"-aaa", "bbb", "-ccc", "ddd", "eee"};
        var v = JSQL.getArgValue(args, "-ccc");

        assertEquals("ddd", v.orElse(""));
    }
    @Test
    public void argMissingValue() {
        String[] args = {"-aaa", "bbb", "-ccc"};
        var v = JSQL.getArgValue(args, "-ccc");

        assertTrue(v.isEmpty());
    }
    @Test
    public void argMissingArg() {
        String[] args = {"-aaa", "bbb", "ddd"};
        var v = JSQL.getArgValue(args, "-ccc");

        assertTrue(v.isEmpty());
    }
    @Test
    public void argExists() {
        String[] args = {"-aaa", "bbb", "-ccc", "ddd", "eee"};

        assertTrue(JSQL.hasArg(args, "-ccc"));
        assertFalse(JSQL.hasArg(args, "-ggg"));
    }
}
