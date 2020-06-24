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
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;

public class SwanOrOperationNode extends SwanOperatorNode {

  public SwanOrOperationNode(SwanNode left, SwanNode right) {
    super(left, right);
  }

  //this is a copy constructor
  public SwanOrOperationNode(SwanOrOperationNode originalNode) {
    super(originalNode.getNodes().toArray(new SwanNode[originalNode.getNodes().size()]));
  }

  @Override
  public SpanQuery getSpanQuery(String field) {
    SpanQuery [] nodes = new SpanQuery[_nodes.size()];
    for(int i = 0; i < _nodes.size(); i++) {
      nodes[i] = _nodes.get(i).getSpanQuery(field);
    }
    return new SpanOrQuery(nodes);
  }

  @Override
  protected String getOperation() {
    return "OR";
  }

  @Override
  public BooleanClause.Occur getClause() {
    return BooleanClause.Occur.SHOULD;
  }
}
