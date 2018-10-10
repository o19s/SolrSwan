package com.o19s.solr.swan;



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

public interface ISwanSearcher<V> {

  V or(V a, V b);
  V xor(V a, V b);
  V and(V a, V b);
  V same(V a, V b, int n);
  V with(V a, V b, int n);
  V near(V a, V b, int n);
  V adj(V a, V b, int n);
  V defaultOp(V a, V b);
  V not(V a, V b);
  V term(String match);
  V phrase(String match);
  V boundRange(String field, String op1, String val1, String op2, String val2);
  V range(String field,String op1, String val1);
  V classRange(String field, String mainClassification, String subClassification1, String subClassification2);
  V fieldedExpression(String field,V expression);
  V fieldedSubExpressions(String field, V expression);

  V wrap(V peek);
}
