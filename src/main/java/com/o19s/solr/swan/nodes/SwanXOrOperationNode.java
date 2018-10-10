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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;

public class SwanXOrOperationNode extends SwanOperatorNode {

  public SwanXOrOperationNode(SwanNode left, SwanNode right) {
    _nodes.add(left);
    _nodes.add(right);
  }
  
  //this is a copy constructor
  public SwanXOrOperationNode(SwanXOrOperationNode originalNode){
	  this(originalNode.getNodes().get(0), originalNode.getNodes().get(1));
  }

  public void add(SwanNode node) {
    _nodes.add(node);
  }

  @Override
  protected String getOperation() {
    return "XOR";
  }

  @Override
  protected BooleanClause.Occur getClause() {
    return BooleanClause.Occur.SHOULD;
  }

  @Override
  public Query getQuery(String field) {
    return getSpanQuery(field);
  }

  @Override
  public Query getQuery(String[] fields) {
    BooleanQuery query = new BooleanQuery();

    List<SwanNode> inc;
    for (int x = 0; x < _nodes.size(); x++) {
      inc = new ArrayList<SwanNode>();
      inc.addAll(_nodes);
      inc.remove(x);

      BooleanQuery inner = new BooleanQuery();
      for (SwanNode n : inc) {
        inner.add(n.getQuery(), BooleanClause.Occur.MUST_NOT);
      }
      inner.add(_nodes.get(x).getQuery(), BooleanClause.Occur.MUST);
      query.add(inner, BooleanClause.Occur.SHOULD);
    }

    return query;
  }

  @Override
  public SpanQuery getSpanQuery(String field) {
    SpanOrQuery query = new SpanOrQuery();

    List<SwanNode> inc;
    for (int x = 0; x < _nodes.size(); x++) {
      inc = new ArrayList<SwanNode>();
      inc.addAll(_nodes);
      inc.remove(x);

      SpanOrQuery or = new SpanOrQuery();
      for (SwanNode n : inc) {
        or.addClause(n.getSpanQuery(field));
      }
      if (or.getClauses().length > 0) {
        SpanNotQuery not = new SpanNotQuery(_nodes.get(x).getSpanQuery(field), or);
        query.addClause(not);
      }
    }

    return query;
  }

  @Override
  public String toString() {
    return "XOR(" + StringUtils.join(_nodes, ",") + ")";
  }
}
