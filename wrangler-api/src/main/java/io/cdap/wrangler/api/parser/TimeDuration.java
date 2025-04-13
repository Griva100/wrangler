/*
 * Copyright © 2025 Cask Data, Inc.
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

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token implementation for time durations like "500ms", "2h"
 */
public class TimeDuration implements Token {
  private static final Pattern PATTERN = Pattern.compile("(?i)^([0-9]*\\.?[0-9]+)(ms|s|min|h)$");
  private final long millis;

  public TimeDuration(String input) {
    Matcher matcher = PATTERN.matcher(input.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time duration: " + input);
    }

    double value = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(2).toLowerCase();

    switch (unit) {
      case "ms":
        millis = (long) value;
        break;
      case "s":
        millis = (long) (value * 1000);
        break;
      case "min":
        millis = (long) (value * 60 * 1000);
        break;
      case "h":
        millis = (long) (value * 60 * 60 * 1000);
        break;
      default:
        throw new IllegalArgumentException("Unsupported time unit: " + unit);
    }
  }

  public long getMillis() {
    return millis;
  }

  @Override
  public Object value() {
    return getMillis();
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(getMillis());
  }
}
