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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

public class SwanPhraseNode extends SwanNode {

  private String phrase;

  public SwanPhraseNode(String phrase) {
    this.phrase = phrase;
  }

  //this is a copy constructor
  public SwanPhraseNode(SwanPhraseNode originalNode) {
	this(originalNode.phrase);
  }

  @Override
  public Query getQuery(String field) {
    List<String> words = SwanNodeUtils.tokenizeString(schema, phrase, field);

    if (words.size() == 1)
      return new SpanTermQuery(new Term(field, words.get(0)));
    return getSpanQuery(field, words);
  }

  @Override
  public SpanQuery getSpanQuery(String field) {
    List<String> words = SwanNodeUtils.tokenizeString(schema, phrase, field);
    return getSpanQuery(field, words);
  }

  private SpanQuery getSpanQuery(String field, List<String> words) {
    List<SpanQuery> tqs = new ArrayList<SpanQuery>(words.size());
    for (String word : words) {
      tqs.add(new SpanTermQuery(new Term(field, word)));
    }

    return new SpanNearQuery(tqs.toArray(new SpanQuery[tqs.size()]), 0, true);
  }

  @Override
  public String toString() {
    return "PHRASE("+ phrase +")";
  }


}
