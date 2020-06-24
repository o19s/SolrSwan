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

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.solr.schema.IndexSchema;

public class SwanNotNode extends SwanNode {

  SwanNode _left;
  SwanNode _right;

  public SwanNotNode(SwanNode left, SwanNode right) {
    _left = left;
    _right = right;
  }

  //this is a copy constructor
  public SwanNotNode(SwanNotNode originalNode) {
	this(originalNode._left, originalNode._right);
  }

  @Override
  public String toString() {
    return "NOT("+ _left +","+ _right +")";
  }

  @Override
  public Query getQuery(String field) {
    BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
    queryBuilder.add(_left.getQuery(field),BooleanClause.Occur.MUST);
    queryBuilder.add(_right.getQuery(field),BooleanClause.Occur.MUST_NOT);
    return queryBuilder.build();
  }

  @Override
  public SpanQuery getSpanQuery(String field) {
    return new SpanNotQuery(_left.getSpanQuery(field), _right.getSpanQuery(field));
  }

  @Override
  public Query getQuery(String[] fields) {
    BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

    queryBuilder.add(_left.getQuery(),BooleanClause.Occur.MUST);
    queryBuilder.add(_right.getQuery(),BooleanClause.Occur.MUST_NOT);

    return queryBuilder.build();
  }

  @Override
  public void setSchema(IndexSchema schema) {
    _left.setSchema(schema);
    _right.setSchema(schema);
    super.setSchema(schema);
  }
}
