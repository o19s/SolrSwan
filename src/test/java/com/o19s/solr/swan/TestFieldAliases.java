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

import org.apache.solr.SolrTestCaseJ4;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFieldAliases extends SolrTestCaseJ4 {

	@BeforeClass
	public static void beforeClass() throws Exception {
		initCore("solrconfig.xml", "schema.xml");
		createIndex();
	}

	public static void createIndex() {
		//Note, in the test schema.xml there is a field named "text" to which all other fields are copied
		int i = 1;
		assertU(adoc(
				"id", Integer.toString(i),
				"x","Here is some text, and some more, then even more...",
				"y","ABC",
				"fruit", "a seed bearing plant",
				"z","should find this through c",
				"z2", "this is a multi to multi test",
                "range", "2001"
		));
		i++;
		assertU(adoc(
				"id", Integer.toString(i),
				"x","Here is some text",
				"y","ABC",
				"fruit", "a flowering plant",
				"z1", "should find this through c",
				"z3", "this is a multi to multi test",
                "range", "2005"
			));
		i++;
		assertU(commit());
	}

	@Test
  //test field alias case dependency
  public void testAliasCase() {
	  test("this.c.", 2);
	  test("this.C.", 2);
	}
	
	@Test
	//test some or conditions for a current bug
	public void testOrBug(){
		test("(should | find).z. OR (find).z.", 1);
	}

	@Test
	//test out mappings where a is aliasing x and b is aliasing y
	public void testOneToOneAliases() {
		test("Here*.a.",2);
		test("Here is some text.a.",2);
		test("even.a.",1);
		test("abc.b.", 2);
	}

	@Test
	//test out fieldStemming param
	public void testFieldStemming() {
        testStemming("bier.fruit.", 0);
        testStemming("bearing.fruit.", 1);
        testStemming("bear.fruit.", 1);
	}
	
	@Test
	//test out mappings where apple, banana, coconut, and donut are aliases to fruit
	public void testManyToOneAliases(){
		test("plant.apple.", 2);
	}
	
	@Test
	//test out mappings where c can be an alias for z or z1
	public void testOneToManyAliases(){
		test("should find this through c.c.", 2);
	}

	@Test
	//test out mappings where d or e can be aliases for z2 or z3
	public void testManyToManyAliases(){
		test("this is a multi to multi.d.", 2);
		test("this is a multi to multi.e.", 2);
		test("this is a multi to multi.f.", 1);
	}

    @Test
    public void testRangeFieldAliases() {
        test("@rg < 2010", 2);
        test("@rg > 2010", 0);
        test("@rg > 2003", 1);
        test("@rg < 2003", 1);
        test("@rg >= 1995<=2002", 1);
    }

    @Test
    public void testEqualityWithFieldAliases() {
        test("@rg = 2001", 1);
        test("@rg =2002", 0);
    }

    @Test
    public void testInEqualityWithFieldAliases(){
        test("@rg <> 2001", 1);
        test("@rg <> 2002", 2);
        test("@rg <> 2001", 1);
    }

    @Test
    public void testCaseInsensitivityInFieldAliases(){
        test("@RG <> 2001", 1);
        test("@rg <> 2001", 1);
        test("@rG <> 2001", 1);
        test("@Rg <> 2001", 1);
    }

	private void test(String q, int numFound) {
	   assertQ(req("qt", "swan",
               "debugQuery", "true",
               "q", q,
               "qf", "x, y",
               "indent", "true",
               "sm", "xxxsentencexxx",
               "pm", "xxxparagraphxxx"), "//*[@numFound='" + Integer.toString(numFound) + "']");
	}

	private void testStemming(String q, int numFound) {
	   assertQ(req("qt","swan",
			      "debugQuery", "true",
                  "fieldStemming", "true",
			      "q",q,
			      "qf","x, y",
			      "indent","true",
			      "sm","xxxsentencexxx",
				  "pm","xxxparagraphxxx"),"//*[@numFound='"+ Integer.toString(numFound) +"']");
	}

}
