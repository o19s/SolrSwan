package com.o19s.solr.swan.nodes;

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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TextField;

import com.o19s.solr.swan.query.SpanRegexpQuery;
//import org.apache.commons.lang3.StringUtils;

public class SwanTermNode extends SwanNode {

  private String _term;
  private String _wildcardTerm;
  private Boolean wildcard = false;
  static final Pattern WILDCARD_PATTERN = Pattern.compile("\\*|\\?|\\$(\\d{0,2})");


  public SwanTermNode(String term) {
    this._term = term;

    if(WILDCARD_PATTERN.matcher(term).find()) {
      wildcard = true;
    }

  }
  
  //this is a copy constructor
  public SwanTermNode(SwanTermNode originalNode) {
	  this(originalNode._term);
  }

  public Query getQuery() {
    if(isMatchAll())
      return new MatchAllDocsQuery();
    return super.getQuery();
  }

  public Query getQuery(String field) {
    if(!wildcard)
      return schema.getField(field).getType().getFieldQuery(_parser, schema.getField(field), _term);

    if (_wildcardTerm != null)
      return new RegexpQuery(new Term(field, _wildcardTerm));

    String term = analyzeIfMultitermTermText(field, _term, schema.getFieldType(field));
    _wildcardTerm = translateWildcard(term);
    return new RegexpQuery(new Term(field, _wildcardTerm));
  }

  public boolean isMatchAll() {
    return _term.equals("*");
  }

  @Override
  public SpanQuery getSpanQuery(String field) {
    if(wildcard) {
//      return new SpanMultiTermQueryWrapper<RegexpQuery>((RegexpQuery) getQuery(field));
      return new SpanRegexpQuery((RegexpQuery) getQuery(field));
    } else {
      // check for '/', if it exists get the SpanQuery from the SwanPhraseNode
      if (SwanNodeUtils.tokenizeString(schema, _term, field).size() > 1){
          return getSpanPhraseQuery(field);
      }
      String term = analyzeIfMultitermTermText(field, _term, schema.getFieldType(field));
      return new SpanTermQuery(new Term(field, term));
    }
  }

    // Since SwanPhraseQuery handles classifications with '/' characters by tokenizing the string, we are going
    // to return a SpanQuery generated from SwanPhraseQuery
    private SpanQuery getSpanPhraseQuery(String field) {
        SwanPhraseNode swanPhraseNode  = new SwanPhraseNode(_term);
        swanPhraseNode.setSchema(this.schema);
        swanPhraseNode.setParser(this._parser);
        return swanPhraseNode.getSpanQuery(field);
    }

  private String analyzeIfMultitermTermText(String field, String part, FieldType fieldType) {
    if (part == null) return part;

    SchemaField sf = schema.getFieldOrNull(field);
    if (sf == null || ! (fieldType instanceof TextField)) return part;

    String out = TextField.analyzeMultiTerm(field, part, ((TextField)fieldType).getMultiTermAnalyzer()).utf8ToString();
    // System.out.println("INPUT="+part + " OUTPUT="+out);
    return out;
  }

  @Override
  public String toString() {
    String f = _field == null ? "" : _field + ":";
    if(wildcard) {
      return "WILDCARD("+ f + _term +")";
    } else {
      return "TERM("+ f + _term +")";
    }
  }

  private static String translateWildcard(String in) {
    Matcher m = WILDCARD_PATTERN.matcher(in);
    StringBuffer sb = new StringBuffer(in.length());

    while (m.find())
    {
      if (m.group(0).equals("?")) {
        m.appendReplacement(sb, ".");
      }
      else if (m.group(0).equals("*")) {
        m.appendReplacement(sb, ".*");
      }
      else {
        if (!m.group(1).isEmpty())
          m.appendReplacement(sb, ".{0," + m.group(1) + "}");
        else
          m.appendReplacement(sb, ".*");
      }
    }
    m.appendTail(sb);

    return sb.toString();
  }
}