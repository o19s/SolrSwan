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

public class JsonStubSwanSearcher implements ISwanSearcher<String> {

	@Override
	public String or(String a, String b) {
		return "{\"OR\":[" + a + "," + b + "]}";
	}
	
	@Override
	public String xor(String a, String b) {
		return "{\"XOR\":[" + a + "," + b + "]}";
	}
	
	@Override
	public String and(String a, String b) {
		return "{\"AND\":[" + a + "," + b + "]}";
	}

	@Override
	public String same(String a, String b, int n) {
		return "{\"SAME\":[" + a + "," + b + "," + n + "]}";
	}

	@Override
	public String with(String a, String b, int n) {
		return "{\"WITH\":[" + a + "," + b + "," + n + "]}";
	}

	@Override
	public String near(String a, String b, int n) {
		return "{\"NEAR\":[" + a + "," + b + "," + n + "]}";
	}

	@Override
	public String adj(String a, String b, int n) {
		return "{\"ADJ\":[" + a + "," + b + "," + n + "]}";
	}

    @Override
    public String not(String a, String b) {
        return "{\"NOT\":[" + a + "," + b + "]}";
    }
	
	@Override
	public String term(String str) {
		if(str.matches(".*[?*$].*")) {
			return "{\"WILDCARD\":\"" + str + "\"}";
		} else {
			return "{\"TERM\":\"" + str + "\"}";
		}
	}

	@Override
	public String phrase(String str) {
		return "{\"PHRASE\":\"" + str + "\"}";
	}

  @Override
  public String boundRange(String field, String op1, String val1, String op2, String val2) {
    return "{\"RANGE\":{\"field\":\""+ field +"\", \"op\":\""+ op1 +"\", \"val\":\""+ val1 +"\", \"op\":\""+ op2 +"\", \"val\":\""+ val2 +"\"}}";//,"+ op2 +","+ val2 +")";
  }

  @Override
	public String range(String field, String op1, String val1) {
		return "{\"RANGE\":{\"field\":\""+ field +"\", \"op\":\""+ op1 +"\", \"val\":\""+ val1 +"\"}}";//,"+ op2 +","+ val2 +")";
	}

  public String classRange(String field, String mainClassification, String subClassification1, String subClassification2) {
	return "{\"CLASS_RANGE\":{\"field\":\""+ field +"\", \"mainClassification\":\""+ mainClassification +"\", \"subClassification1\":\""+ subClassification1 +"\", \"subClassification2\":\""+ subClassification2 +"\"}}";
  }
  
	@Override
	public String fieldedExpression(String field, String expression) {
		return "{\"FIELDED_EXPRESSION\":{\"field\":\"" + field + "\", \"exp\":" + expression + "}}";
	}

  @Override
  public String wrap(String peek) {
    return "{\"WRAP\":{" + peek + "}";
  }

  @Override
	public String defaultOp(String a, String b) {
		return "{\"DEFAULT_OP\":[" + a + "," + b + "]}";
	}

  @Override
  public String fieldedSubExpressions(String field, String expression) {
	return "{\"FIELDED_EXPRESSION\":{\"field\":\"" + field + "\", \"exp\":" + expression + "}}";
  }
}
