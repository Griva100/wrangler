/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives.aggregates;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.TestingRig;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests {@link AggregateSizeAndTime}
*/

public class AggregateSizeAndTimeTest {

    @Test
    public void testAggregateStats() throws Exception {
        List<Row> rows = createSampleData();

        String[] recipe = new String[] {
            "aggregateSizeAndTime :size :time :total_size_mb :total_time_s 'MB' 's' 'sum'"
        };

        List<Row> results = TestingRig.execute(recipe, rows);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(0.0009765625,  ((Number)results.get(0).getValue("total_size_mb")).doubleValue(), 0.001);
        Assert.assertEquals(0.2,  ((Number)results.get(0).getValue("total_time_s")).doubleValue(), 0.000000001);
    }

    @Test
    public void testAggregateStatsWithDifferentUnits() throws Exception {
        List<Row> rows = createSampleData();

        String[] recipe = new String[] {
            "aggregateSizeAndTime :size :time :total_size_kb :total_time_ms 'KB' 'min' 'sum'"
        };

        List<Row> results = TestingRig.execute(recipe, rows);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(1.0, ((Number) results.get(0).getValue("total_size_kb")).doubleValue(), 0.001);
        Assert.assertEquals(0.0033333333333333335, ((Number) results.get(0).getValue("total_time_ms")).doubleValue(), 0.000000001);
    }

    private List<Row> createSampleData() {
        List<Row> rows = new ArrayList<>();

        Row row1 = new Row();
        row1.add("size", "1024"); // bytes
        row1.add("time", "200ms");

        rows.add(row1);
        
        return rows;
    }
}
