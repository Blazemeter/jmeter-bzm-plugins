package com.blazemeter.jmeter.rte.core.wait;

import com.blazemeter.jmeter.rte.core.Position;
import java.awt.Dimension;
import java.util.Objects;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WaitCondition} to wait for certain text to be in terminal screen.
 */
public class TextWaitCondition extends WaitCondition {

  private static final Logger LOG = LoggerFactory.getLogger(TextWaitCondition.class);

  private final Pattern regex;
  private final PatternMatcher matcher;
  private final Area searchArea;

  public TextWaitCondition(Pattern regex, PatternMatcher matcher, Area searchArea,
      long timeoutMillis,
      long stableTimeoutMillis) {
    super(timeoutMillis, stableTimeoutMillis);
    this.regex = regex;
    this.matcher = matcher;
    this.searchArea = searchArea;
  }

  @Override
  public String getDescription() {
    return "emulator screen area " + searchArea + " to contain " + regex;
  }

  public boolean matchesScreen(String screen, Dimension screenSize) {
    String screenArea = extractScreenArea(searchArea, screen, screenSize);
    return matcher.contains(screenArea, regex);
  }

  private String extractScreenArea(Area searchArea, String screen, Dimension screenSize) {
    StringBuilder builder = new StringBuilder();
    int top = getBoundedValueOrDefault(searchArea.getTop(), 1, screenSize.height, 1, "top row");
    int left = getBoundedValueOrDefault(searchArea.getLeft(), 1, screenSize.width, 1,
        "left column");
    int bottom = getBoundedValueOrDefault(
        searchArea.getBottom() == Position.UNSPECIFIED_INDEX ? screenSize.height
            : searchArea.getBottom(), top, screenSize.height, screenSize.height, "bottom row");
    int right = getBoundedValueOrDefault(
        searchArea.getRight() == Position.UNSPECIFIED_INDEX ? screenSize.width
            : searchArea.getRight(), left, screenSize.width, screenSize.width, "right column");
    for (int i = top; i <= bottom; i++) {
      // we increase one due to new line at end of row
      int rowStart = (i - 1) * (screenSize.width + 1);
      builder.append(screen.substring(rowStart + left - 1, rowStart + right));
      builder.append("\n");
    }
    return builder.toString();
  }

  private int getBoundedValueOrDefault(int value, int lowerBound, int upperBound, int defaultValue,
      String description) {
    if (value < lowerBound || value > upperBound) {
      LOG.warn("Search area {} {} is outside of allowed bounds ({},{}). Defaulting to {}.",
          description, value, lowerBound, upperBound, defaultValue);
      return defaultValue;
    }
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    TextWaitCondition that = (TextWaitCondition) o;
    return Objects.equals(regex, that.regex) &&
        Objects.equals(matcher, that.matcher) &&
        Objects.equals(searchArea, that.searchArea);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), regex, matcher, searchArea);
  }

  @Override
  public String toString() {
    return "TextWaitCondition{" +
        "regex=" + regex +
        ", matcher=" + matcher +
        ", searchArea=" + searchArea +
        ", timeoutMillis=" + timeoutMillis +
        ", stableTimeoutMillis=" + stableTimeoutMillis +
        '}';
  }
}
