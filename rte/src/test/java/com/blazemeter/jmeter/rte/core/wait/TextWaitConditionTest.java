package com.blazemeter.jmeter.rte.core.wait;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.Position;
import java.awt.Dimension;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Test;

public class TextWaitConditionTest {

  private static final Area DEFAULT_SEARCH_AREA = Area
      .fromTopLeftBottomRight(1, 1, Position.UNSPECIFIED_INDEX,
          Position.UNSPECIFIED_INDEX);
  private static final String SCREEN = "+------------------+\n"
      + "| Welcome to the   |\n"
      + "|  awesome server! |\n"
      + "+------------------+\n";
  private static final Dimension SCREEN_SIZE = new Dimension(20, 4);
  private static final String MATCHING_SIMPLE_REGEX = "awesome";

  @Test
  public void shouldMatchScreenWhenMatchedRegexAndDefaultSearchArea() throws Exception {
    assertThat(buildCondition(MATCHING_SIMPLE_REGEX, DEFAULT_SEARCH_AREA)
        .matchesScreen(SCREEN, SCREEN_SIZE))
        .isTrue();
  }

  private TextWaitCondition buildCondition(String regex, Area searchArea)
      throws MalformedPatternException {
    return new TextWaitCondition(new Perl5Compiler().compile(regex), new Perl5Matcher(), searchArea,
        60000, 1000);
  }

  @Test
  public void shouldMatchScreenWhenMatchedRegexAndReducedSearchArea() throws Exception {
    assertThat(buildCondition(MATCHING_SIMPLE_REGEX, Area.fromTopLeftBottomRight(3, 4, 3, 10))
        .matchesScreen(SCREEN, SCREEN_SIZE))
        .isTrue();
  }

  @Test
  public void shouldMatchScreenWhenMatchedMultilineRegex() throws Exception {
    assertThat(buildCondition("Welcome.*\\n.*awesome", Area.fromTopLeftBottomRight(2, 1, 3, 10))
        .matchesScreen(SCREEN, SCREEN_SIZE))
        .isTrue();
  }

  @Test
  public void shouldNotMatchScreenWhenNotMatchingRegex() throws Exception {
    assertThat(buildCondition("fail", DEFAULT_SEARCH_AREA)
        .matchesScreen(SCREEN, SCREEN_SIZE))
        .isFalse();
  }

  @Test
  public void shouldNotMatchScreenWhenRegexNotMatchingInArea() throws Exception {
    assertThat(buildCondition(MATCHING_SIMPLE_REGEX, Area.fromTopLeftBottomRight(2, 4, 2, 10))
        .matchesScreen(SCREEN, SCREEN_SIZE))
        .isFalse();
  }

  @Test
  public void shouldMatchScreenWhenAreaOutsizeOfScreenDueToDefaultValuesReset() throws Exception {
    assertThat(buildCondition(MATCHING_SIMPLE_REGEX,
        Area.fromTopLeftBottomRight(SCREEN_SIZE.height + 1, SCREEN_SIZE.width + 1,
            SCREEN_SIZE.height + 1, SCREEN_SIZE.width + 1))
        .matchesScreen(SCREEN, SCREEN_SIZE))
        .isTrue();
  }

  @Test
  public void shouldMatchScreenWhenAreaIntersectsScreen() throws Exception {
    assertThat(buildCondition(MATCHING_SIMPLE_REGEX,
        Area.fromTopLeftBottomRight(3, 4,
            SCREEN_SIZE.height + 1, SCREEN_SIZE.width + 1))
        .matchesScreen(SCREEN, SCREEN_SIZE))
        .isTrue();
  }

}
