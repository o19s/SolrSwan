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
 * See the License for the specific language governing permissions anxxd
 * limitations under the License.
 */

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.util.TestHarness;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

public class TestSwanQParserPlugin extends SolrTestCaseJ4 {


  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig.xml", "schema.xml");
    createIndex();
  }

  public static void createIndex() {
    //Note, in the test schema.xml there is a field named "text" to which all other fields are copied
    int i = 1;

    //Add documents for Swan Parser
    assertU(adoc(
      "id", Integer.toString(i),
      "x", "xxxparagraphxxx xxxsentencexxx ax1 bx1 cx1 dx1 xxxsentencexxx ex1 fx1 gx1 hx1 ix1 xxxparagraphxxx xxxsentencexxx jx1 kx1 lx1 money nx1 ox1 px1 xxxsentencexxx qx1 rx1 sx1 xxxsentencexxx tx1 ux1 xxxparagraphxxx xxxsentencexxx vx1 wx1 xx1 yx1 zx1",
      "y", "xxxparagraphxxx xxxsentencexxx ay1 by1 cy1 dy1 xxxsentencexxx ey1 fy1 gy1 hy1 iy1 xxxparagraphxxx xxxsentencexxx jy1 ky1 ly1 money ny1 oy1 py1 xxxsentencexxx qy1 ry1 sy1 xxxsentencexxx ty1 uy1 xxxparagraphxxx xxxsentencexxx vy1 wy1 xy1 yy1 zy1",
      "z", "xxxparagraphxxx xxxsentencexxx az1 bz1 cz1 dz1 xxxsentencexxx ez1 fz1 gz1 hz1 iz1 xxxparagraphxxx xxxsentencexxx jz1 kz1 lz1 money nz1 oz1 pz1 xxxsentencexxx qz1 rz1 sz1 xxxsentencexxx tz1 uz1 xxxparagraphxxx xxxsentencexxx vz1 wz1 xz1 yz1 zz1",
      "z1", "xxxparagraphxxx xxxsentencexxx A61K36/16 xxxsentencexxx xxxparagraphxxx "
    ));
    i++;
    assertU(adoc(
      "id", Integer.toString(i),
      "x", "xxxparagraphxxx xxxsentencexxx ax2 bx2 cx2 dx2 xxxsentencexxx ex2 fx2 gx2 hx2 ix2 xxxparagraphxxx xxxsentencexxx jx2 kx2 lx2 money nx2 ox2 px2 xxxsentencexxx qx2 rx2 sx2 xxxsentencexxx tx2 ux2 xxxparagraphxxx xxxsentencexxx vx2 wx2 xx2 yx2 zx2",
      "y", "xxxparagraphxxx xxxsentencexxx ay2 by2 cy2 dy2 xxxsentencexxx ey2 fy2 gy2 hy2 iy2 xxxparagraphxxx xxxsentencexxx jy2 ky2 ly2 money ny2 oy2 py2 xxxsentencexxx qy2 ry2 sy2 xxxsentencexxx ty2 uy2 xxxparagraphxxx xxxsentencexxx vy2 wy2 xy2 yy2 zy2",
      "z", "xxxparagraphxxx xxxsentencexxx az2 bz2 cz2 dz2 xxxsentencexxx ez2 fz2 gz2 hz2 iz2 xxxparagraphxxx xxxsentencexxx jz2 kz2 lz2 money nz2 oz2 pz2 xxxsentencexxx qz2 rz2 sz2 xxxsentencexxx tz2 uz2 xxxparagraphxxx xxxsentencexxx vz2 wz2 xz2 yz2 zz2",
      "z1", "1 1 A A61K A61K36/16 20130101 L I B H 20130101 EP "
    ));
    i++;



    // Add range query docs
    assertU(adoc(
      "id", Integer.toString(i),
      "range", Integer.toString(12342 + i),
      "rangeInt", Integer.toString(12342 + i),
      "rangeLong", Integer.toString(12342 + i),
      "rangeFloat", Integer.toString(12342 + i),
      "rangeDouble", Integer.toString(12342 + i),
      "rangeDate", String.format("2012-0%d-15T12:30:30Z", i),
      "x", "apple"
    ));
    i++;
    assertU(adoc(
      "id", Integer.toString(i),
      "range", Integer.toString(12342 + i),
      "rangeInt", Integer.toString(12342 + i),
      "rangeLong", Integer.toString(12342 + i),
      "rangeFloat", Integer.toString(12342 + i),
      "rangeDouble", Integer.toString(12342 + i),
      "rangeDate", String.format("2012-0%d-15T12:30:30Z", i),
      "x", "banana"
    ));
    i++;
    assertU(adoc(
      "id", Integer.toString(i),
      "range", Integer.toString(12342 + i),
      "rangeInt", Integer.toString(12342 + i),
      "rangeLong", Integer.toString(12342 + i),
      "rangeFloat", Integer.toString(12342 + i),
      "rangeDouble", Integer.toString(12342 + i),
      "rangeDate", String.format("2012-0%d-15T12:30:30Z", i),
      "x", "coconut"
    ));
    i++;
    assertU(adoc(
      "id", Integer.toString(i),
      "range", Integer.toString(12342 + i),
      "rangeInt", Integer.toString(12342 + i),
      "rangeLong", Integer.toString(12342 + i),
      "rangeFloat", Integer.toString(12342 + i),
      "rangeDouble", Integer.toString(12342 + i),
      "rangeDate", String.format("2012-0%d-15T12:30:30Z", i),
      "x", "dorian"
    ));
    i++;
    assertU(adoc(
      "id", Integer.toString(i),
      "range", Integer.toString(12342 + i),
      "rangeInt", Integer.toString(12342 + i),
      "rangeLong", Integer.toString(12342 + i),
      "rangeFloat", Integer.toString(12342 + i),
      "rangeDouble", Integer.toString(12342 + i),
      "rangeDate", String.format("2012-0%d-15T12:30:30Z", i),
      "x", "epcot"
    ));
    i++;
    assertU(adoc(
      "id", Integer.toString(i),
      "range", Integer.toString(12342 + i),
      "rangeInt", Integer.toString(12342 + i),
      "rangeLong", Integer.toString(12342 + i),
      "rangeFloat", Integer.toString(12342 + i),
      "rangeDouble", Integer.toString(12342 + i),
      "rangeDate", String.format("2012-0%d-15T12:30:30Z", i),
      "x", "pickle"
    ));
    i++;
    assertU(adoc(
      "id", Integer.toString(i),
      "range", Integer.toString(12342 + i),
      "rangeInt", Integer.toString(12342 + i),
      "rangeLong", Integer.toString(12342 + i),
      "rangeFloat", Integer.toString(12342 + i),
      "rangeDouble", Integer.toString(12342 + i),
      "rangeDate", String.format("2012-0%d-15T12:30:30Z", i),
      "x", "fridge"
    ));

    assertU(commit());
  }
	
	/* *********************************
	 *         Range Queries
	 * *********************************/

  @Test
  public void testRangeQuery() {
    test("@range<12346",1);
    test("@range > 12346",5);
    test("@range <= 12346",2);
    test("@range >= 12346",6);
    test("@range >= 1234",7);
  }

  @Test
  public void testRangeIntQuery() {
    test("@rangeInt<12346",1);
    test("@rangeInt > 12346",5);
    test("@rangeInt <= 12346",2);
    test("@rangeInt >= 12346",6);
    test("@rangeInt >= 1234",7);
  }

  @Test
  public void testRangeLongQuery() {
    test("@rangeLong<12346",1);
    test("@rangeLong > 12346",5);
    test("@rangeLong <= 12346",2);
    test("@rangeLong >= 12346",6);
    test("@rangeLong >= 1234",7);
  }

  @Test
  public void testRangeFloatQuery() {
    test("@rangeFloat<12346",1);
    test("@rangeFloat > 12346",5);
    test("@rangeFloat <= 12346",2);
    test("@rangeFloat >= 12346",6);
    test("@rangeFloat >= 1234",7);
  }

  @Test
  public void testRangeDoubleQuery() {
    test("@rangeDouble<12346",1);
    test("@rangeDouble > 12346",5);
    test("@rangeDouble <= 12346",2);
    test("@rangeDouble >= 12346",6);
    test("@rangeDouble >= 1234",7);
  }

  @Test
  public void testRangeDateQuery() {
    test("@rangeDate<20121015",7);
    test("@rangeDate<201204",2);
    test("@rangeDate<201205",3);
    test("@rangeDate>201205<201206",2);
    test("@rangeDate>201205<20120601",1);
    test("@rangeDate>20120515<20120601",1);
    test("@rangeDate>201205<20120515",1);
  }

  @Test
  public void testRangeQueryWrapped() {
    test("@range<\"12346\"",1);
  }

  @Test
  public void testBoundRangeQuery() {
    test("@range>=12346<=12347",2);
  }

  @Test
  public void testExclusionQuery() {
    test("@range<>12346",6);
  }

    @Test
    public void testFailingExclusionQuery() {
        test("@rangeInt<>12346",6);
    }

    @Test
  public void testRangeEqualQuery() {
    test("@range=12345",1);
  }

  @Test
  public void testRangeQueryWithOther() {
    test("@range >= 12346 AND coconut",1);
    test("@range >= 12346 coconut",1);
    test("coconut AND @range >= 12346",1);
    test("coconut @range >= 12346",1);
    test("coconut OR apple @range < 12349",2);
  }
	
	/* *********************************
	 *         SWAN Queries
	 * *********************************/
	
	/*
	 * Atomic queries
	 */

  @Test
  public void testCrazyQuery() {
    test("xxxsentencexxx ADJ (ax1 OR ex1).x.", 1);
    test("(dx1 AND ix1).x. ADJ (xxxsentencexxx OR xxxparagraphxxx)", 1);
    test("(dx1 AND iy1).x. ADJ (xxxsentencexxx OR xxxparagraphxxx)", 0);
    test("(dx1 AND ix2).x. ADJ (xxxsentencexxx OR xxxparagraphxxx)", 0);
  }

  @Test
  public void testTermQuery() {
    test("ax1",1);
    test("money",2);
    test("X",0);
  }

  /*
   * Wildcard and Phrase
   */
  @Test
  public void testWildcardQuery1() {
    test("ax1*",1);
    test("notthere*",0);
  }

  @Test
  public void testWildcardQuery2() {
    test("m?ney",2);
  }

  @Test
  public void testWildcardQuery3() {
    test("m$3y",2);
    test("m$5y",2);
    test("m$y",2);
  }

  @Test
  public void testWildcardQuery4() {
    test("m*y",2);
  }

  @Test
  public void testPhrase() {
    test("\"fx1 gx1 hx1\"",1);
    test("\"vx1 wx1 yx1\"",0);
  }

  @Test
  public void testWildcardAndPhrase() {
    test("m?ney SAME \"nx1 ox1 px1\"",1);
    test("m?ney SAME \"vx1 wx1 xx1\"",0);
  }

	/*
	 * Basic SWAN operators
	 */

  @Test
  public void testWITH1() {
    //should find words in the same sentence
    test("ax1 WITH dx1",1);
    test("ax1 WITH ex1",0);
    test("ax1 WITH2 ex1",1);
    test("ax1 WITH2 jx1",0);
    test("ax1 WITH3 jx1",1);
  }

  @Test
  public void testWITH2() {
    //should find words in the same sentence and order shouldn'tx1 matter
    test("cx1 WITH ax1",1);
  }

  @Test
  public void testWITH3() {
    //should not find words in ax1 different sentence
    test("ax1 WITH lx1",0);
  }

  @Test
  public void testWITH4() {
    test("gx1 WITH ix1",1);
  }

  @Test
  public void testSAME1() {
    //should find words in the same sentence
    test("ax1 SAME cx1",1);
  }

  @Test
  public void testSAME2() {
    //should find words in the same paragraph
    test("bx1 SAME ex1",1);
  }

  @Test
  public void testSAME3() {
    //should find words in the same paragraph and order shouldn't matter
    test("ex1 SAME bx1",1);
  }

  @Test
  public void testSAME4() {
    //should not find words in ax1 different paragraph
    test("bx1 SAME kx1",0);
  }

  @Test
  public void testADJ1() {
    //should find adjacent words
    test("ax1 ADJ bx1",1);
  }

  @Test
  public void testADJ2() {
    //should not find non-adjacent words
    test("ax1 ADJ cx1",0);
  }

  @Test
  public void testAndWith() {
    //should not find non-adjacent words
    test("(ax1 AND bx1) WITH dy1",0);
    test("(ax1 AND bx1) WITH dx1",1);
  }

  @Test
  public void testOrAdj() {
    test("(notthere OR bx1) ADJ cx1",1);
    test("(notthere OR alsonotthere) ADJ cx1",0);
  }

  @Test
  public void testADJn1() {
    //should find adjacent words
    test("ax1 ADJ1 bx1",1);
  }

  @Test
  public void testADJn2() {
    //should find spaced words
    test("ax1 ADJ2 cx1",1);
    test("ax1 ADJ2 bx1",1);
  }

  @Test
  public void testADJn3() {
    //should find words in wrong order
    test("bx1 ADJ ax1",0);
    test("cx1 ADJ2 ax1",0);
    test("bx1 ADJ2 ax1",0);
  }

  @Test
  public void testNEAR1() {
    //should find adjacent words
    test("ax1 NEAR bx1",1);
  }

  @Test
  public void testNEAR2() {
    //should not find non-adjacent words
    test("ax1 NEAR cx1",0);
  }

  @Test
  public void testNEARn1() {
    //should find adjacent words
    test("ax1 NEAR1 bx1",1);
  }

  @Test
  public void testNEARn2() {
    //should find spaced words
    test("ax1 NEAR2 cx1",1);
    test("ax1 NEAR2 bx1",1);
  }

  @Test
  public void testNEARn3() {
    //should find words regardless of order
    test("bx1 NEAR ax1",1);
    test("cx1 NEAR2 ax1",1);
    test("bx1 NEAR2 ax1",1);
  }

	/*
	 * Boolean
	 */

  @Test
  public void testOR() {
    test("gx1 OR notPresentInIndex",1);
    test("notPresentInIndex OR gx1",1);
    test("bx1 OR cx1",1);
    test("bx1 OR cy1",1);
    test("bx1 OR cy2",2);
    test("notPresentInIndex OR notPresentInIndexEither",0);
  }

  @Test
  public void testOR2() {
    test("gx1 | notPresentInIndex",1);
    test("notPresentInIndex | gx1",1);
    test("bx1 | cx1",1);
    test("bx1 | cy1",1);
    test("bx1 | cy2",2);
    test("notPresentInIndex | notPresentInIndexEither",0);
  }

  @Test
  public void testAND() {
    test("gx1 AND notPresentInIndex",0);
    test("notPresentInIndex AND gx1",0);
    test("bx1 AND cx1",1);
    test("bx1 AND cx2",0);
    test("notPresentInIndex AND notPresentInIndexEither",0);
  }

  @Test
  public void testNOT() {
    test("money NOT ax1",1);
    test("ax1 NOT ay1",0);
    test("ax1 NOT bx1",0);
    test("bx1 NOT money",0);
  }

	/*
	 * Compound boolean queries
	 */

  @Test
  public void testCompoundBoolean_Match() {
    test("ax1 ADJ bx1 AND gx1",1);
    test("bx1 AND gx1 NEAR fx1",1);
    test("ax1 ADJ bx1 AND gx1 NEAR fx1",1);
    test("bx1 WITH cx1 AND px1 SAME rx1",1);
    test("ax1 ADJ bx1 OR ax2 ADJ bx2",2);
  }

  @Test
  public void testCompoundBoolean_Mismatch() {
    test("ax1 ADJ cx1 AND gx1",0);
    test("bx1 AND gx1 NEAR kx1",0);
    test("3 AND gx1 NEAR fx1",0);
    test("bx1 WITH cx1 AND ax1 SAME zx1",0);
  }

  @Test
  public void testCompoundBoolean_Mismatch_broken() {
    test("bx1 ADJ ax1 OR ax1 SAME zx1",0);
  }

  @Test
  public void testMixedAndMoreCompoundBoolean() {
    test("ax1 AND bx1 ADJ cx1 AND (dx1 SAME ex1 AND fx1) AND gx1 AND hx1",1);
    test("ax2 OR (bx1 ADJ cx1 AND (dx1 SAME ex1 AND fx1) AND gx1 AND hx1)",2);
  }

  @Test
  public void testMixedBooleanAndRange() {
    // Placeholder... Add test
  }

	/*
	 * Compound SWAN queries
	 */

  @Test
  public void testADJ_ADJ() {
    test("ax1 ADJ bx1 ADJ cx1",1);
    test("ex1 ADJ2 gx1 ADJ2 ix1",1);
    test("ex1 ADJ2 gx1 ADJ ix1",0);
    test("ex1 ADJ gx1 ADJ2 ix1",0);
    test("gx1 ADJ fx1 ADJ2 ix1",0);
  }

  @Test
  public void testNEAR_NEAR() {
    test("ax1 NEAR bx1 NEAR cx1",1);
    test("ex1 NEAR2 gx1 NEAR2 ix1",1);
    test("ex1 NEAR2 gx1 NEAR ix1",0);
    test("ex1 NEAR gx1 NEAR2 ix1",0);
    test("gx1 NEAR fx1 NEAR2 ix1",1);
  }

  @Test
  public void testWITH_NEAR() {
    //NEAR binds earlier than WITH, so ex1 NEAR2 gx1 WITH ix1 => (ex1 NEAR2 gx1) WITH ix1
    test("ex1 NEAR2 gx1 WITH ix1",1);
    test("ex1 NEAR2 gx1 WITH zx1",0);
    test("(ax1 WITH bx1) NEAR99 (yx1 WITH zx1)",1); //find ax1 sentence with ax1 and bx1 near ax1 sentence with yx1 and zx1
    test("ax1 WITH (bx1 NEAR99 yx1) WITH zx1",0); //find (bx1 near yx1) in the same sentence with ax1 and the same sentence with zx1
  }

  @Test
  public void testSAME_NEAR() {
    //NEAR binds earlier than SAME, so ex1 NEAR2 gx1 SAME ix1 => (ex1 NEAR2 gx1) SAME ix1
    test("ex1 NEAR2 gx1 SAME ax1",1);
    test("ex1 NEAR2 gx1 SAME ix1",1);
    test("ex1 NEAR2 gx1 SAME zx1",0);
  }

	/*
	 * Otherwise
	 */

  @Test
  public void testOverlapping() {
    test("(ex1 NEAR fx1) AND (ex1 ADJ fx1)",1);
    test("(ex1 NEAR2 gx1) AND (fx1 ADJ2 hx1)",1);
    test("(ex1 NEAR2 gx1) NEAR (fx1 ADJ2 hx1)",1);
  }

	/*
	 * FieldedQueries
	 */

  @Test
  public void testAtomicFieldedQuery() {
    test("ax1.x.",1);
    test("ax1.y.",0);
  }

  @Test
  public void testBooleanFieldedQuery() {
    test("(ax1 AND bx1).x.",1);
    test("(ax1 AND bx1).y.",0);

    //should nested fielded queries generate a warning or error?
    test("(ax1.x. AND bx1).x.",1);
//		test("(ax1.y. AND bx1).x.",0);
//		test("(ay1.y. AND bx1.x.",1); //this should cause an error, but it doesn't
    test("ay1.y. AND bx1.x.",1);
  }

  @Test
  public void testSwanFieldedQuery() {
    test("(ex1 NEAR2 gx1).x.",1);
    test("(ex1 NEAR2 gx1).y.",0);
//		test("ex1.x. NEAR2 gx1.x.",1);
  }

  @Test
  public void testComplexFieldedQuery() {

//		test("(ex1 NEAR2 gx1 SAME ix1).x.",1);
//		test("(ex1 NEAR2 gx1 SAME ix1).y.",0);
//		test("ex1.x. NEAR2 gx1 SAME ix1",1);
//		test("(ex1 NEAR2 gx1 SAME zx1).x",0);
  }

  @Test
  public void testFieldAliases() {
    //Can we use different names for fields as described by the fieldAliases.txt file in the conf directory?
    test("ax1.a.",1);
    test("ax1.A.",1);
    test("ay1.b.",1);
    test("ay1.B.",1);
  }

  @Test
  public void testCrossTermIssue() {
    //This should parse as
    // ( (ax1.x. AND ay1.x.) OR (ax1.y. AND ay1.y.) OR (ax1.x. AND ay1.y.) OR (ax1.y. AND ay1.x.) ) OR somethingNotThere
    //But currently parsed as
    //( (ax1.x. AND ay1.x.) OR (ax1.y. AND ay1.y.)) OR somethingNotThere
    //The difference is that the cross terms are missing.
    test("(ax1 AND ay1) OR somethingNotThere",1);
  }

  @Test
  public void testAndSemantics() {
    //The problem is that AND is getting parsed with OR semantics ... fix my making SHOULD match into MUST
    test("ax1 AND somethingNotThere",0);
  }

  @Test
  public void testAllDocsQuery() {
    test("*",9);
    test("* OR ax1",9);
    test("* AND ax1",1);
    test("(* AND ax1).x.",1);
  }

  @Test
  public void testPartiallyFieldedBoolean() {
    //This shouldn't fail
    test("a.x. OR b",0);
    test("a.x. AND b",0);
  }

  @Test
  public void testClassificationStyleFielded(){
    test("A61K36/16.z1.", 2);
  }

  @Test
  public void testClassificationStyleProximityFielded(){
    test("(A61K SAME (L ADJ I)).z1.", 1);
  }

  @Ignore //these aren't real test - but I keep them around because they are useful for examining the output queries
  public void testTrash() {
    test("(a AND b) AND (c AND d)",0);
    test("(a OR b) AND (c AND d)",0);
    test("(a OR b) OR (c AND d)",0);
    test("(a OR b) OR (c OR d)",0);
    test("(((a OR b) OR c) OR d)",0);
    test("(((a AND b) OR c) OR d)",0);
    test("(((a AND b) AND c) OR d)",0);
    test("(((a AND b) AND c) AND d)",0);
    test("(((a OR b) AND c) AND d)",0);
    test("(((a OR b) OR c) AND d)",0);
  }


  /*
   * Things to ponder
   */
  @Ignore
  public void thingsToPonder() {
    assertQ(req("qt","swan","q","ax1 ADJ (bx1 OR ax2) ADJ bx2"),"//*[@numFound='?']");
  }

  private void test(String q,int numFound) {
    assertQ(req("qt","swan",
      "debugQuery", "true",
      "q",q,
      "qf","x, y, z",
      "indent","true",
      "sm","xxxsentencexxx",
      "pm","xxxparagraphxxx"),"//*[@numFound='"+ Integer.toString(numFound) +"']");
  }

  private LocalSolrQueryRequest lquery(String q) {
    HashMap<String, String> args = new HashMap<String, String>();
    args.put("fl", "*");
    args.put("hl", "true");
    args.put("hl.fl", "x, y");
    args.put("f.x.hl.alternateField","x");
    args.put("f.y.hl.alternateField","y");
    args.put("qf", "x, y");
    args.put("indent", "true");
    args.put("sm", "xxxsentencexxx");
    args.put("pm", "xxxparagraphxxx");
    args.put("hl.usePhraseHighlighter", "true");
    args.put("hl.requireFieldMatch", "true");
    args.put("debugQuery", "true");
    TestHarness.LocalRequestFactory sumLRF = h.getRequestFactory("swan", 0,
      200, args);
    return sumLRF.makeRequest(q);
  }

}
