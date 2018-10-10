package com.o19s.solr.swan.query;

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

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: mike
 * Date: 2/18/13
 * Time: 8:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpanRegexpQuery extends SpanMultiTermQueryWrapper<RegexpQuery> {
  private static final int MAX_MTQ_TERMS = 1024;
  private SpanQuery rewrittenQuery = null;

  public SpanRegexpQuery(RegexpQuery query) {
    super(query);
  }


  @Override
  public Spans getSpans(AtomicReaderContext context, Bits acceptDocs,
                        Map<Term, TermContext> termContexts) throws IOException {
    if (rewrittenQuery == null) {
      //TODO this approach is to avoid disassembling, rewriting, and then reassembling query at time of highlighting just to get spans and terms.
      //I think this approach is not ideal. A better approach is due once the SpanQueries and Queries have been merged.
      this.setRewriteMethod(new SpanMultiTermQueryWrapper.TopTermsSpanBooleanQueryRewrite(MAX_MTQ_TERMS));
      rewrittenQuery = (SpanQuery) this.rewrite(context.reader());
    }
    return rewrittenQuery.getSpans(context, acceptDocs, termContexts);
  }

  @Override
  public void extractTerms(java.util.Set<Term> terms) {
    if (rewrittenQuery != null)
      rewrittenQuery.extractTerms(terms);
//    else
//      System.out.println("getSpans was not called before extractTerms.");
  }
}
