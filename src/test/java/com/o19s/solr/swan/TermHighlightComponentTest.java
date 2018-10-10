package com.o19s.solr.swan;

/*
 * Copyright 2012 OpenSource Connections, LLC.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import junit.framework.Assert;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 *
 **/
public class TermHighlightComponentTest extends SolrTestCaseJ4 {

    @BeforeClass
    public static void beforeClass() throws Exception {
        initCore("solrconfig.xml","schema.xml");
        assertU(adoc("id", "0",
                "fruit", "apple"
        ));
        assertU(adoc("id", "1",
                "fruit", "apple orange"
        ));
        assertU(adoc("id", "2",
                "fruit", "a seed bearing plant"
        ));
        assertU(adoc("id", "3",
                "fruit", "a bearish flower"
        ));
        assertU(adoc("id", "4",
                "animal", "giraffe"
        ));
        assertU(adoc("id", "5",
                "fruit", "giraffe is not a fruit!"
        ));
        assertNull(h.validateUpdate(commit()));
    }

    static String tv = "tvrh";

    private SolrQueryRequest getRequest(String q) {
        return  req(
            "json.nl","map", 
            "df", "fruit", 
            "qt",tv, 
            "q", q,
            TermHighlightComponent.COMPONENT_NAME, "true");
    }

    @Test
    public void testSingleTerm() throws Exception {

        SolrQueryRequest request;
        request = getRequest("orange");
        assertJQ(request
                ,"/termHighlights=={" +
                "'orange':{ " +
                   "'termFrequency':1," +
                   "'id':10}}}");
    }


    @Test
    public void testMultiTerm() throws Exception {

        SolrQueryRequest request;
        // q.op is adj by default
        request = getRequest("apple orange");
        assertJQ(request
            ,"/termHighlights=={" +
                    "'apple':{'termFrequency':1, 'id':170}," +
                    "'orange':{'termFrequency':1, 'id':10}}");
    }

  @Test
  public void testAndMultiTerm() throws Exception {

    SolrQueryRequest request;
    request = getRequest("apple AND orange");
    assertJQ(request
      ,"/termHighlights=={" +
      "'apple':{'termFrequency':1, 'id':170}," +
      "'orange':{'termFrequency':1, 'id':10}}");
  }

  @Test
  public void testOrMultiTerm() throws Exception {

    SolrQueryRequest request;
    request = getRequest("apple OR orange");
    assertJQ(request
      ,"/termHighlights=={" +
      "'apple':{'termFrequency':2, 'id':170}," +
      "'orange':{'termFrequency':1, 'id':10}}");
  }

  @Test
  public void testOrMultiTermMissing1() throws Exception {

    SolrQueryRequest request;
    request = getRequest("apple OR miss");
    assertJQ(request
      ,"/termHighlights=={" +
      "'apple':{'termFrequency':2, 'id':170}}");
  }

  @Test
  public void testOrMultiTermMissing2() throws Exception {

    SolrQueryRequest request;
    request = getRequest("drop OR miss");
    assertJQ(request
      ,"/termHighlights=={}");
  }

  @Test
    public void testPhrase() throws Exception {

        SolrQueryRequest request;
        request = getRequest("\"apple orange\"");
        assertJQ(request
                ,"/termHighlights=={" +
                "'apple':{'termFrequency':1, 'id':170}," +
                "'orange':{'termFrequency':1, 'id':10}}");
    }

    @Test
    public void testFieldSearch() throws Exception {

        SolrQueryRequest request;
        request = getRequest("giraffe.animal.");
        // Note that giraffe shows up a second time in the fruit field, but
        // is not counted in the results.
        assertJQ(request
                ,"/termHighlights=={" +
                "'giraffe':{'termFrequency':1, 'id':132}}");
    }

    @Test
    public void testWildcard() throws Exception {

        SolrQueryRequest request;
        request = getRequest("bear*");
        assertJQ(request
                ,"/termHighlights=={" +
                "'bearing':{'termFrequency':1, 'id':298}," +
                "'bearish':{'termFrequency':1, 'id':142}}");
    }

    @Test
    public void testFinishStage() throws Exception {

        TermHighlightComponent c = new TermHighlightComponent();
        NamedList<NamedList> n1, n2;
        NamedList<Object> nsub1, nsub2;

        nsub1 = new NamedList<Object>();
        nsub1.add("termFrequency", 1l);
        nsub1.add("id", 100);

        nsub2 = new NamedList<Object>();
        nsub2.add("termFrequency", 4l);
        nsub2.add("id", 100);

        n1 = new NamedList<NamedList>();
        n1.add("john", nsub1);

        n2 = new NamedList<NamedList>();
        n2.add("john", nsub2);

        Assert.assertEquals(1l, nsub1.get("termFrequency"));
        c.merge(n1, n2);

        Assert.assertEquals(1, n1.size());
        Assert.assertEquals("john", n1.getName(0));

        Assert.assertEquals(2, nsub1.size());
        Assert.assertEquals(100, nsub1.get("id"));
        Assert.assertEquals(5l, nsub1.get("termFrequency"));

    }

}
