package com.o19s.solr.swan.nodes;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.solr.schema.IndexSchema;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SwanNodeUtils {
    public static List<String> tokenizeString(IndexSchema schema, String phrase, String field) {
        List<String> result = new ArrayList<String>();
        TokenStream stream;
        try {
            stream  = schema.getField(field).getType().getQueryAnalyzer().tokenStream(field, new StringReader(phrase));
            stream.reset();
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
            stream.close();
        } catch (IOException e) {
            // not thrown b/c we're using a string reader...
            throw new RuntimeException(e);
        }
        return result;
    }
}
