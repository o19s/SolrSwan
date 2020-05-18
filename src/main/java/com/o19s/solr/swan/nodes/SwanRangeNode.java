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

import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.schema.DatePointField;
import org.apache.solr.schema.TrieDateField;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SwanRangeNode extends SwanNode {

  private Operation operation1 = null;
  private String value1;
  private Operation operation2 = null;
  private String value2;
  private static Map<String, String> _fieldTypes = new HashMap();
  private String dateTemplateLower = "%d-%02d-%dT00:00:00Z";
  private String dateTemplateUpper = "%d-%02d-%dT23:59:59Z";

  private enum Operation { LESS_THAN, LESS_THAN_EQUAL, GREATER_THAN, GREATER_THAN_EQUAL, EQUAL, NOT_EQUAL }

  public SwanRangeNode(String field, String operation1, String value1) {
    _field = field;
    if(operation1.equals("<")) {
      this.operation1 = Operation.LESS_THAN;
    } else if(operation1.equals("<=")) {
      this.operation1 = Operation.LESS_THAN_EQUAL;
    } else if(operation1.equals(">")) {
      this.operation1 = Operation.GREATER_THAN;
    } else if(operation1.equals(">=")) {
      this.operation1 = Operation.GREATER_THAN_EQUAL;
    } else if(operation1.equals("<>")) {
      this.operation1 = Operation.NOT_EQUAL;
    } else if(operation1.equals("=") || operation1.equals("==")) {
      this.operation1 = Operation.EQUAL;
    }
    this.value1 = value1;
  }

  public SwanRangeNode(String field, String operation1, String value1, String operation2, String value2) {
    _field = field;
    if(operation1.equals("<")) {
      this.operation1 = Operation.LESS_THAN;
    } else if(operation1.equals("<=")) {
      this.operation1 = Operation.LESS_THAN_EQUAL;
    } else if(operation1.equals(">")) {
      this.operation1 = Operation.GREATER_THAN;
    } else if(operation1.equals(">=")) {
      this.operation1 = Operation.GREATER_THAN_EQUAL;
    }
    this.value1 = value1;

    if(operation2.equals("<")) {
      this.operation2 = Operation.LESS_THAN;
    } else if(operation2.equals("<=")) {
      this.operation2 = Operation.LESS_THAN_EQUAL;
    } else if(operation2.equals(">")) {
      this.operation2 = Operation.GREATER_THAN;
    } else if(operation2.equals(">=")) {
      this.operation2 = Operation.GREATER_THAN_EQUAL;
    }
    this.value2 = value2;
  }

  // this is a copy constructor
  public SwanRangeNode(SwanRangeNode originalNode){
    this._field = originalNode.getField();
    this.operation1 = originalNode.operation1;
    this.value1 = originalNode.value1;
    this.operation2 = originalNode.operation2;
    this.value2 = originalNode.value2;
  }

  @Override
  public Query getQuery(String field) {// the field value1 is always overridden
    SwanRangeBounds bounds = new SwanRangeBounds();

    Query query = setSwanRangeBounds(bounds, operation1, value1);
    if (query != null)
      return query;

    query = setSwanRangeBounds(bounds, operation2, value2);
    if (query != null)
      return query;

    String type = getType(field);
//    System.out.println("Type: " + type);
    if (type.equals("IntPointField") || type.equals("TrieIntField")) {
      //return NumericRangeQuery.newIntRange(_field, bounds.getIntLower(), bounds.getIntUpper(), bounds.inc_lower, bounds.inc_upper);
      int lower = (bounds.inc_lower) ? bounds.getIntLower() : Math.addExact(bounds.getIntLower(), 1);
      int upper = (bounds.inc_upper) ? bounds.getIntUpper() : Math.addExact(bounds.getIntUpper(), -1);
      return IntPoint.newRangeQuery(_field, lower, upper);
    } else if (type.equals("LongPointField") || type.equals("TrieLongField")) {
      //return NumericRangeQuery.newLongRange(_field, bounds.getLongLower(), bounds.getLongUpper(), bounds.inc_lower, bounds.inc_upper);
      long lower = (bounds.inc_lower) ? bounds.getLongLower() : Math.addExact(bounds.getLongLower(), 1);
      long upper = (bounds.inc_upper) ? bounds.getLongUpper() : Math.addExact(bounds.getLongUpper(), -1);
      return LongPoint.newRangeQuery(_field, lower, upper);
    } else if (type.equals("DoublePointField") || type.equals("TrieDoubleField")) {
      //return NumericRangeQuery.newDoubleRange(_field, bounds.getDoubleLower(), bounds.getDoubleUpper(), bounds.inc_lower, bounds.inc_upper);
      double lower = (bounds.inc_lower) ? bounds.getDoubleLower() : Math.nextUp(bounds.getDoubleLower());
      double upper = (bounds.inc_upper) ? bounds.getDoubleUpper() : Math.nextDown(bounds.getDoubleUpper());
      return DoublePoint.newRangeQuery(_field, lower, upper);
    } else if (type.equals("FloatPointField") || type.equals("TrieFloatField")) {
      //return NumericRangeQuery.newFloatRange(_field, bounds.getFloatLower(), bounds.getFloatUpper(), bounds.inc_lower, bounds.inc_upper);
      float lower = (bounds.inc_lower) ? bounds.getFloatLower() : Math.nextUp(bounds.getFloatLower());
      float upper = (bounds.inc_upper) ? bounds.getFloatUpper() : Math.nextDown(bounds.getFloatUpper());
      return FloatPoint.newRangeQuery(_field, lower, upper);
    } else if (type.equals("DatePointField")) {
//      System.out.println("field: " + schema.getField(field).getType());
      DatePointField dateField = (DatePointField) schema.getField(field).getType();
      //TrieDateField dateField = (TrieDateField) schema.getField(field).getType();
      return dateField.getRangeQuery(_parser, schema.getField(field), bounds.getDateLower().toString(), bounds.getDateUpper().toString(), bounds.inc_lower, bounds.inc_upper);
    } else if(type.equals("TrieDateField")) {
//      System.out.println("field: " + schema.getField(field).getType());
      DatePointField dateField = (DatePointField) schema.getField(field).getType();
      //TrieDateField dateField = (TrieDateField) schema.getField(field).getType();
      return dateField.getRangeQuery(_parser, schema.getField(field), bounds.getDateLower().toString(), bounds.getDateUpper().toString(), bounds.inc_lower, bounds.inc_upper);
    }
    else
      return new TermRangeQuery(_field, bounds.ref_lower, bounds.ref_upper, bounds.inc_lower, bounds.inc_upper);

  }

  private Query setSwanRangeBounds(SwanRangeBounds bounds, Operation operation, String value) {
    if (value == null || operation == null)
      return null;

    switch (operation) {
      case LESS_THAN: // @key < value
        bounds.inc_upper = false;
        bounds.ref_upper = new BytesRef(value);
        break;
      case LESS_THAN_EQUAL: // @key <= value
        bounds.inc_upper = true;
        bounds.ref_upper = new BytesRef(value);
        break;
      case GREATER_THAN:
        bounds.inc_lower = false;
        bounds.ref_lower = new BytesRef(value);
        break;
      case GREATER_THAN_EQUAL:
        bounds.inc_lower = true;
        bounds.ref_lower = new BytesRef(value);
        break;
      case EQUAL:
        SwanTermNode termNode = new SwanTermNode(value);
        termNode.setField(_field);
        termNode.setSchema(schema);
        return termNode.getQuery();
      case NOT_EQUAL:
        SwanOrOperationNode node = new SwanOrOperationNode(new SwanRangeNode(_field, "<", value), new SwanRangeNode(_field, ">", value));
        node.setSchema(schema);
        return node.getQuery();

      default:
        break;
    }

    return null;
  }

  @Override
  public SpanQuery getSpanQuery(String field) {
    throw new UnsupportedOperationException("Range Queries within a Span is not yet supported.");
  }

  @Override
  public String toString() {
    if (value2 != null)
      return "RANGE(" + _field + "," + operation1 + "," + value1 + "," + operation2 + "," + value2 + ")";
    return "RANGE(" + _field + "," + operation1 + "," + value1 + ")";
  }

  protected class SwanRangeBounds {
    boolean inc_lower = true;
    boolean inc_upper = true;
    BytesRef ref_lower = null;
    BytesRef ref_upper = null;

    public Integer getIntLower() {
      if (ref_lower == null) return Integer.MIN_VALUE;
      return Integer.parseInt(ref_lower.utf8ToString());
    }

    public Integer getIntUpper() {
      if (ref_upper == null) return Integer.MAX_VALUE;
      return Integer.parseInt(ref_upper.utf8ToString());
    }

    public Long getLongLower() {
      if (ref_lower == null) return Long.MIN_VALUE;
      return Long.parseLong(ref_lower.utf8ToString());
    }

    public Long getLongUpper() {
      if (ref_upper == null) return Long.MAX_VALUE;
      return Long.parseLong(ref_upper.utf8ToString());
    }

    public Double getDoubleLower() {
      if (ref_lower == null) return Double.MIN_VALUE;
      return Double.parseDouble(ref_lower.utf8ToString());
    }

    public Double getDoubleUpper() {
      if (ref_upper == null) return Double.MAX_VALUE;
      return Double.parseDouble(ref_upper.utf8ToString());
    }

    public Float getFloatLower() {
      if (ref_lower == null) return Float.MIN_VALUE;
      return Float.parseFloat(ref_lower.utf8ToString());
    }

    public Float getFloatUpper() {
      if (ref_upper == null) return Float.MAX_VALUE;
      return Float.parseFloat(ref_upper.utf8ToString());
    }

    public Date getDateLower() {
      Calendar cal = Calendar.getInstance();
      cal.set(1800, 1, 1, 0, 0, 0);
      if (ref_lower == null || ref_lower.length < 4) return cal.getTime();
      String ref = ref_lower.utf8ToString();
      int year = ref.length() == 4 ? Integer.parseInt(ref) : Integer.parseInt(ref.substring(0, 4));
      int month = ref.length() < 6 ? 01 : Integer.parseInt(ref.substring(4,6));
      int day = ref.length() < 8 ? 01 : Integer.parseInt(ref.substring(6,8));
      cal.set(year, month - 1, day);
      return cal.getTime();
    }

    public Date getDateUpper() {
      Calendar cal = Calendar.getInstance();
      cal.set(2100, 1, 1, 23, 59, 59);
      if (ref_upper == null || ref_upper.length < 4) return cal.getTime();
      String ref = ref_upper.utf8ToString();
      int year = ref.length() == 4 ? Integer.parseInt(ref) : Integer.parseInt(ref.substring(0, 4));
      int month = ref.length() < 6 ? 12 : Integer.parseInt(ref.substring(4,6));
      int day = ref.length() < 8 ? 31 : Integer.parseInt(ref.substring(6,8));
      cal.set(year, month - 1, day);
      return cal.getTime();
    }
  }

  private String getType(String field) {
    if (!_fieldTypes.containsKey(field)) {
      String[] type = schema.getField(field).getType().getClass().getName().split("\\.");
      _fieldTypes.put(field, type[type.length -1]);
    }

    return _fieldTypes.get(field);
  }
}
