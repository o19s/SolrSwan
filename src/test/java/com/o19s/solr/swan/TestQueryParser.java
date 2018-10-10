package com.o19s.solr.swan;

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

import junit.framework.Assert;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.util.TestHarness;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestQueryParser extends SolrTestCaseJ4 {


  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig.xml", "schema.xml");
  }


  @Test
  public void testRangeQuery1() {
    test("@range >= 1000", "range:[1000 TO *]");
  }

  @Test
  public void testRangeWithFieldQuery() {
      test("@rg >= 1000", "range:[1000 TO *]");
  }

  @Test
  public void testRangeQuery2() {
    test("@range <= 1000", "range:[* TO 1000]");
  }

  @Test
  public void testRangeQuery3() {
    test("@range = 1000", "range:1000");
  }

  @Test
  public void testRangeQuery4() {
    test("@range == 1000", "range:1000");
  }

  @Test
  public void testRangeQuery5() {
    test("@range < 1000", "range:[* TO 1000}");
  }

  @Test
  public void testRangeQuery6() {
    test("@range > 1000", "range:{1000 TO *]");
  }

  @Test
  public void testRangeQuery7() {
    test("@range > 1000 < 2000", "range:{1000 TO 2000}");
  }

  @Test // TODO: Is this ok?
  public void testRangeQuery8() {
    test("@range > 2000 < 1000", "range:{2000 TO 1000}");
  }

  @Test
  public void testRangeQuery9() {
    test("@range >= 1000 <= 2000", "range:[1000 TO 2000]");
  }

  @Test // TODO: Is this ok?
  public void testRangeQuery10() {
    test("@range >= 2000 <= 1000", "range:[2000 TO 1000]");
  }

  @Test
  public void testRangeQuery11() {
    test("@range > 1000 <= 2000", "range:{1000 TO 2000]");
  }

  @Test // TODO: Is this ok?
  public void testRangeQuery12() {
    test("@range > 2000 <= 1000", "range:{2000 TO 1000]");
  }

  @Test
  public void testRangeQuery13() {
    test("@range >= 1000 < 2000", "range:[1000 TO 2000}");
  }

  @Test // TODO: Is this ok?
  public void testRangeQuery14() {
    test("@range >= 2000 < 1000", "range:[2000 TO 1000}");
  }

  @Test
  public void testAscendingClassificationRangeQuery() {
    test("(123/456-789).range.","range:[123/456 TO 123/789]");
  }

  @Test
  public void testIncludeAlphaInClassificationRangeQuery() {
    test("(1B3/456-789).range.","range:[1B3/456 TO 1B3/789]");
  }

  @Test
  public void testDescendingClassificationRangeQuery() {
    test("(123/789-456).range.","range:[123/789 TO 123/456]");
  }

  @Test
  public void testBasicClassificationOr() {
    test("(123/456,789).range.","range:123/456 range:123/789");
  }

  @Test
  public void testClassificationRangePlusOr() {
    test("(123/456-789,234).range.","range:[123/456 TO 123/789] range:123/234");
  }

  @Test
  public void testClassificationMultipleOrs() {
    test("(123/45,67,89).range.","range:123/45 range:123/67 range:123/89");
  }

  @Test
  public void testClassificationMultipleRanges() {
    test("(123/45-67,12-89).range.","range:[123/45 TO 123/67] range:[123/12 TO 123/89]");
  }

  @Test
  public void testClassificationMultipleRangesMultipleOrs() {
    test("(123/45-67,63,12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
  }
  
  @Test
  public void testFullClassificationOr(){
      test("(123/45;678/11).range.","range:123/45 range:678/11");
  }

  @Test
  public void testClassificationOrNoParens(){
      test("123/45;234/43.range.","range:123/45 range:234/43");
  }

  @Test
  public void testFullClassificationOrMultipleOrs(){
      test("(123/22,45;678/11,33).range.","range:123/22 range:123/45 range:678/11 range:678/33");
  }

  @Test
  public void testFullClassificationOrMultipleRanges(){
      test("(123/45-53;678/11-15).range.","range:[123/45 TO 123/53] range:[678/11 TO 678/15]");
  }

  @Test
  public void testFullClassificationOrMoreThanTwo(){
      test("(123/45;678/11,12;890/10-15).range.","range:123/45 range:678/11 range:678/12 range:[890/10 TO 890/15]");
  }

  @Test
  public void testWhitespaceInClassificationRangeQueries(){
      test("( 123/45-67,63,12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
      test("(123 /45-67,63,12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
      test("(123/ 45-67,63,12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
      test("(123/45 -67,63,12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
      test("(123/45- 67,63,12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
      test("(123/45-67 ,63,12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
      test("(123/45-67, 63,12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
      test("(123/45-67,63 ,12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
      test("(123/45-67,63 ,12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
      test("(123/45-67,63, 12-89,11).range.","range:[123/45 TO 123/67] range:123/63 range:[123/12 TO 123/89] range:123/11");
    }

  @Test
  public void testSingleTerm1() {
    test("data", "x:data y:data");
  }

  @Test
  public void testSingleTerm2() {
    test("data.x.", "x:data");
  }

  @Test
  public void testSingleTerm3() {
    test("(data).x.", "x:data");
  }

  @Test
  public void testSingleTerm4() {
    test("(data.x.)", "x:data");
  }

  @Test
  public void testSingleTerm5() {
    testException("(data.x.).y.");
  }

  @Test
  public void testWildcardTerm1() {
    test("sample*", "x:/sample.*/ y:/sample.*/");
  }

  @Test
  public void testWildcardTerm2() {
    test("sample$", "x:/sample.*/ y:/sample.*/");
  }

  @Test
  public void testWildcardTerm3() {
    test("sample$1", "x:/sample.{0,1}/ y:/sample.{0,1}/");
  }

  @Test
  public void testWildcardTerm4() {
    test("sample$5", "x:/sample.{0,5}/ y:/sample.{0,5}/");
  }

  @Test
  public void testWildcardTerm5() {
    test("?sample$5", "x:/.sample.{0,5}/ y:/.sample.{0,5}/");
  }

  @Test
  public void testWildcardTerm6() {
    test("*sample$5", "x:/.*sample.{0,5}/ y:/.*sample.{0,5}/");
  }

  @Test
  public void testWildcardTerm7() {
    test("*sample?", "x:/.*sample./ y:/.*sample./");
  }

  @Test
  public void testWildcardTerm8() {
    test("*sa$2mp$le?", "x:/.*sa.{0,2}mp.*le./ y:/.*sa.{0,2}mp.*le./");
  }

  @Test
  public void testWildcardTerm9() {
    test("*", "*:*");
  }

  @Test
  public void testTwoTerms1() { // This tests for a default of separator of ADJ
    test("big data", "spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)");
  }

  @Test
  public void testTwoTerms2() { // This tests for a default of separator of ADJ
    test("(big data).x.", "spanNear([x:big, x:data], 0, true)");
  }

  @Test
  public void testTwoTerms3() { // This tests for a default of separator of ADJ
    test("big.x. data.x.", "spanNear([x:big, x:data], 0, true)");
  }

  @Test
  public void testTwoTerms4() { // This tests for a default of separator of ADJ
    testException("big.x. data.y.");
  }

  @Test
  public void testTwoTerms5() { // This tests for a default of separator of ADJ
    test("big.x. data", "spanNear([x:big, x:data], 0, true)");
  }

  @Test
  public void testTwoTerms6() { // This tests for a default of separator of ADJ
    test("big data.y.", "spanNear([y:big, y:data], 0, true)");
  }

  @Test
  public void testTwoAdjTerms1() {
    test("big ADJ data", "spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)");
  }

  @Test
  public void testTwoAdjTerms2() {
    test("(big ADJ data).x.", "spanNear([x:big, x:data], 0, true)");
  }

  @Test
  public void testTwoAdjTerms3() {
    test("big.x. ADJ data.x.", "spanNear([x:big, x:data], 0, true)");
  }

  @Test
  public void testTwoAdjTerms4() {
    testException("big.x. ADJ data.y.");
  }

  @Test
  public void testTwoAdjTerms5() {
    test("big.x. ADJ data", "spanNear([x:big, x:data], 0, true)");
  }

  @Test
  public void testTwoAdjTerms6() {
    test("big ADJ data.y.", "spanNear([y:big, y:data], 0, true)");
  }

  @Test
  public void testTwoAdjNTerms1() {
    test("big ADJ3 data", "spanNear([x:big, x:data], 2, true) spanNear([y:big, y:data], 2, true)");
  }

  @Test
  public void testTwoAdjNTerms2() {
    test("(big ADJ42 data).x.", "spanNear([x:big, x:data], 41, true)");
    assertQ("", lquery("(big ADJ42 data).x."),
      "//lst[@name='debug']/str[@name='parsedquery' and text()='SpanNearQuery(spanNear([x:big, x:data], 41, true))']");
  }

  @Test
  public void testTwoAdjNTerms3() {
    test("big.x. ADJ7 data.x.", "spanNear([x:big, x:data], 6, true)");
  }

  @Test
  public void testTwoAdjNTerms4() {
    testException("big.x. ADJ3 data.y.");
  }

  @Test
  public void testTwoAdjNTerms5() {
    test("big.x. ADJ2 data", "spanNear([x:big, x:data], 1, true)");
  }

  @Test
  public void testTwoAdjNTerms6() {
    test("big ADJ99 data.y.", "spanNear([y:big, y:data], 98, true)");
  }

  @Test
  public void testTwoOrTerms1() {
    test("big OR data", "(x:big y:big) (x:data y:data)");
  }

  @Test
  public void testTwoOrTerms2() {
    test("(big OR data).x.", "x:big x:data");
  }

  @Test
  public void testTwoOrTerms3() {
    test("big.x. OR data.x.", "x:big x:data");
  }

  @Test
  public void testTwoOrTerms4() {
    test("big.x. OR data.y.", "x:big y:data");
  }

  @Test // TODO: Room for improvement in how this query is optimized
  public void testTwoOrTerms5() {
    test("big.x. OR data", "x:big (x:data y:data)");
  }

  @Test // TODO: Room for improvement in how this query is optimized
  public void testTwoOrTerms6() {
    test("big OR data.y.", "(x:big y:big) y:data");
  }

  @Test
  public void testTwoFieldedOrTerms1() {
    test("(big).x. OR (data).x.", "x:big x:data");
  }

  @Test
  public void testTwoFieldedOrTerms2() {
    test("(big).x. OR (data).y.", "x:big y:data");
  }

  @Test
  public void testTwoFieldedOrTerms3() {
    test("(big OR small).x. OR (data).y.", "(x:big x:small) y:data");
  }

  @Test
  public void testTwoFieldedOrTerms4() {
    test("(big OR small).x. OR (data OR big).y.", "(x:big x:small) (y:data y:big)");
  }

  @Test
  public void testTwoAndTerms1() {
    test("big AND data", "+(x:big y:big) +(x:data y:data)");
  }

  @Test
  public void testTwoAndTerms2() {
    test("(big AND data).x.", "+x:big +x:data");
  }

  @Test
  public void testTwoAndTerms3() {
    test("big.x. AND data.x.", "+x:big +x:data");
  }

  @Test
  public void testTwoAndTerms4() {
    test("big.x. AND data.y.", "+x:big +y:data");
  }

  @Test
  public void testTwoAndTerms5() {
    test("big.x. AND data", "+x:big +(x:data y:data)");
  }

  @Test
  public void testTwoAndTerms6() {
    test("big AND data.y.", "+(x:big y:big) +y:data");
  }

  @Test
  public void testTwoNearTerms1() {
    test("big NEAR data", "spanNear([x:big, x:data], 0, false) spanNear([y:big, y:data], 0, false)");
  }

  @Test
  public void testTwoNearTerms2() {
    test("(big NEAR data).x.", "spanNear([x:big, x:data], 0, false)");
  }

  @Test
  public void testTwoNearTerms3() {
    test("big.x. NEAR data.x.", "spanNear([x:big, x:data], 0, false)");
  }

  @Test
  public void testTwoNearTerms4() {
    testException("big.x. NEAR data.y.");
  }

  @Test
  public void testTwoNearTerms5() {
    test("big.x. NEAR data", "spanNear([x:big, x:data], 0, false)");
  }

  @Test
  public void testTwoNearTerms6() {
    test("big NEAR data.y.", "spanNear([y:big, y:data], 0, false)");
  }

  @Test
  public void testTwoNearNTerms1() {
    test("big NEAR1 data", "spanNear([x:big, x:data], 0, false) spanNear([y:big, y:data], 0, false)");
  }

  @Test
  public void testTwoNearNTerms2() {
    test("(big NEAR4 data).x.", "spanNear([x:big, x:data], 3, false)");
  }

  @Test
  public void testTwoNearN7Terms3() {
    test("big.x. NEAR7 data.x.", "spanNear([x:big, x:data], 6, false)");
  }

  @Test
  public void testTwoNearNTerms4() {
    testException("big.x. NEAR3 data.y.");
  }

  @Test
  public void testTwoNearNTerms5() {
    test("big.x. NEAR12 data", "spanNear([x:big, x:data], 11, false)");
  }

  @Test
  public void testTwoNearNTerms6() {
    test("big NEAR9 data.y.", "spanNear([y:big, y:data], 8, false)");
  }

  @Test
  public void testTwoSameTerms1() {
    test("big SAME data", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx)");
  }

  @Test
  public void testTwoSameTerms2() {
    test("(big SAME data).x.", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx)");
  }

  @Test
  public void testTwoSameTerms3() {
    test("big.x. SAME data.x.", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx)");
  }

  @Test
  public void testTwoSameTerms4() {
    testException("big.x. SAME data.y.");
  }

  @Test
  public void testTwoSameTerms5() {
    test("big.x. SAME data", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx)");
  }

  @Test
  public void testTwoSameTerms6() {
    test("big SAME data.y.", "spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx)");
  }

  @Test
  public void testTwoWithTerms1() {
    test("big WITH data", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)");
  }

  @Test
  public void testTwoWithTerms2() {
    test("(big WITH data).x.", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testTwoWithTerms3() {
    test("big.x. WITH data.x.", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testTwoWithTerms4() {
    testException("big.x. WITH data.y.");
  }

  @Test
  public void testTwoWithTerms5() {
    test("big.x. WITH data", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testTwoWithTerms6() {
    test("big WITH data.y.", "spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)");
  }

  @Test
  public void testTwoNotTerms1() {
    test("big NOT data", "+(x:big y:big) -(x:data y:data)");
  }

  @Test
  public void testTwoNotTerms2() {
    test("(big NOT data).x.", "+x:big -x:data");
  }

  @Test
  public void testTwoNotTerms3() {
    test("big.x. NOT data.x.", "+x:big -x:data");
  }

  @Test
  public void testTwoNotTerms4() {
    test("big.x. NOT data.y.", "+x:big -y:data");
  }

  @Test
  public void testTwoNotTerms5() {
    test("big.x. NOT data", "+x:big -(x:data y:data)");
  }

  @Test
  public void testTwoNotTerms6() {
    test("big NOT data.y.", "+(x:big y:big) -y:data");
  }

  @Test
  public void testTwoXOrTerms1() {
    test("big XOR data", "(-(x:data y:data) +(x:big y:big)) (-(x:big y:big) +(x:data y:data))");
  }

  @Test
  public void testTwoXOrTerms2() {
    test("(big XOR data).x.", "spanOr([spanNot(x:big, spanOr([x:data]), 0, 0), spanNot(x:data, spanOr([x:big]), 0, 0)])");
  }

  @Test
  public void testTwoXOrTerms3() {
    test("big.x. XOR data.x.", "(-x:data +x:big) (-x:big +x:data)");
  }

  @Test
  public void testTwoXOrTerms4() {
    test("big.x. XOR data.y.", "(-y:data +x:big) (-x:big +y:data)");
  }

  @Test
  public void testTwoXOrTerms5() {
    test("big.x. XOR data", "(-(x:data y:data) +x:big) (-x:big +(x:data y:data))");
  }

  @Test
  public void testTwoXOrTerms6() {
    test("big XOR data.y.", "(-y:data +(x:big y:big)) (-(x:big y:big) +y:data)");
  }

  @Test
  public void testTwoXOrTerms7() {
    testException("(big.x. XOR data).y.");
  }

  @Test
  public void testTwoXOrTerms8() {
    testException("(big XOR data.x.).y.");
  }

  @Test // TODO: This query can be optimized
  public void testThreeXOrXOrTerms1() {
    test("big XOR data XOR rocks", "(-(x:data y:data) -(x:rocks y:rocks) +(x:big y:big)) (-(x:big y:big) -(x:rocks y:rocks) +(x:data y:data)) (-(x:big y:big) -(x:data y:data) +(x:rocks y:rocks))");
  }

  @Test
  public void testThreeXOrXOrTerms2() {
    test("big.x. XOR data XOR rocks", "(-(x:data y:data) -(x:rocks y:rocks) +x:big) (-x:big -(x:rocks y:rocks) +(x:data y:data)) (-x:big -(x:data y:data) +(x:rocks y:rocks))");
  }

  @Test
  public void testThreeXOrXOrTerms3() {
    test("big XOR data.x. XOR rocks", "(-x:data -(x:rocks y:rocks) +(x:big y:big)) (-(x:big y:big) -(x:rocks y:rocks) +x:data) (-(x:big y:big) -x:data +(x:rocks y:rocks))");
  }

  @Test
  public void testThreeXOrXOrTerms4() {
    test("big XOR data XOR rocks.x.", "(-(x:data y:data) -x:rocks +(x:big y:big)) (-(x:big y:big) -x:rocks +(x:data y:data)) (-(x:big y:big) -(x:data y:data) +x:rocks)");
  }

  @Test
  public void testThreeXOrXOrTerms5() {
    test("big XOR data.y. XOR rocks.x.", "(-y:data -x:rocks +(x:big y:big)) (-(x:big y:big) -x:rocks +y:data) (-(x:big y:big) -y:data +x:rocks)");
  }

  @Test
  public void testThreeXOrXOrTerms6() {
    test("(big XOR data.y.) XOR rocks.x.", "(-x:rocks +((-y:data +(x:big y:big)) (-(x:big y:big) +y:data))) (-((-y:data +(x:big y:big)) (-(x:big y:big) +y:data)) +x:rocks)");
  }

  @Test
  public void testThreeXOrXOrTerms7() {
    test("(big XOR data).y. XOR rocks.x.", "(-x:rocks +spanOr([spanNot(y:big, spanOr([y:data]), 0, 0), spanNot(y:data, spanOr([y:big]), 0, 0)])) (-spanOr([spanNot(y:big, spanOr([y:data]), 0, 0), spanNot(y:data, spanOr([y:big]), 0, 0)]) +x:rocks)");
  }

  @Test
  public void testThreeXOrXOrTerms8() {
    test("big XOR (data.y. XOR rocks.x.)", "(-((-x:rocks +y:data) (-y:data +x:rocks)) +(x:big y:big)) (-(x:big y:big) +((-x:rocks +y:data) (-y:data +x:rocks)))");
  }

  @Test
  public void testThreeXOrXOrTerms9() {
    testException("big XOR (data.y. XOR rocks).x.");
  }

  @Test
  public void testThreeXOrXOrTerms10() {
    test("(big XOR data) XOR rocks.x.", "(-x:rocks +((-(x:data y:data) +(x:big y:big)) (-(x:big y:big) +(x:data y:data)))) (-((-(x:data y:data) +(x:big y:big)) (-(x:big y:big) +(x:data y:data))) +x:rocks)");
  }

  @Test
  public void testThreeXOrXOrTerms11() {
    test("(big XOR data XOR rocks).x.", "spanOr([spanNot(x:big, spanOr([x:data, x:rocks]), 0, 0), spanNot(x:data, spanOr([x:big, x:rocks]), 0, 0), spanNot(x:rocks, spanOr([x:big, x:data]), 0, 0)])");
  }

  @Test
  public void testThreeXOrXOrTerms12() {
    test("big XOR (data XOR rocks).x.", "(-spanOr([spanNot(x:data, spanOr([x:rocks]), 0, 0), spanNot(x:rocks, spanOr([x:data]), 0, 0)]) +(x:big y:big)) (-(x:big y:big) +spanOr([spanNot(x:data, spanOr([x:rocks]), 0, 0), spanNot(x:rocks, spanOr([x:data]), 0, 0)]))");
  }

  @Test
  public void testThreeXOrXOrTerms13() {
    test("big XOR (data XOR rocks.x.)", "(-((-x:rocks +(x:data y:data)) (-(x:data y:data) +x:rocks)) +(x:big y:big)) (-(x:big y:big) +((-x:rocks +(x:data y:data)) (-(x:data y:data) +x:rocks)))");
  }

  @Test
  public void testThreeXOrXOrTerms14() {
    test("(big XOR data) XOR rocks", "(-(x:rocks y:rocks) +((-(x:data y:data) +(x:big y:big)) (-(x:big y:big) +(x:data y:data)))) (-((-(x:data y:data) +(x:big y:big)) (-(x:big y:big) +(x:data y:data))) +(x:rocks y:rocks))");
  }

  @Test // TODO: This query can be optimized
  public void testThreeXOrSameTerms1() {
    test("big XOR data SAME rocks", "(-(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx)) +(x:big y:big)) (-(x:big y:big) +(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx)))");
  }

  @Test
  public void testThreeXOrSameTerms2() {
    test("big.x. XOR data SAME rocks", "(-(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx)) +x:big) (-x:big +(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx)))");
  }

  @Test
  public void testThreeXOrSameTerms3() {
    test("big XOR data.x. SAME rocks", "(-spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) +(x:big y:big)) (-(x:big y:big) +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx))");
  }

  @Test
  public void testThreeXOrSameTerms4() {
    test("big XOR data SAME rocks.x.", "(-spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) +(x:big y:big)) (-(x:big y:big) +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx))");
  }

  @Test
  public void testThreeXOrSameTerms5() {
    testException("big XOR data.y. SAME rocks.x.");
  }

  @Test
  public void testThreeXOrSameTerms6() {
    testException("(big XOR data.y.) SAME rocks.x.");
  }

  @Test
  public void testThreeXOrSameTerms7() {
    testException("(big XOR data).y. SAME rocks.x.");
  }

  @Test
  public void testThreeXOrSameTerms8() {
    testException("big XOR (data.y. SAME rocks.x.)");
  }

  @Test
  public void testThreeXOrSameTerms9() {
    testException("big XOR (data.y. SAME rocks).x.");
  }

  @Test
  public void testThreeXOrSameTerms10() {
    test("(big XOR data) SAME rocks.x.", "(-spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) +spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx)) (-spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx))");
  }

  @Test
  public void testThreeXOrSameTerms11() {
    test("(big XOR data SAME rocks).x.", "spanOr([spanNot(x:big, spanOr([spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx)]), 0, 0), spanNot(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx), spanOr([x:big]), 0, 0)])");
  }

  @Test
  public void testThreeXOrSameTerms12() {
    test("big XOR (data SAME rocks).x.", "(-spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) +(x:big y:big)) (-(x:big y:big) +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx))");
  }

  @Test
  public void testThreeXOrSameTerms13() {
    test("big XOR (data SAME rocks.x.)", "(-spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) +(x:big y:big)) (-(x:big y:big) +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx))");
  }

  @Test
  public void testThreeXOrSameTerms14() {
    test("(big XOR data) SAME rocks", "(-(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx)) +(spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:big, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx))) (-(spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:big, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx)) +(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx)))");
  }

  @Test
  public void testThreeXOrSameTerms15() {
    test("(big.x. XOR data) SAME rocks", "(-(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx)) +spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx)) (-spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) +(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx)))");
  }

  @Test
  public void testThreeXOrSameTerms16() {
    test("(big.x. XOR data.y.) SAME rocks", "(-spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx) +spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx)) (-spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx) +spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx))");
  }

  @Test
  public void testThreeXOrSameTerms17() {
    test("(big XOR data).y. SAME rocks", "(-spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx) +spanWithin(spanNear([y:big, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx)) (-spanWithin(spanNear([y:big, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx) +spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxparagraphxxx))");
  }

  @Test
  public void testThreeWithXOrTerms1() {
    test("big WITH data XOR rocks", "(-(x:rocks y:rocks) +(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx))) (-(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) +(x:rocks y:rocks))");
  }

  @Test
  public void testThreeWithXOrTerms2() {
    test("big.x. WITH data XOR rocks", "(-(x:rocks y:rocks) +spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx)) (-spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) +(x:rocks y:rocks))");
  }

  @Test
  public void testThreeWithXOrTerms3() {
    test("big WITH data.x. XOR rocks", "(-(x:rocks y:rocks) +spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx)) (-spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) +(x:rocks y:rocks))");
  }

  @Test
  public void testThreeWithXOrTerms4() {
    test("big WITH data XOR rocks.x.", "(-x:rocks +(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx))) (-(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) +x:rocks)");
  }

  @Test
  public void testThreeWithXOrTerms5() {
    test("big WITH data.y. XOR rocks.x.", "(-x:rocks +spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) (-spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx) +x:rocks)");
  }

  @Test
  public void testThreeWithXOrTerms6() {
    test("(big WITH data.y.) XOR rocks.x.", "(-x:rocks +spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) (-spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx) +x:rocks)");
  }

  @Test
  public void testThreeWithXOrTerms7() {
    test("(big WITH data).y. XOR rocks.x.", "(-x:rocks +spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) (-spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx) +x:rocks)");
  }

  @Test
  public void testThreeWithXOrTerms8() {
    test("big WITH (data.y. XOR rocks.x.)", "(-spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx) +spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) (-spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx) +spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx))");
  }

  @Test
  public void testThreeWithXOrTerms9() {
    testException("big WITH (data.y. XOR rocks).x.");
  }

  @Test
  public void testThreeWithXOrTerms10() {
    test("(big WITH data) XOR rocks.x.", "(-x:rocks +(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx))) (-(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) +x:rocks)");
  }

  @Test
  public void testThreeWithXOrTerms11() {
    test("(big WITH data XOR rocks).x.", "spanOr([spanNot(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx), spanOr([x:rocks]), 0, 0), spanNot(x:rocks, spanOr([spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx)]), 0, 0)])");
  }

  @Test
  public void testThreeWithXOrTerms12() {
    test("big WITH (data XOR rocks).x.", "(-spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx) +spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx)) (-spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) +spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx))");
  }

  @Test
  public void testThreeWithXOrTerms13() {
    test("big WITH (data XOR rocks.x.)", "(-spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx) +(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx))) (-(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) +spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx))");
  }

  @Test
  public void testThreeWithXOrTerms14() {
    test("(big WITH data) XOR rocks", "(-(x:rocks y:rocks) +(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx))) (-(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) +(x:rocks y:rocks))");
  }

  @Test // TODO: This query can be optimized
  public void testThreeTerms1() {
    test("big data rocks", "spanNear([spanNear([x:big, x:data], 0, true), x:rocks], 0, true) spanNear([spanNear([y:big, y:data], 0, true), y:rocks], 0, true)");
  }

  @Test // TODO: This query can be optimized
  public void testThreeTerms2() {
    test("big.x. data rocks", "spanNear([spanNear([x:big, x:data], 0, true), x:rocks], 0, true)");
  }

  @Test // TODO: This query can be optimized
  public void testThreeTerms3() {
    test("big data.x. rocks", "spanNear([spanNear([x:big, x:data], 0, true), x:rocks], 0, true)");
  }

  @Test // TODO: This query can be optimized
  public void testThreeTerms4() {
    test("big data rocks.x.", "spanNear([spanNear([x:big, x:data], 0, true), x:rocks], 0, true)");
  }

  @Test
  public void testThreeTerms5() {
    testException("big data.y. rocks.x.");
  }

  @Test
  public void testThreeTerms6() {
    testException("(big data.y.) rocks.x.");
  }

  @Test
  public void testThreeTerms7() {
    testException("(big data).y. rocks.x.");
  }

  @Test
  public void testThreeTerms8() {
    testException("big (data.y. rocks.x.)");
  }

  @Test
  public void testThreeTerms9() {
    testException("big (data.y. rocks).x.");
  }

  @Test // TODO: This query can be optimized
  public void testThreeTerms10() {
    test("(big data) rocks.x.", "spanNear([spanNear([x:big, x:data], 0, true), x:rocks], 0, true)");
  }

  @Test // TODO: This query can be optimized
  public void testThreeTerms11() {
    test("(big data rocks).x.", "spanNear([spanNear([x:big, x:data], 0, true), x:rocks], 0, true)");
  }

  @Test // TODO: This query can be optimized
  public void testThreeTerms12() {
    test("big (data rocks).x.", "spanNear([x:big, spanNear([x:data, x:rocks], 0, true)], 0, true)");
  }

  @Test // TODO: This query can be optimized
  public void testThreeTerms13() {
    test("big (data rocks.x.)", "spanNear([x:big, spanNear([x:data, x:rocks], 0, true)], 0, true)");
  }

  @Test
  public void testThreeTerms14() {
    test("(big data) rocks", "spanNear([spanNear([x:big, x:data], 0, true), x:rocks], 0, true) spanNear([spanNear([y:big, y:data], 0, true), y:rocks], 0, true)");
  }

  /*****************************************
   * Pending feedback from john on operator precedence
   */


  @Test // TODO: This query can be optimized
  public void testThreeAndOrTerms1() {
    test("big AND data OR rocks", "(+(x:big y:big) +(x:data y:data)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeAndOrTerms2() {
    test("big.x. AND data OR rocks", "(+x:big +(x:data y:data)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeAndOrTerms3() {
    test("big AND data.x. OR rocks", "(+(x:big y:big) +x:data) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeAndOrTerms4() {
    test("big AND data OR rocks.x.", "(+(x:big y:big) +(x:data y:data)) x:rocks");
  }

  @Test
  public void testThreeAndOrTerms5() {
    test("big AND data.y. OR rocks.x.", "(+(x:big y:big) +y:data) x:rocks");
  }

  @Test
  public void testThreeAndOrTerms6() {
    test("(big AND data.y.) OR rocks.x.", "(+(x:big y:big) +y:data) x:rocks");
  }

  @Test
  public void testThreeAndOrTerms7() {
    test("(big AND data).y. OR rocks.x.", "(+y:big +y:data) x:rocks");
  }

  @Test
  public void testThreeAndOrTerms8() {
    test("big AND (data.y. OR rocks.x.)", "+(x:big y:big) +(y:data x:rocks)");
  }

  @Test
  public void testThreeAndOrTerms9() {
    testException("big AND (data.y. OR rocks).x.");
  }

  @Test
  public void testThreeAndOrTerms10() {
    test("(big AND data) OR rocks.x.", "(+(x:big y:big) +(x:data y:data)) x:rocks");
  }

  @Test
  public void testThreeAndOrTerms11() {
    test("(big AND data OR rocks).x.", "(+x:big +x:data) x:rocks");
  }

  @Test
  public void testThreeAndOrTerms12() {
    test("big AND (data OR rocks).x.", "+(x:big y:big) +(x:data x:rocks)");
  }

  @Test
  public void testThreeAndOrTerms13() {
    test("big AND (data OR rocks.x.)", "+(x:big y:big) +((x:data y:data) x:rocks)");
  }

  @Test
  public void testThreeAndOrTerms14() {
    test("(big AND data) OR rocks", "(+(x:big y:big) +(x:data y:data)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeNearOrTerms1() {
    test("big NEAR data OR rocks", "(spanNear([x:big, x:data], 0, false) spanNear([y:big, y:data], 0, false)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeNearOrTerms2() {
    test("big.x. NEAR data OR rocks", "spanNear([x:big, x:data], 0, false) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeNearOrTerms3() {
    test("big NEAR data.x. OR rocks", "spanNear([x:big, x:data], 0, false) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeNearOrTerms4() {
    test("big NEAR data OR rocks.x.", "(spanNear([x:big, x:data], 0, false) spanNear([y:big, y:data], 0, false)) x:rocks");
  }

  @Test
  public void testThreeNearOrTerms5() {
    test("big NEAR data.y. OR rocks.x.", "spanNear([y:big, y:data], 0, false) x:rocks");
  }

  @Test
  public void testThreeNearOrTerms6() {
    test("(big NEAR data.y.) OR rocks.x.", "spanNear([y:big, y:data], 0, false) x:rocks");
  }

  @Test
  public void testThreeNearOrTerms7() {
    test("(big NEAR data).y. OR rocks.x.", "spanNear([y:big, y:data], 0, false) x:rocks");
  }

  @Test
  public void testThreeNearOrTerms8() {
    test("big NEAR (data.y. OR rocks.x.)", "spanNear([y:big, y:data], 0, false) spanNear([x:big, x:rocks], 0, false)");
  }

  @Test
  public void testThreeNearOrTerms9() {
    testException("big NEAR (data.y. OR rocks).x.");
  }

  @Test
  public void testThreeNearOrTerms10() {
    test("(big NEAR data) OR rocks.x.", "(spanNear([x:big, x:data], 0, false) spanNear([y:big, y:data], 0, false)) x:rocks");
  }

  @Test
  public void testThreeNearOrTerms11() {
    test("(big NEAR data OR rocks).x.", "spanNear([x:big, x:data], 0, false) x:rocks");
  }

  @Test
  public void testThreeNearOrTerms12() {
    test("big NEAR (data OR rocks).x.", "spanNear([x:big, x:data], 0, false) spanNear([x:big, x:rocks], 0, false)");
  }

  @Test
  public void testThreeNearOrTerms13() {
    test("big NEAR (data OR rocks.x.)", "spanNear([x:big, x:data], 0, false) spanNear([y:big, y:data], 0, false) spanNear([x:big, x:rocks], 0, false)");
  }

  @Test
  public void testThreeNearOrTerms14() {
    test("(big NEAR data) OR rocks", "(spanNear([x:big, x:data], 0, false) spanNear([y:big, y:data], 0, false)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeSameOrTerms1() {
    test("big SAME data OR rocks", "(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeSameOrTerms2() {
    test("big.x. SAME data OR rocks", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeSameOrTerms3() {
    test("big SAME data.x. OR rocks", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeSameOrTerms4() {
    test("big SAME data OR rocks.x.", "(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx)) x:rocks");
  }

  @Test
  public void testThreeSameOrTerms5() {
    test("big SAME data.y. OR rocks.x.", "spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx) x:rocks");
  }

  @Test
  public void testThreeSameOrTerms6() {
    test("(big SAME data.y.) OR rocks.x.", "spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx) x:rocks");
  }

  @Test
  public void testThreeSameOrTerms7() {
    test("(big SAME data).y. OR rocks.x.", "spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx) x:rocks");
  }

  @Test
  public void testThreeSameOrTerms8() {
    test("big SAME (data.y. OR rocks.x.)", "spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx) spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx)");
  }

  @Test
  public void testThreeSameOrTerms9() {
    testException("big SAME (data.y. OR rocks).x.");
  }

  @Test
  public void testThreeSameOrTerms10() {
    test("(big SAME data) OR rocks.x.", "(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx)) x:rocks");
  }

  @Test
  public void testThreeSameOrTerms11() {
    test("(big SAME data OR rocks).x.", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx) x:rocks");
  }

  @Test
  public void testThreeSameOrTerms12() {
    test("big SAME (data OR rocks).x.", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx)");
  }

  @Test
  public void testThreeSameOrTerms13() {
    test("big SAME (data OR rocks.x.)", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx) spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxparagraphxxx)");
  }

  @Test
  public void testThreeSameWithTerms14() {
    test("(big SAME data) OR rocks", "(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxparagraphxxx)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeWithOrTerms1() {
    test("big WITH data OR rocks", "(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeWithOrTerms2() {
    test("big.x. WITH data OR rocks", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeWithOrTerms3() {
    test("big WITH data.x. OR rocks", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeWithOrTerms4() {
    test("big WITH data OR rocks.x.", "(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) x:rocks");
  }

  @Test
  public void testThreeWithOrTerms5() {
    test("big WITH data.y. OR rocks.x.", "spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx) x:rocks");
  }

  @Test
  public void testThreeWithOrTerms6() {
    test("(big WITH data.y.) OR rocks.x.", "spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx) x:rocks");
  }

  @Test
  public void testThreeWithOrTerms7() {
    test("(big WITH data).y. OR rocks.x.", "spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx) x:rocks");
  }

  @Test
  public void testThreeWithOrTerms8() {
    test("big WITH (data.y. OR rocks.x.)", "spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx) spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testThreeWithOrTerms9() {
    testException("big WITH (data.y. OR rocks).x.");
  }

  @Test
  public void testThreeWithOrTerms10() {
    test("(big WITH data) OR rocks.x.", "(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) x:rocks");
  }

  @Test
  public void testThreeWithOrTerms11() {
    test("(big WITH data OR rocks).x.", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) x:rocks");
  }

  @Test
  public void testThreeWithOrTerms12() {
    test("big WITH (data OR rocks).x.", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testThreeWithOrTerms13() {
    test("big WITH (data OR rocks.x.)", "spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx) spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testThreeWithOrTerms14() {
    test("(big WITH data) OR rocks", "(spanWithin(spanNear([x:big, x:data], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:data], 2147483647, false), 1 ,y:xxxsentencexxx)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeAdjOrTerms1() {
    test("big ADJ data OR rocks", "(spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeAdjOrTerms2() {
    test("big.x. ADJ data OR rocks", "spanNear([x:big, x:data], 0, true) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeAdjOrTerms3() {
    test("big ADJ data.x. OR rocks", "spanNear([x:big, x:data], 0, true) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeAdjOrTerms4() {
    test("big ADJ data OR rocks.x.", "(spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)) x:rocks");
  }

  @Test
  public void testThreeAdjOrTerms5() {
    test("big ADJ data.y. OR rocks.x.", "spanNear([y:big, y:data], 0, true) x:rocks");
  }

  @Test
  public void testThreeAdjOrTerms6() {
    test("(big ADJ data.y.) OR rocks.x.", "spanNear([y:big, y:data], 0, true) x:rocks");
  }

  @Test
  public void testThreeAdjOrTerms7() {
    test("(big ADJ data).y. OR rocks.x.", "spanNear([y:big, y:data], 0, true) x:rocks");
  }

  @Test
  public void testThreeAdjOrTerms8() {
    test("big ADJ (data.y. OR rocks.x.)", "spanNear([y:big, y:data], 0, true) spanNear([x:big, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAdjOrTerms9() {
    testException("big ADJ (data.y. OR rocks).x.");
  }

  @Test
  public void testThreeAdjOrTerms10() {
    test("(big ADJ data) OR rocks.x.", "(spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)) x:rocks");
  }

  @Test
  public void testThreeAdjOrTerms11() {
    test("(big ADJ data OR rocks).x.", "spanNear([x:big, x:data], 0, true) x:rocks");
  }

  @Test
  public void testThreeAdjOrTerms12() {
    test("big ADJ (data OR rocks).x.", "spanNear([x:big, x:data], 0, true) spanNear([x:big, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAdjOrTerms13() {
    test("big ADJ (data OR rocks.x.)", "spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true) spanNear([x:big, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAdjOrTerms14() {
    test("(big ADJ data) OR rocks", "(spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeOrAdjTerms1() {
    test("big OR data ADJ rocks", "(x:big y:big) (spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true))");
  }

  @Test
  public void testThreeOrAdjTerms2() {
    test("big.x. OR data ADJ rocks", "x:big (spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true))");
  }

  @Test
  public void testThreeOrAdjTerms3() {
    test("big OR data.x. ADJ rocks", "(x:big y:big) spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeOrAdjTerms4() {
    test("big OR data ADJ rocks.x.", "(x:big y:big) spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeOrAdjTerms5() {
    testException("big OR data.y. ADJ rocks.x.");
  }

  @Test
  public void testThreeOrAdjTerms6() {
    testException("(big OR data.y.) ADJ rocks.x.");
  }

  @Test
  public void testThreeOrAdjTerms7() {
    testException("(big OR data).y. ADJ rocks.x.");
  }

  @Test
  public void testThreeOrAdjTerms8() {
    testException("big OR (data.y. ADJ rocks.x.)");
  }

  @Test
  public void testThreeOrAdjTerms9() {
    testException("big OR (data.y. ADJ rocks).x.");
  }

  @Test
  public void testThreeOrAdjTerms10() {
    test("(big OR data) ADJ rocks.x.", "spanNear([x:big, x:rocks], 0, true) spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeOrAdjTerms11() {
    test("(big OR data ADJ rocks).x.", "x:big spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeOrAdjTerms12() {
    test("big OR (data ADJ rocks).x.", "(x:big y:big) spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeOrAdjTerms13() {
    test("big OR (data ADJ rocks.x.)", "(x:big y:big) spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeOrAdjTerms14() {
    test("(big OR data) ADJ rocks", "spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true) spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)");
  }

  @Test
  public void testThreeOrOrTerms1() {
    test("big OR data OR rocks", "(x:big y:big) (x:data y:data) (x:rocks y:rocks)");
  }

  @Test
  public void testThreeAndAndTerms1() {
    test("big AND data AND rocks", "+(x:big y:big) +(x:data y:data) +(x:rocks y:rocks)");
  }

  @Test
  public void testThreeAdjAndTerms1() {
    test("big ADJ data AND rocks", "+(spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)) +(x:rocks y:rocks)");
  }

  @Test
  public void testThreeAdjAndTerms2() {
    test("big.x. ADJ data AND rocks", "+spanNear([x:big, x:data], 0, true) +(x:rocks y:rocks)");
  }

  @Test
  public void testThreeAdjAndTerms3() {
    test("big ADJ data.x. AND rocks", "+spanNear([x:big, x:data], 0, true) +(x:rocks y:rocks)");
  }

  @Test
  public void testThreeAdjAndTerms4() {
    test("big ADJ data AND rocks.x.", "+(spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)) +x:rocks");
  }

  @Test
  public void testThreeAdjAndTerms5() {
    test("big ADJ data.y. AND rocks.x.", "+spanNear([y:big, y:data], 0, true) +x:rocks");
  }

  @Test
  public void testThreeAdjAndTerms6() {
    test("(big ADJ data.y.) AND rocks.x.", "+spanNear([y:big, y:data], 0, true) +x:rocks");
  }

  @Test
  public void testThreeAdjAndTerms7() {
    test("(big ADJ data).y. AND rocks.x.", "+spanNear([y:big, y:data], 0, true) +x:rocks");
  }

  @Test
  public void testThreeAdjAndTerms8() {
    test("big ADJ (data.y. AND rocks.x.)", "+spanNear([y:big, y:data], 0, true) +spanNear([x:big, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAdjAndTerms9() {
    testException("big ADJ (data.y. AND rocks).x.");
  }

  @Test
  public void testThreeAdjAndTerms10() {
    test("(big ADJ data) AND rocks.x.", "+(spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)) +x:rocks");
  }

  @Test
  public void testThreeAdjAndTerms11() {
    test("(big ADJ data AND rocks).x.", "+spanNear([x:big, x:data], 0, true) +x:rocks");
  }

  @Test
  public void testThreeAdjAndTerms12() {
    test("big ADJ (data AND rocks).x.", "+spanNear([x:big, x:data], 0, true) +spanNear([x:big, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAdjAndTerms13() {
    test("big ADJ (data AND rocks.x.)", "+(spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)) +spanNear([x:big, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAdjAndTerms14() {
    test("(big ADJ data) AND rocks", "+(spanNear([x:big, x:data], 0, true) spanNear([y:big, y:data], 0, true)) +(x:rocks y:rocks)");
  }

  @Test
  public void testThreeAndAdjTerms1() {
    test("big AND data ADJ rocks", "+(x:big y:big) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true))");
  }

  @Test
  public void testThreeAndAdjTerms2() {
    test("big.x. AND data ADJ rocks", "+x:big +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true))");
  }

  @Test
  public void testThreeAndAdjTerms3() {
    test("big AND data.x. ADJ rocks", "+(x:big y:big) +spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAndAdjTerms4() {
    test("big AND data ADJ rocks.x.", "+(x:big y:big) +spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAndAdjTerms5() {
    testException("big AND data.y. ADJ rocks.x.");
  }

  @Test
  public void testThreeAndAdjTerms6() {
    testException("(big AND data.y.) ADJ rocks.x.");
  }

  @Test
  public void testThreeAndAdjTerms7() {
    testException("(big AND data).y. ADJ rocks.x.");
  }

  @Test
  public void testThreeAndAdjTerms8() {
    testException("big AND (data.y. ADJ rocks.x.)");
  }

  @Test
  public void testThreeAndAdjTerms9() {
    testException("big AND (data.y. ADJ rocks).x.");
  }

  @Test
  public void testThreeAndAdjTerms10() {
    test("(big AND data) ADJ rocks.x.", "+spanNear([x:big, x:rocks], 0, true) +spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAndAdjTerms11() {
    test("(big AND data ADJ rocks).x.", "+x:big +spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAndAdjTerms12() {
    test("big AND (data ADJ rocks).x.", "+(x:big y:big) +spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAndAdjTerms13() {
    test("big AND (data ADJ rocks.x.)", "+(x:big y:big) +spanNear([x:data, x:rocks], 0, true)");
  }

  @Test
  public void testThreeAndAdjTerms14() {
    test("(big AND data) ADJ rocks", "+(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true))");
  }

  @Test // TODO: This query can be optimized
  public void testThreeAndWithTerms1() {
    test("big AND data WITH rocks", "+(x:big y:big) +(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxsentencexxx))");
  }

  @Test
  public void testThreeAndWithTerms2() {
    test("big.x. AND data WITH rocks", "+x:big +(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxsentencexxx))");
  }

  @Test
  public void testThreeAndWithTerms3() {
    test("big AND data.x. WITH rocks", "+(x:big y:big) +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testThreeAndWithTerms4() {
    test("big AND data WITH rocks.x.", "+(x:big y:big) +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testThreeAndWithTerms5() {
    testException("big AND data.y. WITH rocks.x.");
  }

  @Test
  public void testThreeAndWithTerms6() {
    testException("(big AND data.y.) WITH rocks.x.");
  }

  @Test
  public void testThreeAndWithTerms7() {
    testException("(big AND data).y. WITH rocks.x.");
  }

  @Test
  public void testThreeAndWithTerms8() {
    testException("big AND (data.y. WITH rocks.x.)");
  }

  @Test
  public void testThreeAndWithTerms9() {
    testException("big AND (data.y. WITH rocks).x.");
  }

  @Test
  public void testThreeAndWithTerms10() {
    test("(big AND data) WITH rocks.x.", "+spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx) +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testThreeAndWithTerms11() {
    test("(big AND data WITH rocks).x.", "+x:big +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testThreeAndWithTerms12() {
    test("big AND (data WITH rocks).x.", "+(x:big y:big) +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testThreeAndWithTerms13() {
    test("big AND (data WITH rocks.x.)", "+(x:big y:big) +spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx)");
  }

  @Test
  public void testThreeAndWithTerms14() {
    test("(big AND data) WITH rocks", "+(spanWithin(spanNear([x:big, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:big, y:rocks], 2147483647, false), 1 ,y:xxxsentencexxx)) +(spanWithin(spanNear([x:data, x:rocks], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([y:data, y:rocks], 2147483647, false), 1 ,y:xxxsentencexxx))");
  }

  @Test // TODO: Test output of each of these queries, believe they can be optimized to be closer to testFourAndAdjOrTerms7+
  public void testFourAndAdjOrTerms1() {
    test("(big AND data) ADJ (rocks OR you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourAndAdjOrTerms2() {
    test("(big.x. AND data) ADJ (rocks OR you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourAndAdjOrTerms3() {
    test("(big AND data) ADJ (rocks.x. OR you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourAndAdjOrTerms4() {
    test("(big.x. AND data.y.) ADJ (rocks OR you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true)) +(spanNear([y:data, y:rocks], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourAndAdjOrTerms5() {
    test("(big.x. AND data) ADJ (rocks.y. OR you)", "+(spanNear([x:big, x:you], 0, true)) +(spanNear([y:data, y:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourAndAdjOrTerms6() {
    testException("(big AND data).x. ADJ (rocks OR you).y.");
  }

  @Test
  public void testFourAndAdjOrTerms7() {
    test("(big AND data).x. ADJ (rocks OR you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([x:data, x:you], 0, true))");
  }

  @Test
  public void testFourAndAdjOrTerms8() {
    test("(big AND data) ADJ (rocks OR you).y.", "+(spanNear([y:big, y:rocks], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([y:data, y:rocks], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test // TODO: This could be optimized
  public void testFourAndAdjOrTerms9() {
    test("(big AND data).x. ADJ (rocks.y. OR you)", "+(spanNear([x:big, x:you], 0, true)) +(spanNear([x:data, x:you], 0, true))");
  }

  @Test
  public void testFourAndAdjOrTerms10() {
    testException("(big.y. AND data).x. ADJ (rocks.y. OR you).x.");
  }

  @Test
  public void testFourOrAdjAndTerms1() {
    test("(big OR data) ADJ (rocks AND you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true) spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) +(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourOrAdjAndTerms2() {
    test("(big.x. OR data) ADJ (rocks AND you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) +(spanNear([x:big, x:you], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourOrAdjAndTerms3() {
    test("(big OR data) ADJ (rocks.x. AND you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([x:data, x:rocks], 0, true)) +(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourOrAdjAndTerms4() {
    test("(big.x. OR data.y.) ADJ (rocks AND you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) +(spanNear([x:big, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourOrAdjAndTerms5() {
    test("(big.x. OR data) ADJ (rocks.y. AND you)", "+(spanNear([y:data, y:rocks], 0, true)) +(spanNear([x:big, x:you], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourOrAdjAndTerms6() {
    testException("(big OR data).x. ADJ (rocks AND you).y.");
  }

  @Test
  public void testFourOrAdjAndTerms7() {
    test("(big OR data).x. ADJ (rocks AND you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([x:data, x:rocks], 0, true)) +(spanNear([x:big, x:you], 0, true) spanNear([x:data, x:you], 0, true))");
  }

  @Test
  public void testFourOrAdjAndTerms8() {
    test("(big OR data) ADJ (rocks AND you).y.", "+(spanNear([y:big, y:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) +(spanNear([y:big, y:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourOrAdjAndTerms9() {
    testException("(big OR data).x. ADJ (rocks.y. AND you)");
  }

  @Test
  public void testFourOrAdjAndTerms10() {
    testException("(big.y. OR data).x. ADJ (rocks.y. AND you).x.");
  }

  @Test
  public void testFourAndAdjAndTerms1() {
    test("(big AND data) ADJ (rocks AND you)", "+(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true)) +(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourAndAdjAndTerms2() {
    test("(big.x. AND data) ADJ (rocks AND you)", "+spanNear([x:big, x:rocks], 0, true) +spanNear([x:big, x:you], 0, true) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourAndAdjAndTerms3() {
    test("(big AND data) ADJ (rocks.x. AND you)", "+spanNear([x:big, x:rocks], 0, true) +(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) +spanNear([x:data, x:rocks], 0, true) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourAndAdjAndTerms4() {
    test("(big.x. AND data.y.) ADJ (rocks AND you)", "+spanNear([x:big, x:rocks], 0, true) +spanNear([x:big, x:you], 0, true) +spanNear([y:data, y:rocks], 0, true) +spanNear([y:data, y:you], 0, true)");
  }

  @Test // TODO: Should this fail?
  public void testFourAndAdjAndTerms5() {
    test("(big.x. AND data) ADJ (rocks.y. AND you)", "+spanNear([x:big, x:you], 0, true) +spanNear([y:data, y:rocks], 0, true) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourAndAdjAndTerms6() {
    testException("(big AND data).x. ADJ (rocks AND you).y.");
  }

  @Test
  public void testFourAndAdjAndTerms7() {
    test("(big AND data).x. ADJ (rocks AND you)", "+spanNear([x:big, x:rocks], 0, true) +spanNear([x:big, x:you], 0, true) +spanNear([x:data, x:rocks], 0, true) +spanNear([x:data, x:you], 0, true)");
  }

  @Test
  public void testFourAndAdjAndTerms8() {
    test("(big AND data) ADJ (rocks AND you).y.", "+spanNear([y:big, y:rocks], 0, true) +spanNear([y:big, y:you], 0, true) +spanNear([y:data, y:rocks], 0, true) +spanNear([y:data, y:you], 0, true)");
  }

  @Test
  public void testFourAndAdjAndTerms9() {
    test("(big AND data).x. ADJ (rocks.y. AND you)", "+spanNear([x:big, x:you], 0, true) +spanNear([x:data, x:you], 0, true)");
  }

  @Test
  public void testFourAndAdjAndTerms10() {
    testException("(big.y. AND data).x. ADJ (rocks.y. AND you).x.");
  }

  @Test
  public void testFourOrAdjOrTerms1() {
    test("(big OR data) ADJ (rocks OR you)", "spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true) spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)");
  }

  @Test
  public void testFourOrAdjOrTerms2() {
    test("(big.x. OR data) ADJ (rocks OR you)", "spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)");
  }

  @Test
  public void testFourOrAdjOrTerms3() {
    test("(big OR data) ADJ (rocks.x. OR you)", "spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true) spanNear([x:data, x:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)");
  }

  @Test
  public void testFourOrAdjOrTerms4() {
    test("(big.x. OR data.y.) ADJ (rocks OR you)", "spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([y:data, y:you], 0, true)");
  }

  @Test
  public void testFourOrAdjOrTerms5() {
    test("(big.x. OR data) ADJ (rocks.y. OR you)", "spanNear([x:big, x:you], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)");
  }

  @Test
  public void testFourOrAdjOrTerms6() {
    testException("(big OR data).x. ADJ (rocks OR you).y.");
  }

  @Test
  public void testFourOrAdjOrTerms7() {
    test("(big OR data).x. ADJ (rocks OR you)", "spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([x:data, x:rocks], 0, true) spanNear([x:data, x:you], 0, true)");
  }

  @Test
  public void testFourOrAdjOrTerms8() {
    test("(big OR data) ADJ (rocks OR you).y.", "spanNear([y:big, y:rocks], 0, true) spanNear([y:big, y:you], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([y:data, y:you], 0, true)");
  }

  @Test
  public void testFourOrAdjOrTerms9() {
    test("(big OR data).x. ADJ (rocks.y. OR you)", "spanNear([x:big, x:you], 0, true) spanNear([x:data, x:you], 0, true)");
  }

  @Test
  public void testFourOrAdjOrTerms10() {
    testException("(big.y. OR data).x. ADJ (rocks.y. OR you).x.");
  }

  @Test
  public void testFourXOrAdjXOrTerms1() {
    test("(big XOR data) ADJ (rocks XOR you)", "(-(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) -(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) -(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true))) (-(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true)) -(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) -(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true))) (-(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true)) -(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) -(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true))) (-(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true)) -(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) -(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)))");
  }

  @Test
  public void testFourXOrAdjXOrTerms2() {
    test("(big.x. XOR data) ADJ (rocks XOR you)", "(-spanNear([x:big, x:you], 0, true) -(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) -(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +spanNear([x:big, x:rocks], 0, true)) (-spanNear([x:big, x:rocks], 0, true) -(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) -(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +spanNear([x:big, x:you], 0, true)) (-spanNear([x:big, x:rocks], 0, true) -spanNear([x:big, x:you], 0, true) -(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true))) (-spanNear([x:big, x:rocks], 0, true) -spanNear([x:big, x:you], 0, true) -(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)))");
  }

  @Test
  public void testFourXOrAdjXOrTerms3() {
    test("(big XOR data) ADJ (rocks.x. XOR you)", "(-(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) -spanNear([x:data, x:rocks], 0, true) -(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +spanNear([x:big, x:rocks], 0, true)) (-spanNear([x:big, x:rocks], 0, true) -spanNear([x:data, x:rocks], 0, true) -(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true))) (-spanNear([x:big, x:rocks], 0, true) -(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) -(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +spanNear([x:data, x:rocks], 0, true)) (-spanNear([x:big, x:rocks], 0, true) -(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) -spanNear([x:data, x:rocks], 0, true) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)))");
  }

  @Test
  public void testFourXOrAdjXOrTerms4() {
    test("(big.x. XOR data.y.) ADJ (rocks XOR you)", "(-spanNear([x:big, x:you], 0, true) -spanNear([y:data, y:rocks], 0, true) -spanNear([y:data, y:you], 0, true) +spanNear([x:big, x:rocks], 0, true)) (-spanNear([x:big, x:rocks], 0, true) -spanNear([y:data, y:rocks], 0, true) -spanNear([y:data, y:you], 0, true) +spanNear([x:big, x:you], 0, true)) (-spanNear([x:big, x:rocks], 0, true) -spanNear([x:big, x:you], 0, true) -spanNear([y:data, y:you], 0, true) +spanNear([y:data, y:rocks], 0, true)) (-spanNear([x:big, x:rocks], 0, true) -spanNear([x:big, x:you], 0, true) -spanNear([y:data, y:rocks], 0, true) +spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourXOrAdjXOrTerms5() {
    testException("(big.x. XOR data) ADJ (rocks.y. XOR you)");
  }

  @Test
  public void testFourXOrAdjXOrTerms6() {
    testException("(big XOR data).x. ADJ (rocks XOR you).y.");
  }

  @Test
  public void testFourXOrAdjXOrTerms7() {
    test("(big XOR data).x. ADJ (rocks XOR you)", "(-spanNear([x:big, x:you], 0, true) -spanNear([x:data, x:rocks], 0, true) -spanNear([x:data, x:you], 0, true) +spanNear([x:big, x:rocks], 0, true)) (-spanNear([x:big, x:rocks], 0, true) -spanNear([x:data, x:rocks], 0, true) -spanNear([x:data, x:you], 0, true) +spanNear([x:big, x:you], 0, true)) (-spanNear([x:big, x:rocks], 0, true) -spanNear([x:big, x:you], 0, true) -spanNear([x:data, x:you], 0, true) +spanNear([x:data, x:rocks], 0, true)) (-spanNear([x:big, x:rocks], 0, true) -spanNear([x:big, x:you], 0, true) -spanNear([x:data, x:rocks], 0, true) +spanNear([x:data, x:you], 0, true))");
  }

  @Test
  public void testFourXOrAdjXOrTerms8() {
    test("(big XOR data) ADJ (rocks XOR you).y.", "(-spanNear([y:big, y:you], 0, true) -spanNear([y:data, y:rocks], 0, true) -spanNear([y:data, y:you], 0, true) +spanNear([y:big, y:rocks], 0, true)) (-spanNear([y:big, y:rocks], 0, true) -spanNear([y:data, y:rocks], 0, true) -spanNear([y:data, y:you], 0, true) +spanNear([y:big, y:you], 0, true)) (-spanNear([y:big, y:rocks], 0, true) -spanNear([y:big, y:you], 0, true) -spanNear([y:data, y:you], 0, true) +spanNear([y:data, y:rocks], 0, true)) (-spanNear([y:big, y:rocks], 0, true) -spanNear([y:big, y:you], 0, true) -spanNear([y:data, y:rocks], 0, true) +spanNear([y:data, y:you], 0, true))");
  }

  @Test
  public void testFourXOrAdjXOrTerms9() {
    testException("(big XOR data).x. ADJ (rocks.y. XOR you)");
  }

  @Test
  public void testFourXOrAdjXOrTerms10() {
    testException("(big.y. XOR data).x. ADJ (rocks.y. XOR you).x.");
  }

  @Test
  public void testFourXOrAdjOrTerms1() {
    test("(big XOR data) ADJ (rocks OR you)", "(-(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true))) (-(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)))");
  }

  @Test
  public void testFourXOrAdjOrTerms2() {
    test("(big.x. XOR data) ADJ (rocks OR you)", "(-(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true))) (-(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)))");
  }

  @Test
  public void testFourXOrAdjOrTerms3() {
    test("(big XOR data) ADJ (rocks.x. OR you)", "(-(spanNear([x:data, x:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)) +(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true))) (-(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true)))");
  }

  @Test
  public void testFourXOrAdjOrTerms4() {
    test("(big.x. XOR data.y.) ADJ (rocks OR you)", "(-(spanNear([y:data, y:rocks], 0, true) spanNear([y:data, y:you], 0, true)) +(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true))) (-(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true)) +(spanNear([y:data, y:rocks], 0, true) spanNear([y:data, y:you], 0, true)))");
  }

  @Test
  public void testFourXOrAdjOrTerms5() {
    testException("(big.x. XOR data) ADJ (rocks.y. OR you)");
  }

  @Test
  public void testFourXOrAdjOrTerms6() {
    testException("(big XOR data).x. ADJ (rocks OR you).y.");
  }

  @Test
  public void testFourXOrAdjOrTerms7() {
    test("(big XOR data).x. ADJ (rocks OR you)", "(-(spanNear([x:data, x:rocks], 0, true) spanNear([x:data, x:you], 0, true)) +(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true))) (-(spanNear([x:big, x:rocks], 0, true) spanNear([x:big, x:you], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([x:data, x:you], 0, true)))");
  }

  @Test
  public void testFourXOrAdjOrTerms8() {
    test("(big XOR data) ADJ (rocks OR you).y.", "(-(spanNear([y:data, y:rocks], 0, true) spanNear([y:data, y:you], 0, true)) +(spanNear([y:big, y:rocks], 0, true) spanNear([y:big, y:you], 0, true))) (-(spanNear([y:big, y:rocks], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([y:data, y:rocks], 0, true) spanNear([y:data, y:you], 0, true)))");
  }

  @Test
  public void testFourXOrAdjOrTerms9() {
    testException("(big XOR data).x. ADJ (rocks.y. OR you)");
  }

  @Test
  public void testFourXOrAdjOrTerms10() {
    testException("(big.y. XOR data).x. ADJ (rocks.y. OR you).x.");
  }

  @Test
  public void testFourAndAdjXOrTerms1() {
    test("(big AND data) ADJ (rocks XOR you)", "(-(+(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))) +(+(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)))) (-(+(spanNear([x:big, x:rocks], 0, true) spanNear([y:big, y:rocks], 0, true)) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true))) +(+(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))))");
  }

  @Test
  public void testFourAndAdjXOrTerms2() {
    test("(big.x. AND data) ADJ (rocks XOR you)", "(-(+spanNear([x:big, x:you], 0, true) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))) +(+spanNear([x:big, x:rocks], 0, true) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true)))) (-(+spanNear([x:big, x:rocks], 0, true) +(spanNear([x:data, x:rocks], 0, true) spanNear([y:data, y:rocks], 0, true))) +(+spanNear([x:big, x:you], 0, true) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))))");
  }

  @Test
  public void testFourAndAdjXOrTerms3() {
    test("(big AND data) ADJ (rocks.x. XOR you)", "(-(+(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))) +(+spanNear([x:big, x:rocks], 0, true) +spanNear([x:data, x:rocks], 0, true))) (-(+spanNear([x:big, x:rocks], 0, true) +spanNear([x:data, x:rocks], 0, true)) +(+(spanNear([x:big, x:you], 0, true) spanNear([y:big, y:you], 0, true)) +(spanNear([x:data, x:you], 0, true) spanNear([y:data, y:you], 0, true))))");
  }

  @Test
  public void testFourAndAdjXOrTerms4() {
    test("(big.x. AND data.y.) ADJ (rocks XOR you)", "(-(+spanNear([x:big, x:you], 0, true) +spanNear([y:data, y:you], 0, true)) +(+spanNear([x:big, x:rocks], 0, true) +spanNear([y:data, y:rocks], 0, true))) (-(+spanNear([x:big, x:rocks], 0, true) +spanNear([y:data, y:rocks], 0, true)) +(+spanNear([x:big, x:you], 0, true) +spanNear([y:data, y:you], 0, true)))");
  }

  @Test
  public void testFourAndAdjXOrTerms5() {
    testException("(big.x. AND data) ADJ (rocks.y. XOR you)");
  }

  @Test
  public void testFourAndAdjXOrTerms6() {
    testException("(big AND data).x. ADJ (rocks XOR you).y.");
  }

  @Test
  public void testFourAndAdjXOrTerms7() {
    test("(big AND data).x. ADJ (rocks XOR you)", "(-(+spanNear([x:big, x:you], 0, true) +spanNear([x:data, x:you], 0, true)) +(+spanNear([x:big, x:rocks], 0, true) +spanNear([x:data, x:rocks], 0, true))) (-(+spanNear([x:big, x:rocks], 0, true) +spanNear([x:data, x:rocks], 0, true)) +(+spanNear([x:big, x:you], 0, true) +spanNear([x:data, x:you], 0, true)))");
  }

  @Test
  public void testFourAndAdjXOrTerms8() {
    test("(big AND data) ADJ (rocks XOR you).y.", "(-(+spanNear([y:big, y:you], 0, true) +spanNear([y:data, y:you], 0, true)) +(+spanNear([y:big, y:rocks], 0, true) +spanNear([y:data, y:rocks], 0, true))) (-(+spanNear([y:big, y:rocks], 0, true) +spanNear([y:data, y:rocks], 0, true)) +(+spanNear([y:big, y:you], 0, true) +spanNear([y:data, y:you], 0, true)))");
  }

  @Test
  public void testFourAndAdjXOrTerms9() {
    testException("(big AND data).x. ADJ (rocks.y. XOR you)");
  }

  @Test
  public void testFourAndAdjXOrTerms10() {
    testException("(big.y. AND data).x. ADJ (rocks.y. XOR you).x.");
  }

  @Test
  public void testSimpleQueries() {
    assertQ("", lquery("data AND 1.id."),
      "//lst[@name='debug']/str[@name='parsedquery' and text()='+(x:data y:data) +id:1']");
  }

  @Test
  public void testAdhocQuery1() {
    test("((mobile adj (unit$1 or terminal)) or (cellular adj (telephone or phone))) with convave with shap$1", "spanWithin(spanNear([spanWithin(spanNear([spanOr([spanNear([x:mobile, spanOr([SpanMultiTermQueryWrapper(x:/unit.{0,1}/), x:terminal])], 0, true), spanNear([x:cellular, spanOr([x:telephone, x:phone])], 0, true)]), x:convave], 2147483647, false), 1 ,x:xxxsentencexxx), SpanMultiTermQueryWrapper(x:/shap.{0,1}/)], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([spanWithin(spanNear([spanOr([spanNear([y:mobile, spanOr([SpanMultiTermQueryWrapper(y:/unit.{0,1}/), y:terminal])], 0, true), spanNear([y:cellular, spanOr([y:telephone, y:phone])], 0, true)]), y:convave], 2147483647, false), 1 ,y:xxxsentencexxx), SpanMultiTermQueryWrapper(y:/shap.{0,1}/)], 2147483647, false), 1 ,y:xxxsentencexxx)");
  }

  @Test // TODO: This will work for highlighting but loses phrase boost, will want to fix at a later date to make more correct
  public void testAdhocQuery2() {
    test("((\"one\") OR (\"two\") OR (\"three\")).x.", "x:one x:two x:three");
  }

  @Test // TODO: This will work for highlighting but loses phrase boost, will want to fix at a later date to make more correct
  public void testAdhocQuery3() {
    test("(\"one\" OR \"two\" OR \"three\").x.","x:one x:two x:three");
  }

  @Test // TODO: This will work for highlighting but loses phrase boost, will want to fix at a later date to make more correct
  public void testAdhocQuery4() {
    test("( OPTIMIZER OR OPTIMIZATION ) SAME MODEL$ WITH ( COMBUSTION OR BOILER )","spanWithin(spanNear([x:optimizer, spanWithin(spanNear([SpanMultiTermQueryWrapper(x:/model.*/), spanOr([x:combustion, x:boiler])], 2147483647, false), 1 ,x:xxxsentencexxx)], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:optimizer, spanWithin(spanNear([SpanMultiTermQueryWrapper(y:/model.*/), spanOr([y:combustion, y:boiler])], 2147483647, false), 1 ,y:xxxsentencexxx)], 2147483647, false), 1 ,y:xxxparagraphxxx) spanWithin(spanNear([x:optimization, spanWithin(spanNear([SpanMultiTermQueryWrapper(x:/model.*/), spanOr([x:combustion, x:boiler])], 2147483647, false), 1 ,x:xxxsentencexxx)], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:optimization, spanWithin(spanNear([SpanMultiTermQueryWrapper(y:/model.*/), spanOr([y:combustion, y:boiler])], 2147483647, false), 1 ,y:xxxsentencexxx)], 2147483647, false), 1 ,y:xxxparagraphxxx)");
  }

  @Test // TODO: This will work for highlighting but loses phrase boost, will want to fix at a later date to make more correct
  public void testAdhocQuery5() {
    test("(OPTIMIZER OR OPTIMIZATION) SAME MODEL$ WITH ( COMBUSTION OR BOILER)","spanWithin(spanNear([x:optimizer, spanWithin(spanNear([SpanMultiTermQueryWrapper(x:/model.*/), spanOr([x:combustion, x:boiler])], 2147483647, false), 1 ,x:xxxsentencexxx)], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:optimizer, spanWithin(spanNear([SpanMultiTermQueryWrapper(y:/model.*/), spanOr([y:combustion, y:boiler])], 2147483647, false), 1 ,y:xxxsentencexxx)], 2147483647, false), 1 ,y:xxxparagraphxxx) spanWithin(spanNear([x:optimization, spanWithin(spanNear([SpanMultiTermQueryWrapper(x:/model.*/), spanOr([x:combustion, x:boiler])], 2147483647, false), 1 ,x:xxxsentencexxx)], 2147483647, false), 1 ,x:xxxparagraphxxx) spanWithin(spanNear([y:optimization, spanWithin(spanNear([SpanMultiTermQueryWrapper(y:/model.*/), spanOr([y:combustion, y:boiler])], 2147483647, false), 1 ,y:xxxsentencexxx)], 2147483647, false), 1 ,y:xxxparagraphxxx)");
  }

  @Test // TODO: This will work for highlighting but loses phrase boost, will want to fix at a later date to make more correct
  public void testAdhocQuery6() {
    test("(BIT OR DATA) LINE WITH (BOTTOM OR \"IN\") NEAR5 TRENCH","spanWithin(spanNear([spanNear([spanOr([x:bit, x:data]), x:line], 0, true), spanNear([spanOr([x:bottom, spanNear([x:in], 0, true)]), x:trench], 4, false)], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([spanNear([spanOr([y:bit, y:data]), y:line], 0, true), spanNear([spanOr([y:bottom, spanNear([y:in], 0, true)]), y:trench], 4, false)], 2147483647, false), 1 ,y:xxxsentencexxx)");
  }

  @Test // TODO: This will work for highlighting but loses phrase boost, will want to fix at a later date to make more correct
  public void testAdhocQuery7() {
    test("(\"NOT\" NEAR2 SUBSCRIBER)","spanNear([spanNear([x:not], 0, true), x:subscriber], 1, false) spanNear([spanNear([y:not], 0, true), y:subscriber], 1, false)");
  }

  @Test // TODO: This will work for highlighting but loses phrase boost, will want to fix at a later date to make more correct
  public void testAdhocQuery8() {
    test("DIFFERENTIAL WITH (TIRE$3 TYRE$3) WITH (CALCULAT$4 DETERMIN$5 PROCESS$4)","spanWithin(spanNear([spanWithin(spanNear([x:differential, spanNear([SpanMultiTermQueryWrapper(x:/tire.{0,3}/), SpanMultiTermQueryWrapper(x:/tyre.{0,3}/)], 0, true)], 2147483647, false), 1 ,x:xxxsentencexxx), spanNear([spanNear([SpanMultiTermQueryWrapper(x:/calculat.{0,4}/), SpanMultiTermQueryWrapper(x:/determin.{0,5}/)], 0, true), SpanMultiTermQueryWrapper(x:/process.{0,4}/)], 0, true)], 2147483647, false), 1 ,x:xxxsentencexxx) spanWithin(spanNear([spanWithin(spanNear([y:differential, spanNear([SpanMultiTermQueryWrapper(y:/tire.{0,3}/), SpanMultiTermQueryWrapper(y:/tyre.{0,3}/)], 0, true)], 2147483647, false), 1 ,y:xxxsentencexxx), spanNear([spanNear([SpanMultiTermQueryWrapper(y:/calculat.{0,4}/), SpanMultiTermQueryWrapper(y:/determin.{0,5}/)], 0, true), SpanMultiTermQueryWrapper(y:/process.{0,4}/)], 0, true)], 2147483647, false), 1 ,y:xxxsentencexxx)");
  }

  private void test(String in, String out) {
    assertQ("", lquery(in),
        "//lst[@name='debug']/str[@name='parsedquery_toString' and text()='" + out + "']");
  }

  private void testException(String in) {
    Exception exception = null;

    try {
      assertQ("", lquery(in),
          "//lst[@name='debug']/str[@name='parsedquery_toString']");
    } catch (Exception e) {
      exception = e;
    }
    if (exception != null) {
      Assert.assertTrue(true);
    } else {
      assertQ("", lquery(in),
          "//lst[@name='debug']/str[@name='parsedquery_toString' and text()='FAIL SO YOU CAN SEE QUERY']");
    }
  }

  private LocalSolrQueryRequest lquery(String q) {
    HashMap<String, String> args = new HashMap<String, String>();
    args.put("fl", "*");
    args.put("qf", "x, y");
    args.put("indent", "true");
    args.put("sm", "xxxsentencexxx");
    args.put("pm", "xxxparagraphxxx");
    args.put("debugQuery", "true");
//    args.put("q.op", "OR");
    TestHarness.LocalRequestFactory sumLRF = h.getRequestFactory("swan", 0,
        200, args);
    return sumLRF.makeRequest(q);
  }

}
