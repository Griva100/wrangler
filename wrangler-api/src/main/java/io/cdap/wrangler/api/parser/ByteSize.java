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
 * Token implementation for byte sizes like "10KB", "1.5MB"
 */
public class ByteSize implements Token {
  private static final Pattern PATTERN = Pattern.compile("(?i)^([0-9]*\\.?[0-9]+)(B|KB|MB|GB|TB)$");
  private final long bytes;

  public ByteSize(String input) {
    Matcher matcher = PATTERN.matcher(input.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid byte size: " + input);
    }

    double value = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(2).toUpperCase();

    switch (unit) {
      case "B":
        bytes = (long) value;
        break;
      case "KB":
        bytes = (long) (value * 1024);
        break;
      case "MB":
        bytes = (long) (value * 1024 * 1024);
        break;
      case "GB":
        bytes = (long) (value * 1024 * 1024 * 1024);
        break;
      case "TB":
        bytes = (long) (value * 1024L * 1024 * 1024 * 1024);
        break;
      default:
        throw new IllegalArgumentException("Unsupported byte unit: " + unit);
    }
  }

  public long getBytes() {
    return bytes;
  }

  @Override
  public Object value() {
    return getBytes();
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(getBytes());
  }
}
