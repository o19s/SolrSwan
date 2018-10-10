package com.o19s.solr.swan.highlight;

import org.apache.lucene.analysis.MockAnalyzer;
import org.apache.lucene.analysis.MockTokenFilter;
import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;
import org.testng.Assert;

import java.io.IOException;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: dan
 * Date: 6/20/14
 * Time: 3:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class WordHashFragmentsBuilderTest extends LuceneTestCase {

    private static final String FIELD_NAME = "contents";


    @Test
    public void testHashCreation() {

        int a,b,c,d,e,f,g;
        WordHashFragmentsBuilder formatter = new WordHashFragmentsBuilder();

        a = formatter.toHash("dan");
        b = formatter.toHash("funk");
        c = formatter.toHash("Word!");
        d = formatter.toHash("123rf;lkasfqwerq3412341234cwefwe23124");
        e = formatter.toHash("12");
        f = formatter.toHash("clouds and other random stuff");
        a = formatter.toHash("the");

        Assert.assertNotEquals(a,b);
        Assert.assertNotEquals(b,c);
        Assert.assertNotEquals(c,d);
        Assert.assertNotEquals(d,e);
        Assert.assertNotEquals(e,f);

        Assert.assertTrue(a < 360);
        Assert.assertTrue(b < 360);
        Assert.assertTrue(c < 360);
        Assert.assertTrue(d < 360);
        Assert.assertTrue(e < 360);
        Assert.assertTrue(f < 360);

        a = formatter.toHash("dan");
        b = formatter.toHash("dan");
        c = formatter.toHash("dan");
        d = formatter.toHash("dan");
        e = formatter.toHash("dan");
        f = formatter.toHash("dan");

        Assert.assertEquals(a,b);
        Assert.assertEquals(b,c);
        Assert.assertEquals(c,d);
        Assert.assertEquals(d,e);
        Assert.assertEquals(e,f);

    }


    @Test
    public void testHashIngoresCase() {
        int a,b,c,d;
        WordHashFragmentsBuilder formatter = new WordHashFragmentsBuilder();

        a = formatter.toHash("dan");
        b = formatter.toHash("DAN");
        c = formatter.toHash("dAn");
        d = formatter.toHash("Dan");

        Assert.assertTrue(a == b && b == c && c == d);

    }

}

