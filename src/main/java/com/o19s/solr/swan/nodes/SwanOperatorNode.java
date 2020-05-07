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

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.solr.schema.IndexSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SwanOperatorNode extends SwanNode {

  protected List<SwanNode> _nodes;

  public SwanOperatorNode(SwanNode... nodes) {
    _nodes = new ArrayList(Arrays.asList(nodes));
  }

  public void add(SwanNode node) {
    _nodes.add(node);
  }

  protected abstract String getOperation();
  protected abstract BooleanClause.Occur getClause();

  @Override
  public String getField() {
    return _field;
  }

  @Override
  public boolean isFielded() {
    if (_field == null)
      return false;

    String field;
    for(SwanNode n : _nodes){
      field = n.getField();
      if (field != null && !field.equals(_field))
        throw new IllegalArgumentException("Field mismatch error, Unable to execute query.");
    }

    return true;
  }

  @Override
  public Query getQuery(String field) {
    BooleanQuery query = new BooleanQuery();
    for(SwanNode n : _nodes){
      Query q = n.getQuery(field);
      query.add(q,getClause());
    }
    return query;
  }

  @Override
  public Query getQuery(String[] fields) {
    BooleanQuery query = new BooleanQuery();
    for(SwanNode n : _nodes){
      query.add(n.getQuery(), getClause());
    }
    return query;
  }

  public List<SwanNode> getNodes() {
    return _nodes;
  }

  protected SpanQuery[] getQueries(String field) {
    SpanQuery[] queries = new SpanQuery[_nodes.size()];

    for (int x = 0; x < _nodes.size(); x++) {
      queries[x] = _nodes.get(x).getSpanQuery(field);
    }

    return queries;
  }

  @Override
  public void setSchema(IndexSchema schema) {
    for (SwanNode n : getNodes()) {
      n.setSchema(schema);
    }
    super.setSchema(schema);
  }

  @Override
  public String toString() {
    String field = _field != null
        ? "," + _field
        : "";
    return getOperation() +"("+ StringUtils.join(_nodes, ",") + field + ")";
  }
}
