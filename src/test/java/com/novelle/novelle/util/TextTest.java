package com.novelle.novelle.util;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class TextTest {
  @Test void readingTimeIsAtLeastOneMinute(){ assertEquals(1,Text.readingTime("one two three")); assertEquals(2,Text.readingTime("w ".repeat(201))); }
  @Test void safeUrlAcceptsOnlyHttp(){ assertEquals("https://a.com/x",Text.safeUrl(" https://a.com/x ")); assertNull(Text.safeUrl("javascript:alert(1)")); assertNull(Text.safeUrl(null)); }
}
