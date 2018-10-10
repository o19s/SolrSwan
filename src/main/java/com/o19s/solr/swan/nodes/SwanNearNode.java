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

import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;

public class SwanNearNode extends SwanProxNode {

  public SwanNearNode(SwanNode left, SwanNode right, int proximity) {
    super(left, right, proximity);
  }

  //this is a copy constructor
  public SwanNearNode(SwanNearNode originalNode) {
	this(originalNode._left, originalNode._right, originalNode._proximity);
  }
  
  public SpanQuery getSpanQuery(SwanNode left, SwanNode right, String field) {
    return new SpanNearQuery(
        new SpanQuery[] { left.getSpanQuery(field), right.getSpanQuery(field) },
        _proximity-1,
        false
    );
  }

  @Override
  public String toString() {
    return "NEAR("+ _left +","+ _right +","+ _proximity +")";
  }

}
