/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy o-f
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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.*;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * A directive that aggregates byte size and time duration columns.
 */
@Plugin(type = Directive.TYPE)
@Name("aggregateSizeAndTime")
@Description("Aggregates byte size and time duration columns and returns totals or averages in the desired units.")
public class AggregateSizeAndTime implements Directive {

    private String sizeCol; 
    private String timeCol;
    private String targetSizeCol;
    private String targetTimeCol;
    private String sizeUnit = "bytes";
    private String timeUnit = "ns";
    private String aggregationType = "total";

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregateSizeAndTime");

        builder.define("sizeColumn", TokenType.COLUMN_NAME);
        builder.define("timeColumn", TokenType.COLUMN_NAME);
        builder.define("targetSizeColumn", TokenType.COLUMN_NAME);
        builder.define("targetTimeColumn", TokenType.COLUMN_NAME);
        builder.define("sizeUnit", TokenType.TEXT);
        builder.define("timeUnit", TokenType.TEXT);
        builder.define("aggregationType", TokenType.TEXT);

        return builder.build();
    }

    @Override
    public void initialize(Arguments arguments) throws DirectiveParseException {
        this.sizeCol = ((ColumnName) arguments.value("sizeColumn")).value();
        this.timeCol = ((ColumnName) arguments.value("timeColumn")).value();
        this.targetSizeCol = ((ColumnName) arguments.value("targetSizeColumn")).value();
        this.targetTimeCol = ((ColumnName) arguments.value("targetTimeColumn")).value();

        if (arguments.contains("sizeUnit")) {
            this.sizeUnit = arguments.value("sizeUnit").value().toString().toLowerCase();
        }
        if (arguments.contains("timeUnit")) {
            this.timeUnit = arguments.value("timeUnit").value().toString().toLowerCase();
        }
        if (arguments.contains("aggregationType")) {
            this.aggregationType = arguments.value("aggregationType").value().toString().toLowerCase();
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) {
        // Initialize aggregation values
        long totalBytes = 0;
        long totalTimeNs = 0;

        // Iterate through all rows to aggregate data
        for (Row row : rows) {
            Object sizeObj = row.getValue(sizeCol);
            Object timeObj = row.getValue(timeCol);

            // Parse and aggregate data for size and time
            long size = parseByteSize(sizeObj);
            long time = parseTimeDuration(timeObj);

            totalBytes += size;
            totalTimeNs += time;
        }

        // After processing all rows, calculate the aggregated values
        long finalSizeBytes = aggregationType.equals("average") ? totalBytes / rows.size() : totalBytes;
        double convertedSize = convertFromBytes(finalSizeBytes, sizeUnit);
        long finalTime = aggregationType.equals("average") ? totalTimeNs / rows.size() : totalTimeNs;
        double convertedTime = convertFromNanoseconds(finalTime, timeUnit);

        // Create a single result row for the aggregation
        Row result = new Row();
        result.add(targetSizeCol, convertedSize);
        result.add(targetTimeCol, convertedTime);

        // Return only the aggregated result as a list
        return List.of(result); 
    }

    private long parseByteSize(Object obj) {
        if (obj == null) {
            return 0;
        }

        String str = obj.toString().trim().toLowerCase();

        try {
            if (str.endsWith("kb"))
                return (long) (Double.parseDouble(str.replace("kb", "")) * 1024);
            if (str.endsWith("mb"))
                return (long) (Double.parseDouble(str.replace("mb", "")) * 1024 * 1024);
            if (str.endsWith("gb"))
                return (long) (Double.parseDouble(str.replace("gb", "")) * 1024 * 1024 * 1024);
            return Long.parseLong(str.replace("bytes", "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid byte size value: " + obj.toString());
        }

    }

    private long parseTimeDuration(Object obj) {
        if (obj == null) {
            return 0;
        }

        String str = obj.toString().trim().toLowerCase();
        try {
            if (str.endsWith("ms"))
                return (long) (Double.parseDouble(str.replace("ms", "").trim()) * 1_000_000);
            if (str.endsWith("s"))
                return (long) (Double.parseDouble(str.replace("s", "").trim()) * 1_000_000_000);
            if (str.endsWith("min"))
                return (long) (Double.parseDouble(str.replace("min", "").trim()) * 60 * 1_000_000_000);
            return Long.parseLong(str.replace("ns", "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time duration value: " + str);
        }
    }

    private double convertFromBytes(long bytes, String unit) {
        if (unit.equals("kb")) {
            return bytes / 1024.0;
        } else if (unit.equals("mb")) {
            return bytes / (1024.0 * 1024);
        } else if (unit.equals("gb")) {
            return bytes / (1024.0 * 1024 * 1024);
        } else {
            return bytes;
        }
    }

    private double convertFromNanoseconds(long ns, String unit) {
        if (unit.equals("ms")) {
            return ns / 1000000.0;
        } else if (unit.equals("s")) {
            return ns / 1000000000.0;
        } else if (unit.equals("min")) {
            return ns / (60.0 * 1000000000);
        } else {
            return ns;
        }
    }

    @Override
    public void destroy() {
        // cleanup if needed
    }
}
