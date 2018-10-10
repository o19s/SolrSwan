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

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.solr.schema.IndexSchema;

public abstract class SwanProxNode extends SwanNode {

  protected SwanNode _left;
  protected SwanNode _right;
  protected Integer _proximity;

  public SwanProxNode(SwanNode left, SwanNode right, Integer proximity) {
    _left = left;
    _right = right;
    _proximity = proximity;

    if (proximity > 99)
      throw new IllegalArgumentException("For queries; ADJn, NEARn, WITHn, SAMEn. N must not exceed 99.");
  }

  @Override
  public String getField() {
    String field = _field;
    String left = _left.getField();
    String right = _right.getField();

    field = checkSet(field, left);

    field = checkSet(field, right);

    if (field == null)
      return null;

    if (_left instanceof SwanXOrOperationNode) {
      for (SwanNode n : ((SwanOperatorNode)_left).getNodes())
        checkSet(n.getField(), field);
    }

    if (_right instanceof SwanXOrOperationNode) {
      for (SwanNode n : ((SwanOperatorNode)_right).getNodes())
        checkSet(n.getField(), field);
    }

    return field;
  }

  private String checkSet(String set, String check) {
    if (set != null) {
      if (check != null && !check.equals(set))
        throw new IllegalArgumentException("Field mismatch error, Unable to execute query.");
      return set;
    }
    return check;
  }

  @Override
  public Query getQuery(String field) {
    if (_left instanceof SwanOperatorNode || _right instanceof SwanOperatorNode)
      return getConditionalQuery();
    return getSpanQuery(field);
  }

  @Override
  public SpanQuery getSpanQuery(String field) {
    return getSpanQuery(_left, _right, field);
  }

  public abstract SpanQuery getSpanQuery(SwanNode left, SwanNode right, String field);

  @Override
  public boolean isFielded() {
    if (_left.isFielded())
      return true;
    if (_right.isFielded())
      return true;

    return super.isFielded();
  }

//  protected List<SwanNode> _swanNodes = new ArrayList<SwanNode>();
//
//  public void addSwanNode(SwanNode node) {
//    _swanNodes.add(node);
//  }

  @Override
  public Query getQuery(String[] fields) {
    if (_left instanceof SwanOperatorNode || _right instanceof SwanOperatorNode)
      return getConditionalQuery();

    return super.getQuery(fields);
  }

  private Query getConditionalQuery() {
    if (_left instanceof SwanXOrOperationNode) {
      if (_right instanceof SwanXOrOperationNode)
        return dualXOrConditionalQuery();
      if (_right instanceof SwanOperatorNode)
        return xOrConditionalQuery(_left, _right, false);
      return singleXOrConditionalQuery(_right, _left, false);
    }
    if (_left instanceof SwanOperatorNode) {
      if (_right instanceof SwanXOrOperationNode)
        return xOrConditionalQuery(_right, _left, true);
      if (_right instanceof SwanOperatorNode)
        return dualConditionalQuery();
      return singleConditionalQuery(_left, _right, false);

    }
    if (_right instanceof SwanXOrOperationNode)
      return singleXOrConditionalQuery(_left, _right, true);
    else
      return singleConditionalQuery(_right, _left, true);
  }

  private Query dualConditionalQuery() {
    String lf_def = _left.getField();
    String rf_def = _right.getField();

    BooleanClause.Occur left_occur = ((SwanOperatorNode)_left).getClause();
    List<SwanNode> left_nodes = ((SwanOperatorNode)_left).getNodes();

    // Logic here for both left and right being an instance of IConditional, lots of ways this can play out.
    BooleanClause.Occur right_occur = ((SwanOperatorNode)_right).getClause();
    List<SwanNode> right_nodes = ((SwanOperatorNode)_right).getNodes();

    if (left_occur.equals(right_occur)) {
      return conditionalQuery(right_occur, left_nodes, right_nodes, lf_def, rf_def);
    }
    else if (left_occur.equals(BooleanClause.Occur.MUST)) {
      return nestedConditionalQuery(right_occur, left_occur, left_nodes, right_nodes, lf_def, rf_def, false);
    }
    else {
      return nestedConditionalQuery(left_occur, right_occur, right_nodes, left_nodes, rf_def, lf_def, true);
    }
  }

  private Query nestedConditionalQuery(BooleanClause.Occur innerOccur, BooleanClause.Occur outerOccur, List<SwanNode> left, List<SwanNode> right, String lfDef, String rfDef, boolean reverse) {
    BooleanQuery query = new BooleanQuery();
    BooleanQuery innerQuery;
    for (SwanNode l : left) {
      String lf = l.getField() != null ? l.getField() : lfDef;
      innerQuery = new BooleanQuery();
      for (SwanNode r : right) {
        String f = r.getField() != null ? r.getField() : rfDef;
        if (f == null)
          f = lf;
        else if (lf != null && !lf.equals(f))
          continue;

        if (reverse)
          addSpanQuery(innerQuery, innerOccur, r, l, f);
        else
          addSpanQuery(innerQuery, innerOccur, l, r, f);
      }
      if (innerQuery.getClauses().length == 0)
        throw new IllegalArgumentException("Clauses must have same field.");
      query.add(innerQuery, outerOccur);
    }

    return query;
  }

  private Query conditionalQuery(BooleanClause.Occur occur, List<SwanNode> left, List<SwanNode> right, String lfDef, String rfDef) {
    BooleanQuery query = new BooleanQuery();
    for (SwanNode l : left) {
      String lf = l.getField() != null ? l.getField() : lfDef;
      for (SwanNode r : right) {
        String f = r.getField() != null ? r.getField() : rfDef;
        if (f == null)
          f = lf;
        else if (lf != null && !lf.equals(f))
          continue;

        addSpanQuery(query, occur, l, r, f);
      }
    }

    return query;
  }

  private Query singleXOrConditionalQuery(SwanNode left, SwanNode right, boolean reverse) {
    BooleanQuery q =  (BooleanQuery) singleConditionalQuery(right, left, reverse);
    return xOrNodes(q);
  }

  private Query xOrConditionalQuery(SwanNode left, SwanNode right, boolean reverse) {
    String lf_def = left.getField();
    String rf_def = right.getField();

    BooleanClause.Occur occur = reverse
      ? ((SwanOperatorNode)right).getClause()
      : ((SwanOperatorNode)left).getClause();
    List<SwanNode> left_nodes = ((SwanOperatorNode)left).getNodes();
    List<SwanNode> right_nodes = ((SwanOperatorNode)right).getNodes();

    List<Query> nodes = new ArrayList<Query>();

    for (SwanNode left_node : left_nodes) {
      BooleanQuery outer = new BooleanQuery();
      String lf = checkSet(left_node.getField(), lf_def);

      for (SwanNode right_node : right_nodes) {
        String rf = checkSet(right_node.getField(), rf_def);
        String f = checkSet(lf, rf);

        if (reverse)
          addSpanQuery(outer, occur, right_node, left_node, f);
        else
          addSpanQuery(outer, occur, left_node, right_node, f);
      }
      nodes.add(outer);
    }

    return xOrNodes(nodes);
  }

  private Query dualXOrConditionalQuery() {
    String lf_def = _left.getField();
    String rf_def = _right.getField();

    List<Query> nodes = new ArrayList<Query>();

    for (SwanNode left : ((SwanOperatorNode)_left).getNodes()) {
      String lf = checkSet(left.getField(), lf_def);
      for (SwanNode right : ((SwanOperatorNode)_right).getNodes()) {
        String rf = checkSet(right.getField(), rf_def);
        String f = checkSet(lf, rf);

        // TODO: This I believe can be refactored to something cleaner.
        if (f == null) {
          BooleanQuery inner = new BooleanQuery();
          for (String _f : defaultFields)
            inner.add(getSpanQuery(left, right, _f), BooleanClause.Occur.SHOULD);
          nodes.add(inner);
        } else {
          nodes.add(getSpanQuery(left, right, f));
        }
      }
    }

    return xOrNodes(nodes);
  }

  private Query xOrNodes(BooleanQuery query) {
    List<Query> queries = new ArrayList<Query>(query.getClauses().length);
    for (BooleanClause clause : query.getClauses())
      queries.add(clause.getQuery());
    return xOrNodes(queries);
  }

  private Query xOrNodes(List<Query> queries) {
    BooleanQuery query = new BooleanQuery();
    ArrayList<Query> sub_nodes = new ArrayList<Query>(queries.size());

    for (int x = 0; x < queries.size(); x++) {
      sub_nodes.clear();
      sub_nodes.addAll(queries);
      sub_nodes.remove(x);

      BooleanQuery inner = new BooleanQuery();
      for (Query q : sub_nodes)
        inner.add(q, BooleanClause.Occur.MUST_NOT);
      inner.add(queries.get(x), BooleanClause.Occur.MUST);
      query.add(inner, BooleanClause.Occur.SHOULD);
    }

    return query;
  }

  private Query singleConditionalQuery(SwanNode left, SwanNode right, boolean reverse) {
    BooleanQuery query = new BooleanQuery();
    String lf_def = left.getField();
    String rf_def = right.getField();

    BooleanClause.Occur outerOccur = left instanceof SwanXOrOperationNode
      ? BooleanClause.Occur.MUST
      : ((SwanOperatorNode) left).getClause();
    List<SwanNode> left_nodes = ((SwanOperatorNode)left).getNodes();

    for (SwanNode l : left_nodes) {
      String lf = l.getField() != null ? l.getField() : lf_def;
      if (lf != null && rf_def != null && !lf.equals(rf_def))
        throw new IllegalArgumentException("Clauses must have same field.");
      if (lf == null)
        lf = rf_def;

      if (reverse)
        addSpanQuery(query, outerOccur, right, l, lf);
      else
        addSpanQuery(query, outerOccur, l, right, lf);
    }

    return query;
  }

  private void addSpanQuery(BooleanQuery query, BooleanClause.Occur occur, SwanNode left, SwanNode right, String field) {
    if (field == null) {
      if (occur.equals(BooleanClause.Occur.SHOULD))
        // Optimize OR queries, don't require nesting
        for (String _f : defaultFields)
          query.add(getSpanQuery(left, right, _f), BooleanClause.Occur.SHOULD);
      else {
        BooleanQuery q = new BooleanQuery();
        for (String _f : defaultFields)
          q.add(getSpanQuery(left, right, _f), BooleanClause.Occur.SHOULD);
        query.add(q, occur);
      }
    }
    else
      query.add(getSpanQuery(left, right, field), occur);
  }

  @Override
  public void setSchema(IndexSchema schema) {
    _left.setSchema(schema);
    _right.setSchema(schema);
    super.setSchema(schema);
  }
}
