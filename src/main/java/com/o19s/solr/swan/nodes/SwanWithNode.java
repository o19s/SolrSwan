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

//import com.o19s.solr.swan.query.SpanWithinQuery;
import org.apache.lucene.index.Term;
//import org.apache.lucene.search.BooleanClause;
//import org.apache.lucene.search.BooleanQuery;
//import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
//import org.apache.lucene.search.spans.SpanWithinQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

public class SwanWithNode extends SwanProxNode {

  // TODO: Remove n from operator if not required
  public SwanWithNode(SwanNode left, SwanNode right, int proximity) {
    super(left, right, proximity);
  }
  
  //this is a copy constructor
  public SwanWithNode(SwanWithNode originalNode){
	  this(originalNode._left, originalNode._right, originalNode._proximity);
  }

  public SpanQuery getSpanQuery(SwanNode left, SwanNode right, String field) {
    SpanQuery terms = new SpanNearQuery(
        new SpanQuery[] { left.getSpanQuery(field), right.getSpanQuery(field) },
        Integer.MAX_VALUE,
        false
    );
    SpanQuery sentence_boundary = new SpanTermQuery(new Term(field, getSentenceMarker()));
    return new SpanNotQuery(terms,sentence_boundary);
//    return new SpanWithinQuery(terms, sentence_boundary, _proximity);
  }

  @Override
  public String toString() {
    return "WITH("+ _left +","+ _right +")";
  }

}
