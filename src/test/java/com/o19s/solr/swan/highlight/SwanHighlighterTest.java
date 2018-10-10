package com.o19s.solr.swan.highlight;

/**
 * Copyright 2012 OpenSource Connections, LLC.
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

import java.util.HashMap;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.SolrException;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.util.TestHarness;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests some basic functionality of Solr while demonstrating good Best
 * Practices for using AbstractSolrTestCase
 */
public class SwanHighlighterTest extends SolrTestCaseJ4 {

  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig.xml", "schema.xml");
  }

  @After
  @Override
  public void tearDown() throws Exception {
    // if you override setUp or tearDown, you better call
    // the super class's version
    clearIndex();
    super.tearDown();
  }

  @Test
  public void testSimple() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("long"),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]");
  }

  @Test
  public void testBrsTermHighlight() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("(long).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]");
  }

  @Test
  public void testAlternateFieldHighlight() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("long.a."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]");
  }

  @Test
  public void testMutliDocHighlight() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(adoc(
        "id", "2",
        "x", "a day's long night"));
    assertU(commit());
    assertU(adoc(
        "id", "7",
        "x", "nothing"));
    assertU(commit());
    assertU(adoc(
        "id", "3",
        "x", "a night's day is long"));
    assertU(adoc(
        "id", "4",
        "x", "long day"));
    assertU(adoc(
        "id", "5",
        "x", "one two three four five long"));
    assertU(commit());
    assertU(adoc(
        "id", "8",
        "x", "nothing"));
    assertU(adoc(
        "id", "6",
        "x", "one two three four five six long"));
    assertU(commit());
    assertQ("Basic summarization",
        lquery("(long).x."),
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long<')]",
        "//lst[@name='4']/arr[@name='x']/str[contains(.,'>long<')]");
  }

  /**
   *  This query has been giving me fits
   */
  @Test
  public void testAnaerobicTermHighlight() {
    assertU(adoc(
        "x", "a long day's anaerobic",
        "id", "1"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("(anaerobic SAME long).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>anaerobic</')]");
  }

  @Test
  public void testSameHighlight() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("(long SAME night).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>night</')]");
  }

  @Test
  public void testWithHighlight() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("(long WITH night).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>night</')]");
  }

  @Test
  public void testAdjHighlight() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("long ADJ3 night"),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>night</')]");
  }

  @Test
  public void testAdjHighlight2() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("(long ADJ3 night).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>night</')]");
  }

  @Test
  public void testNearHighlight() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("(long NEAR3 night).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>night</')]");
  }

  @Test
  public void testAndHighlight() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(adoc(
        "id", "2",
        "x", "a long day's night"));
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("(long AND night).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>night</')]",
        "//lst[@name='highlighting']/lst[@name='2']",
        "//lst[@name='2']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='2']/arr[@name='x']/str[contains(.,'>night</')]");
  }

  @Test
  public void testOrHighlight() {
    assertU(adoc(
        "id", "1",
        "x", "a long day's night"));
    assertU(commit());
    assertU(optimize());
    assertQ("Basic summarization",
        lquery("(long OR night).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>night</')]");
  }

  @Test
  public void testSwanAndStrip() {
    assertU(adoc(
        "id", "1",
        "x", "3 long days and nights with my friend bill or tom (which is the same as nobody) an extra adj near extra"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("AND is stripped correctly",
        lquery("(long AND days).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'>and</'))]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>days</')]");
  }
  @Test
  public void testSwanOrStrip() {
    assertU(adoc(
        "id", "1",
        "x", "3 long days and nights with my friend bill or tom (which is the same as nobody) an extra adj near extra"));
    assertU(commit());
    assertU(optimize());
    assertQ("OR is stripped correctly",
        lquery("(long OR days).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'>or</'))]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>days</')]");
  }
  @Test
  public void testSwanSameStrip() {
    assertU(adoc(
        "id", "1",
        "x", "3 long days and nights with my friend bill or tom (which is the same as nobody) an extra adj near extra"));
    assertU(commit());
    assertU(optimize());

    assertQ("SAME is stripped correctly",
        lquery("(long SAME days).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'>same</'))]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>days</')]");
  }
  @Test
  public void testSwanWithStrip() {
    assertU(adoc(
        "id", "1",
        "x", "3 long days and nights with my friend bill or tom (which is the same as nobody) an extra adj near extra"));
    assertU(commit());
    assertU(optimize());

    assertQ("WITH is stripped correctly",
        lquery("(long WITH days).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'>with</'))]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>days</')]");
  }
  @Test
  public void testSwanAdjStrip() {
    assertU(adoc(
        "id", "1",
        "x", "3 long days and nights with my friend bill or tom (which is the same as nobody) an extra adj near extra"));
    assertU(commit());
    assertU(optimize());

    assertQ("ADJ is stripped correctly",
        lquery("(long ADJ days).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'>adj</'))]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>days</')]");
  }
  @Test
  public void testSwanNearStrip() {
    assertU(adoc(
        "id", "1",
        "x", "3 long days and nights with my friend bill or tom (which is the same as nobody) an extra adj near extra"));
    assertU(commit());
    assertU(optimize());

    assertQ("NEAR is stripped correctly",
        lquery("(long NEAR days).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>long</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'>near</'))]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>days</')]");
  }

  @Test
  public void testWildcardBrsFielded() {
    assertU(adoc(
      "id", "1",
      "x", "the data via the first or second secure communication",
      "y", "some whole other text"//extra field added for fun
    ));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for 'secure'",
      lquery("(secur* SAME data).x."),
      "//lst[@name='highlighting']/lst[@name='1']",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'>secure</')]",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'>data</')]");
  }

  @Test
  public void testWildcardBrsFielded2() {
    assertU(adoc(
      "id", "1",
      "x", "the data via the first or second secure communication",
      "y", "some whole other text"//extra field added for fun
    ));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for 'secure'",
      lquery("secure* ADJ communication"),
      "//lst[@name='highlighting']/lst[@name='1']",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'>secure</')]",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'>communication</')]");
  }

  @Test
  public void testWildcard() {
    assertU(adoc(
        "id", "1",
        "x", "the data via the first or second secure communication"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for 'secure'",
        lquery("secur*"),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>secure</')]");
  }

  @Test
  public void testSpanHighlight() {
    assertU(adoc(
        "id", "1",
        "x", "the data via the data first or second secure communication"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data'",
        lquery("(data ADJ first).x"),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>data</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>first</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'> via'))]");
  }

  @Test
  public void testMultiFieldQuery() {
    assertU(adoc(
        "id", "1",
        "x", "the data via the data first or second secure communication",
        "y", "some whole other text"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data' as well as the 'other'",
        lquery("(data ADJ first).x. AND other.y."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>data</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>first</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'> via'))]",
        "//lst[@name='1']/arr[@name='y']/str[contains(.,'>other</')]");
  }

  @Test
  public void testStripHtmlFromHighlighterResponse() {
    assertU(adoc(
        "id", "1",
        "x", "<p>a long day's night</p><p>howdy there buddy</p>"));
    assertU(commit());
    assertU(optimize());
    assertQ("Remove Html",
        lquery("long.x."),
        "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[not(contains(.,'<p>'))]");
  }

  @Test
  public void testAlternateFieldSnippetWithoutHighlight() {
    //Currently when we query specifically for a match in a particular field "(red ADJ green).x." then
    //we don't want to return distracting highlights in other fields
    assertU(adoc(
      "id", "1",
      "x", "this field has the word data and we're looking for it",
      "y", "this field has the word data, but it should be displayed without highlights"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data' as well as the 'other'",
        lquery("data.x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>data</')]");
  }

  @Test
  public void testHighlightAdjFieldParagraph() {
    assertU(adoc(
      "id", "1",
      "x", "<p>red car pink car.</p><p>red blue car red.</p>"));
    assertU(commit());
    assertU(optimize());
    assertQ("Highlight once",
      lquery("(car ADJ red).x."),
      "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[contains(.,'>car<')]",
      "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[contains(.,'>red<')]",
      "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[contains(.,'blue <')]",
      "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[contains(.,'>.')]",
      "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[not(contains(.,'red <'))]",
      "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[not(contains(.,'pink <'))]");
  }

  @Test
  public void testHighlightSameFieldParagraph() {
    assertU(adoc(
      "id", "1",
      "x", "<p>This is. a test of.</p><p>the new prox. query system.</p><p>we need to. see if prox.</p><p>n works. as we expect</p>"));
    assertU(commit());
    assertU(optimize());
    assertQ("Highlight once",
      lquery("(test SAME3 need).x."),
      "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[contains(.,'>test<')]",
      "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[contains(.,'>need<')]");
  }

  @Test
  public void testHighlightOnlySearchedFieldParagraph() {
    assertU(adoc(
        "id", "1",
        "x", "<p>red car blue car.</p><p>red blue car red.</p>",
        "y", "<p>red car blue car.</p><p>red blue car red.</p>"));
    assertU(commit());
    assertU(optimize());
    assertQ("Highlight once",
        lquery("car.x. AND blue.y."),
        "//lst[@name='highlighting']/lst[@name='1']/arr[@name='y']/str[not(contains(.,'>car<'))]",
        "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[contains(.,'>car<')]",
        "//lst[@name='highlighting']/lst[@name='1']/arr[@name='x']/str[not(contains(.,'>blue<'))]",
        "//lst[@name='highlighting']/lst[@name='1']/arr[@name='y']/str[contains(.,'>blue<')]");
  }

  @Test
  public void testComplexQuery() {
    assertU(adoc(
        "id", "1",
        "x", "the data via the data first or second secure communication"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data'",
        lquery("(data ADJ first).x. AND second.x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>data</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>first</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'> via'))]");
  }

  @Test
  public void testComplexQuery2() {
    assertU(adoc(
        "id", "1",
        "x", "the data via the data first or second secure communication"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data'",
        lquery("data ADJ first AND second"),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>data</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>first</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'> via'))]");
  }

  @Test
  public void testComplexQuery3() {
    assertU(adoc(
        "id", "1",
        "x", "the data via the data first or second secure communication",
        "y", "the data via the data first or second or third secure communication"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data'",
        lquery("(data ADJ first).x. AND (third.x. OR third.y.)) AND ((second.x. OR second.y.)"),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>data</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>first</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>second</')]",
        "//lst[@name='1']/arr[@name='y']/str[contains(.,'>second</')]",
        "//lst[@name='1']/arr[@name='y']/str[contains(.,'>third</')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'> via'))]",
        "//lst[@name='1']/arr[@name='y']/str[not(contains(.,'> via'))]",
        "//lst[@name='1']/arr[@name='y']/str[not(contains(.,'>data<'))]",
        "//lst[@name='1']/arr[@name='y']/str[not(contains(.,'>first<'))]");
  }

  @Test
  public void testCrossQuery() {
    assertU(adoc(
        "id", "1",
        "x", "the data via the data first or second secure communication",
        "y", "the data via the data first or second or third secure communication"));
    assertU(commit());
    assertU(optimize());
    //xpathI
    assertQ("Should have 1 hit",
        lquery("(data AND third) OR secure"),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>data</')]",
        "//lst[@name='1']/arr[@name='y']/str[contains(.,'>third</')]");
  }

  @Test
  public void testNestedSpans() {
    assertU(adoc("id", "1",
      "x","x x x x x x x x x x c x x x x x z a z x x x x a b x x x"));
    assertU(adoc("id", "2",
      "x"," x x x x x x x x x x a b x x x x x z a z x x x c x x x x . x x x x x z c z x x x"));
    assertU(commit());
    assertU(optimize());
    // xpath
    assertQ("looking for the second 'data'",
      lquery("((a ADJ b) WITH c).x."),
      "//lst[@name='highlighting']/lst[@name='1']",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'>a<')]",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'>b<')]",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'>c<')]",
      "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'> z'))]",
      "//lst[@name='highlighting']/lst[@name='2']",
      "//lst[@name='2']/arr[@name='x']/str[contains(.,'>a<')]",
      "//lst[@name='2']/arr[@name='x']/str[contains(.,'>b<')]",
      "//lst[@name='2']/arr[@name='x']/str[contains(.,'>c<')]",
      "//lst[@name='2']/arr[@name='x']/str[not(contains(.,'> z'))]");
  }


  @Test
  public void testXOrQuery() {
    assertU(adoc("id", "1",
      "x","x x x x x x x x x x c x x x x x z a z x x x x a b x x x"));
    assertU(adoc("id", "2",
      "x"," x x x x x x x x x x a b x x x x x z a z x x x c x x x x . x x x x x z c z x x x"));
    assertU(commit());
    assertU(optimize());
    // xpath
    assertQ("",
      lquery("a XOR b"),
      "//result[@numFound='0']");
    assertQ("",
      lquery("a XOR d"),
      "//result[@numFound='2']");
  }

  @Test
  public void testParagraphFail() {
    assertU(adoc("id", "1",
        "x","<p>A method for the prevention of turnover of rear wheel steered vehicles" +
        " includes providing a vehicle with a plurality of sensors, and providing slip" +
        " angle restriction software adapted to receive as inputs the outputs of the" +
        " plurality of sensors. The software calculates an estimated sideslip angle" +
        " based on sensor outputs, calculates a slip angle on the rear wheels based" +
        " on the estimated sideslip angle, calculates a maximum allowable slip angle," +
        " compares the slip angle and the maximum allowable slip angle, and determines" +
        " a steering angle correction value if the actual slip angle is greater than" +
        " the maximum allowable slip angle. Any steering angle correction value calculated" +
        " is applied to the rear wheels of the vehicle.</p>"));
    assertU(commit());
    assertU(optimize());
    // xpath
    assertQ("looking for the second 'data'",
        lquery("(and ADJ providing).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'sensors, <')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>and<')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>providing<')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'> slip')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'includes <'))]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'angle <'))]");
  }

  @Test
  public void testParagraphFail2() {
    assertU(adoc("id", "1",
        "x","<ol class=\"claims\"> <li value=\"1\"> An ink jet recording apparatus comprising: <blockquote " +
        "class=\"claim-text\">a plurality of ink tanks each for storing ink of each color;</blockquote>" +
        "<blockquote class=\"claim-text\">a recording head provided with nozzles for discharging onto a " +
        "recording medium the ink stored in said plurality of ink tanks;</blockquote><blockquote class=\"" +
        "claim-text\">a nozzle cap for sealing the surface of said recording head in which said nozzles are" +
        " formed; and</blockquote><blockquote class=\"claim-text\">a pressure unit for applying pressure to" +
        " discharge the ink from said recording head into said nozzle cap, wherein said plurality of ink " +
        "tanks are classified into: <blockquote class=\"claim-text\">an ink tank group consisting of ink " +
        "tanks of three colors of yellow, cyan, and magenta;</blockquote> <blockquote class=\"claim-text\">" +
        "an ink tank group consisting of ink tanks of three colors of red, green, and blue; and</blockquote>" +
        " <blockquote class=\"claim-text\">an ink tank group consisting of an ink tank of black, and wherein" +
        " a plurality of said nozzle caps are provided in correspondence to the groups of said classified " +
        "ink tanks.</blockquote> </blockquote> </li> <li value=\"2\"> The ink jet recording apparatus " +
        "according to claim 1 </li> <li value=\"3\"> The ink jet recording apparatus according to claim 2" +
        "<blockquote class=\"claim-text\">a cylindrical member provided with suction ports corresponding to" +
        " said plurality of nozzle caps; and</blockquote><blockquote class=\"claim-text\">a rotor mounted " +
        "inside the cylindrical member and provided with a suction hole, and wherein when said rotor rotates" +
        " so that any one of said suction ports communicates with said suction hole, any one of said nozzle" +
        " caps communicates with said pressure unit.</blockquote> </li> <li value=\"4\"> The ink jet " +
        "recording apparatus according to claim 1 </li> <li value=\"5\"> The ink jet recording apparatus " +
        "according to claim 4<blockquote class=\"claim-text\">a cylindrical member provided with suction " +
        "ports corresponding to said plurality of nozzle caps; and</blockquote><blockquote class=\"claim-text" +
        "\">a rotor mounted inside the cylindrical member and provided with a suction hole, and wherein when" +
        " said rotor rotates so that any one of said suction ports communicates with said suction hole, any" +
        " one of said nozzle caps communicates with said pressure unit.</blockquote> </li> <li value=\"6\"> " +
        "An ink jet recording apparatus comprising: <blockquote class=\"claim-text\">a plurality of ink " +
        "tanks each for storing ink of each color;</blockquote><blockquote class=\"claim-text\">a recording " +
        "head provided with nozzles for discharging onto a recording medium the ink stored in said plurality " +
        "of ink tanks;</blockquote><blockquote class=\"claim-text\">a nozzle cap for sealing the surface of " +
        "said recording head in which said nozzles are formed; and</blockquote><blockquote class=\"claim-" +
        "text\">a pressure unit for applying pressure to discharge the ink from said recording head into " +
        "said nozzle cap, wherein said plurality of ink tanks are classified into: <blockquote class=\"claim" +
        "-text\">an ink tank group consisting of ink tanks of three colors of yellow, cyan, and magenta;" +
        "</blockquote> <blockquote class=\"claim-text\">an ink tank group consisting of ink tanks of three " +
        "colors of red, green, and blue; and</blockquote> <blockquote class=\"claim-text\">an ink tank group" +
        " consisting of ink tanks of two colors of photo black and black, and wherein a plurality of said " +
        "nozzle caps are provided in correspondence to the groups of said classified ink tanks.</blockquote>" +
        " </blockquote> </li> <li value=\"7\"> The ink jet recording apparatus according to claim 6 </li> " +
        "<li value=\"8\"> The ink jet recording apparatus according to claim 7<blockquote class=\"claim-" +
        "text\">a cylindrical member provided with suction ports corresponding to said plurality of nozzle " +
        "caps; and</blockquote><blockquote class=\"claim-text\">a rotor mounted inside the cylindrical member" +
        " and provided with a suction hole, and wherein when said rotor rotates so that any one of said " +
        "suction ports communicates with said suction hole, any one of said nozzle caps communicates with " +
        "said pressure unit.</blockquote> </li> <li value=\"9\"> The ink jet recording apparatus according " +
        "to claim 6 </li> <li value=\"10\"> The ink jet recording apparatus according to claim 9<blockquote " +
        "class=\"claim-text\">a cylindrical member provided with suction ports corresponding to said " +
        "plurality of nozzle caps; and</blockquote><blockquote class=\"claim-text\">a rotor mounted inside " +
        "the cylindrical member and provided with a suction hole, and wherein when said rotor rotates so " +
        "that any one of said suction ports communicates with said suction hole, any one of said nozzle caps" +
        " communicates with said pressure unit.</blockquote> </li> <li value=\"11\"> An ink jet recording " +
        "apparatus comprising: <blockquote class=\"claim-text\">a plurality of ink tanks each for storing " +
        "ink of each color;</blockquote><blockquote class=\"claim-text\">a recording head provided with " +
        "nozzles for discharging onto a recording medium the ink stored in said plurality of ink tanks;" +
        "</blockquote><blockquote class=\"claim-text\">a nozzle cap for sealing the surface of said recording" +
        " head in which said nozzles are formed; and</blockquote><blockquote class=\"claim-text\">a pressure" +
        " unit for applying pressure to discharge the ink from said recording head into said nozzle cap, " +
        "wherein said plurality of ink tanks are classified into: <blockquote class=\"claim-text\">an ink" +
        " tank group consisting of ink tanks of four colors of yellow, cyan, magenta, and black; and" +
        "</blockquote> <blockquote class=\"claim-text\">an ink tank group consisting of ink tanks of four " +
        "colors of red, green, blue, and photo black, and wherein a plurality of said nozzle caps are " +
        "provided in correspondence to the groups of said classified ink tanks.</blockquote> </blockquote>" +
        " </li> <li value=\"12\"> The ink jet recording apparatus according to claim 11 </li> <li value" +
        "=\"13\"> The ink jet recording apparatus according to claim 12<blockquote class=\"claim-text\">a " +
        "cylindrical member provided with suction ports corresponding to said plurality of nozzle caps; and" +
        "</blockquote><blockquote class=\"claim-text\">a rotor mounted inside the cylindrical member and " +
        "provided with a suction hole, and wherein when said rotor rotates so that any one of said suction " +
        "ports communicates with said suction hole, any one of said nozzle caps communicates with said " +
        "pressure unit.</blockquote> </li> <li value=\"14\"> The ink jet recording apparatus according to " +
        "claim 11 </li> <li value=\"15\"> The ink jet recording apparatus according to claim 14<blockquote " +
        "class=\"claim-text\">a cylindrical member provided with suction ports corresponding to said " +
        "plurality of nozzle caps; and</blockquote><blockquote class=\"claim-text\">a rotor mounted inside " +
        "the cylindrical member and provided with a suction hole, and wherein when said rotor rotates so " +
        "that any one of said suction ports communicates with said suction hole, any one of said nozzle caps" +
        " communicates with said pressure unit. </blockquote> </li> </ol>"));
    assertU(commit());
    assertU(optimize());
    // xpath
    assertQ("Real exmaple of a failed query'",
        lquery("(red NEAR green) ADJ blue"),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>red<')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>green<')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>blue<')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>, <')]");
  }

  @Test
  public void testVeryComplexQuery() {
    assertU(adoc(
        "id", "1",
        "x", "a b c d e f g h i j k l m",
        "y", "n o p q r s t u v w x y z"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data'",
        lquery("(a ADJ b).x. AND (n.y. OR h.x. OR (d ADJ e).x.)"),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>a</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>b</')]",
        "//lst[@name='1']/arr[@name='y']/str[contains(.,'>n</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>h</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>d</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>e</')]");
  }

  @Test
  public void testSpanAndProblem() {
    assertU(adoc(
        "id", "1",
        "x", "a b aaa c d e f g h i bbb j k l m bbb ccc n o p q r s t"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data'",
        lquery("(aaa AND (bbb ADJ ccc)).x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>aaa</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>bbb</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>ccc</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'m <')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'> n')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'> j'))]");
  }

  @Ignore
  public void thingsToTest() {
    //if trying to match things in two fields, you do
    //if trying to match things in one field, you don't match in another
    //multiple span fields match
  }

  @Test
  public void testPhrase() {
    assertU(adoc(
        "id", "1",
        "x", "a b aaa c d e f g h i bbb j k l m bbb ccc n o p q r s t"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data'",
        lquery("\"bbb ccc\""),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>bbb</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>ccc</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'m <')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'> n')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'> j'))]");
  }

  @Test
  public void testPhrase2() {
    assertU(adoc(
        "id", "1",
        "x", "a b aaa c d e f g h i bbb j k l m bbb ccc n o p q r s t"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data'",
        lquery("\"bbb ccc\".x."),
        "//lst[@name='highlighting']/lst[@name='1']",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>bbb</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'>ccc</')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'m <')]",
        "//lst[@name='1']/arr[@name='x']/str[contains(.,'> n')]",
        "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'> j'))]");
  }

  @Test
  public void testPhrase3() {
    assertU(adoc(
      "id", "1",
      "x", "a b aaa c d e f g h i bbb j k l m bbb ccc n o p q r s t"));
    assertU(adoc(
      "id", "2",
      "x", "a b aaa c d e f g h i bbb j k l m bbb ccc n o p q r s t"));
    assertU(adoc(
      "id", "3",
      "x", "a b aaa c d e f g h i bbb j k l m bbb ccc n o p q r s t"));
    assertU(adoc(
      "id", "4",
      "x", "a b aaa c d e f g h i bbb j k l m bbb ccc n o p q r s t"));
    assertU(commit());
    assertU(optimize());
    //xpath
    assertQ("looking for the second 'data'",
      lquery("\"bbb ccc\""),
      "//lst[@name='highlighting']/lst[@name='1']",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'>bbb</')]",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'>ccc</')]",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'m <')]",
      "//lst[@name='1']/arr[@name='x']/str[contains(.,'> n')]",
      "//lst[@name='1']/arr[@name='x']/str[not(contains(.,'> j'))]");
  }


  @Test
  /**
   * Term specific frag list builder parameters seem to be ignored.  Trying
   * to prove that in a test and correct for it.
   */
  public void testFieldSpecificFragListBuilder() {
      HashMap<String,String> args;
      String fieldContents = "This is my rifle. There are many others like it, but this one is mine. My rifle is my best friend. It is my life. I must master it as I must master my life. Without me, my rifle is useless. Without my rifle, I am useless. I must fire my rifle true. I must shoot straighter than my enemy, who is trying to kill me. I must shoot him before he shoots me. I will. Before God I swear this creed: my rifle and myself are defenders of my country, we are the masters of our enemy, we are the saviors of my life. So be it, until there is no enemy, but peace. Amen.";

      args = basicArgs();
      args.put("f.x.hl.fragListBuilder","single");

      assertU(adoc(
              "id", "1",
              "x", fieldContents,
              "y", fieldContents));
      assertU(commit());
      assertU(optimize());
      //xpath
      assertQ("the x field should be highlighted once, and it should be a long field.",
              lquery("rifle", args),
              "//lst[@name='highlighting']/lst[@name='1']",
              "//lst[@name='1']/arr[@name='x']/str[starts-with(.,'This is my')]",
              "//lst[@name='1']/arr[@name='x']/str[contains(.,'Amen.')]",
              "//lst[@name='1']/arr[@name='y']/str[not(starts-with(.,'This is my'))]");

  }

  private LocalSolrQueryRequest lquery(String q) {
    return lquery(q, basicArgs());
  }

  private LocalSolrQueryRequest lquery(String q, HashMap<String,String> args) {
        TestHarness.LocalRequestFactory sumLRF = h.getRequestFactory("swan", 0,
                2, args);
        return sumLRF.makeRequest(q);
  }

  private HashMap<String,String> basicArgs() {
    HashMap<String, String> args = new HashMap<String, String>();
    args.put("fl", "*");
    args.put("hl", "true");
    args.put("hl.fl", "x, y");
    args.put("f.x.hl.alternateField","x");
    args.put("f.y.hl.alternateField","y");
    args.put("qf", "x, y, z");
    args.put("indent", "true");
    args.put("sm", "xxxsentencexxx");
    args.put("pm", "xxxparagraphxxx");
    args.put("hl.usePhraseHighlighter", "true");
    args.put("hl.requireFieldMatch", "true");
    args.put("debugQuery", "true");
    return args;
  }

}

