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

package io.cdap.wrangler.api.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link TimeDuration}
*/

public class TimeDurationTest {

  @Test
  public void testTimeDurationParsing() {
    Assert.assertEquals(5, new TimeDuration("5ms").getMillis());
    Assert.assertEquals((long)(2.1 * 1000), new TimeDuration("2.1s").getMillis());
    Assert.assertEquals(5 * 60 * 1000, new TimeDuration("5min").getMillis());
    Assert.assertEquals(3 * 60 * 60 * 1000, new TimeDuration("3h").getMillis());
  }

  @Test
  public void testTimeDurationParsingCaseInsensitive() {
    Assert.assertEquals(2500, new TimeDuration("2.5S").getMillis());
    Assert.assertEquals(60000, new TimeDuration("1MIN").getMillis());
    Assert.assertEquals(3600000, new TimeDuration("1H").getMillis());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTimeDuration() {
    new TimeDuration("10xyz");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTimeDuration() {
    new TimeDuration("");
  }
}
