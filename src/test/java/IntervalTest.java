/*
 * Copyright 2020 InfAI (CC SES)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;

public class IntervalTest {

    @Test
    public void testOpenInt() throws ParseException {
        Interval interval = new Interval("(0, 8)");

        Assert.assertTrue(interval.check(1));
        Assert.assertTrue(interval.check(2));
        Assert.assertTrue(interval.check(3));
        Assert.assertTrue(interval.check(4));
        Assert.assertTrue(interval.check(5));
        Assert.assertTrue(interval.check(6));
        Assert.assertTrue(interval.check(7));

        Assert.assertFalse(interval.check(-1));
        Assert.assertFalse(interval.check(0));
        Assert.assertFalse(interval.check(8));
        Assert.assertFalse(interval.check(9));
    }

    @Test
    public void testOpen2Int() throws ParseException {
        Interval interval = new Interval("]0, 8[");

        Assert.assertTrue(interval.check(1));
        Assert.assertTrue(interval.check(2));
        Assert.assertTrue(interval.check(3));
        Assert.assertTrue(interval.check(4));
        Assert.assertTrue(interval.check(5));
        Assert.assertTrue(interval.check(6));
        Assert.assertTrue(interval.check(7));

        Assert.assertFalse(interval.check(-1));
        Assert.assertFalse(interval.check(0));
        Assert.assertFalse(interval.check(8));
        Assert.assertFalse(interval.check(9));
    }

    @Test
    public void testClosedInt() throws ParseException {
        Interval interval = new Interval("[0, 8]");

        Assert.assertTrue(interval.check(0));
        Assert.assertTrue(interval.check(1));
        Assert.assertTrue(interval.check(2));
        Assert.assertTrue(interval.check(3));
        Assert.assertTrue(interval.check(4));
        Assert.assertTrue(interval.check(5));
        Assert.assertTrue(interval.check(6));
        Assert.assertTrue(interval.check(7));
        Assert.assertTrue(interval.check(8));

        Assert.assertFalse(interval.check(-1));
        Assert.assertFalse(interval.check(9));
    }

    @Test
    public void testClosedFloat() throws ParseException {
        Interval interval = new Interval("[1.3, 2.4]");

        Assert.assertEquals(interval.check(0), false);
        Assert.assertEquals(interval.check(1), false);
        Assert.assertEquals(interval.check(1.2), false);
        Assert.assertEquals(interval.check(1.3), true);
        Assert.assertEquals(interval.check(1.4), true);
        Assert.assertEquals(interval.check(2), true);
        Assert.assertEquals(interval.check(2.4), true);
        Assert.assertEquals(interval.check(2.5), false);
        Assert.assertEquals(interval.check(3), false);
        Assert.assertEquals(interval.check(4), false);
        Assert.assertEquals(interval.check(5), false);
    }

    @Test
    public void testOpenFloat() throws ParseException {
        Interval interval = new Interval("(1.3, 2.4)");

        Assert.assertEquals(interval.check(0), false);
        Assert.assertEquals(interval.check(1), false);
        Assert.assertEquals(interval.check(1.2), false);
        Assert.assertEquals(interval.check(1.3), false);
        Assert.assertEquals(interval.check(1.4), true);
        Assert.assertEquals(interval.check(2), true);
        Assert.assertEquals(interval.check(2.4), false);
        Assert.assertEquals(interval.check(2.5), false);
        Assert.assertEquals(interval.check(3), false);
        Assert.assertEquals(interval.check(4), false);
        Assert.assertEquals(interval.check(5), false);
    }

    @Test
    public void testOpen2Float() throws ParseException {
        Interval interval = new Interval("]1.3, 2.4[");

        Assert.assertEquals(interval.check(0), false);
        Assert.assertEquals(interval.check(1), false);
        Assert.assertEquals(interval.check(1.2), false);
        Assert.assertEquals(interval.check(1.3), false);
        Assert.assertEquals(interval.check(1.4), true);
        Assert.assertEquals(interval.check(2), true);
        Assert.assertEquals(interval.check(2.4), false);
        Assert.assertEquals(interval.check(2.5), false);
        Assert.assertEquals(interval.check(3), false);
        Assert.assertEquals(interval.check(4), false);
        Assert.assertEquals(interval.check(5), false);
    }

    @Test
    public void testOpenInfinity() throws ParseException {
        Interval interval = new Interval("(*, *)");
        Assert.assertEquals(interval.check(0), true);
        Assert.assertEquals(interval.check(1), true);
        Assert.assertEquals(interval.check(1.2), true);
        Assert.assertEquals(interval.check(1.3), true);
        Assert.assertEquals(interval.check(1.4), true);
        Assert.assertEquals(interval.check(2), true);
        Assert.assertEquals(interval.check(2.4), true);
        Assert.assertEquals(interval.check(2.5), true);
        Assert.assertEquals(interval.check(3), true);
        Assert.assertEquals(interval.check(4), true);
        Assert.assertEquals(interval.check(5), true);
    }

    @Test
    public void testOpen2Infinity() throws ParseException {
        Interval interval = new Interval("]*, *[");
        Assert.assertEquals(interval.check(0), true);
        Assert.assertEquals(interval.check(1), true);
        Assert.assertEquals(interval.check(1.2), true);
        Assert.assertEquals(interval.check(1.3), true);
        Assert.assertEquals(interval.check(1.4), true);
        Assert.assertEquals(interval.check(2), true);
        Assert.assertEquals(interval.check(2.4), true);
        Assert.assertEquals(interval.check(2.5), true);
        Assert.assertEquals(interval.check(3), true);
        Assert.assertEquals(interval.check(4), true);
        Assert.assertEquals(interval.check(5), true);
    }

    @Test
    public void testClosedInfinity() throws ParseException {
        Interval interval = new Interval("[*, *]");
        Assert.assertEquals(interval.check(0), true);
        Assert.assertEquals(interval.check(1), true);
        Assert.assertEquals(interval.check(1.2), true);
        Assert.assertEquals(interval.check(1.3), true);
        Assert.assertEquals(interval.check(1.4), true);
        Assert.assertEquals(interval.check(2), true);
        Assert.assertEquals(interval.check(2.4), true);
        Assert.assertEquals(interval.check(2.5), true);
        Assert.assertEquals(interval.check(3), true);
        Assert.assertEquals(interval.check(4), true);
        Assert.assertEquals(interval.check(5), true);
    }
}
