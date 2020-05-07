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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.SolrParams;

import com.google.common.collect.ListMultimap;
import com.o19s.solr.swan.nodes.SwanAdjNode;
import com.o19s.solr.swan.nodes.SwanAndOperationNode;
import com.o19s.solr.swan.nodes.SwanNearNode;
import com.o19s.solr.swan.nodes.SwanNode;
import com.o19s.solr.swan.nodes.SwanNotNode;
import com.o19s.solr.swan.nodes.SwanOperatorNode;
import com.o19s.solr.swan.nodes.SwanOrOperationNode;
import com.o19s.solr.swan.nodes.SwanPhraseNode;
import com.o19s.solr.swan.nodes.SwanRangeNode;
import com.o19s.solr.swan.nodes.SwanSameNode;
import com.o19s.solr.swan.nodes.SwanTermNode;
import com.o19s.solr.swan.nodes.SwanWithNode;
import com.o19s.solr.swan.nodes.SwanXOrOperationNode;

public class LuceneSwanSearcher implements ISwanSearcher<SwanNode> {

  private static String defaultOp = "adj";
  private static String stemSuffix = "_stem";
  private ListMultimap<String, String> _fieldAliases;

  public LuceneSwanSearcher(SolrParams params, ListMultimap<String, String> fieldAliases) {
	_fieldAliases = fieldAliases;
    SwanNode.setParams(params);
    String defOpStr = params.get("q.op");
    if(defOpStr != null) {
      defaultOp = defOpStr.toLowerCase();
    }
  }

  @Override
  public SwanNode or(SwanNode left, SwanNode right) {
    // This clause says if left is an OR condition, add right as an additional
    // OR in the same scope.
    // example if left = (X OR Y), and right = (Z), we will return (X OR Y OR Z)
    // If left or right have different fields, they should fall out of this and 
    // result in (X OR Y) OR (Z) so the different fields can be used
    if (left instanceof SwanOrOperationNode
        && nodesAreUnfieldedOrHaveSameFields(left.getField(), right.getField())){
      ((SwanOrOperationNode) left).add(right);
      return left;
    }
    return new SwanOrOperationNode(left,right);
  }

  @Override
  public SwanNode xor(SwanNode left, SwanNode right) {
    if (left instanceof SwanXOrOperationNode && !left.isWrapped()) {
      ((SwanXOrOperationNode) left).add(right);
      return left;
    }
    return new SwanXOrOperationNode(left,right);
  }

  @Override
  public SwanNode and(SwanNode left, SwanNode right) {
    if (left instanceof SwanAndOperationNode) {
      ((SwanAndOperationNode) left).add(right);
      return left;
    }
    return new SwanAndOperationNode(left, right);
  }

  @Override
  public SwanNode same(SwanNode left, SwanNode right, int n) {
    return new SwanSameNode(left,right,n);
  }

  @Override
  public SwanNode with(SwanNode left, SwanNode right, int n) {
    return new SwanWithNode(left,right,n);
  }

  @Override
  public SwanNode near(SwanNode left, SwanNode right, int n) {
    return new SwanNearNode(left, right, n);
  }

  @Override
  public SwanNode adj(SwanNode left, SwanNode right, int n) {
    return new SwanAdjNode(left, right, n);
  }

  @Override
  public SwanNode defaultOp(SwanNode left, SwanNode right) {
    Class[] unamenableToSwan = new Class[] { SwanRangeNode.class };
    boolean swanSafe = true;
    for(Class c : unamenableToSwan) {
      if((c == left.getClass()) || (c == right.getClass())) {
        swanSafe = false;
        break;
      }
    }
    // TODO: Figure out what this is for
//		System.out.println("In LSS.dO, "+ left.getClass() +" --- "+ right.getClass() );
    if(defaultOp.equals("same") && swanSafe) {
      return same(left, right, 1);
    } else if(defaultOp.equals("with") && swanSafe) {
      return with(left, right, 1);
    } else if(defaultOp.equals("adj") && swanSafe) {
      return adj(left, right, 1);
    } else if(defaultOp.equals("near") && swanSafe) {
      return near(left, right, 1);
    } else if(defaultOp.equals("and") || !swanSafe) { //<-- defaults to AND if not swanSafe
      return and(left, right);
    } else if(defaultOp.equals("or")) {
      return or(left, right);
    } else {
      throw new UnsupportedOperationException("Default operator may only be on of SAME, WITH, ADJ, NEAR, AND, OR. Was suplied with "+ defaultOp);
    }
  }

  @Override
  public SwanNode not(SwanNode left, SwanNode right) {
    return new SwanNotNode(left, right);
  }

  @Override
  public SwanNode term(String match) {
    return new SwanTermNode(match);
  }

  @Override
  public SwanNode phrase(String match) {
    return new SwanPhraseNode(match);
  }

  @Override
  public SwanNode range(String field, String op1, String val1) {
      if (_fieldAliases.containsKey(field.toLowerCase())) {
          return new SwanRangeNode(_fieldAliases.get(field.toLowerCase()).get(0), op1, val1);
      }
      return new SwanRangeNode(field, op1, val1);
  }

  @Override
	public SwanNode classRange(String field, String mainClassification, String subClassification1, String subClassification2) {
		  return new SwanRangeNode(field, ">=", mainClassification + "/" + subClassification1, "<=", mainClassification + "/" + subClassification2);		  
	}

  @Override
  public SwanNode boundRange(String field, String op1, String val1, String op2, String val2) {
    if (_fieldAliases.containsKey(field.toLowerCase())) {
        return new SwanRangeNode(_fieldAliases.get(field.toLowerCase()).get(0), op1, val1, op2, val2);
    }
    return new SwanRangeNode(field, op1, val1, op2, val2);
  }

  @Override
  public SwanNode fieldedSubExpressions(String field, SwanNode expression){
	  if (expression instanceof SwanOperatorNode) {
	    for(SwanNode node : ((SwanOperatorNode)expression).getNodes()){
		  fieldedSubExpressions(field, node);
	    }
      } else {
		 fieldedExpression(field, expression);
      }
	  return expression;
  }
  
  @Override
  public SwanNode fieldedExpression(String field, SwanNode expression) {
	field = field.toLowerCase();
      if(SwanNode.isFieldStemming()) {
          field = field.concat(stemSuffix);
      }

	if (_fieldAliases.containsKey(field)) {
		try{
	      return getFieldAliasExpression(_fieldAliases.get(field), expression);
		}
		catch (Exception ex){
					//need to do something here.  Log? pass the exception on?
		}
	}
    expression.setField(field);
    return expression;
  }
  
  private SwanNode getFieldAliasExpression(List<String> fields, SwanNode originalExpression) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
	  if (fields.size() == 1){
		  originalExpression.setField(fields.get(0));
		  return originalExpression;
	  }
	  //set up 'or' conditions between all of the fields if there are more than one
	  SwanNode aggregatedExpression = null;
	  for (String field : fields) {
		  if (field !=  null){
			  //use copy constructor to make a copy of the original expression
			  SwanNode newExpression = originalExpression.getClass().getConstructor(originalExpression.getClass()).newInstance(originalExpression);
			  newExpression.setField(field);
			  if (aggregatedExpression != null){
				  aggregatedExpression = or(aggregatedExpression, newExpression);
			  }
			  else{
				  aggregatedExpression = newExpression;
			  }
		  }
	  }	  
	  return aggregatedExpression;
  }
  
  private boolean nodesAreUnfieldedOrHaveSameFields(String firstField, String secondField){
      return ((StringUtils.isBlank(firstField) || StringUtils.isBlank(secondField)) 
               || (StringUtils.equals(firstField, secondField)));
     }

  @Override
  public SwanNode wrap(SwanNode peek) {
    peek.setWrapped();
    return peek;
  }
}
