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
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParser;

public abstract class SwanNode {

  protected static String sentenceMarker;
  protected static String paragraphMarker;
  protected static String[] defaultFields;
  protected static Boolean fieldStemming;

  protected String _field;
  protected IndexSchema schema;
  protected QParser _parser;

  public boolean isWrapped() {
    return wrapped;
  }

  public void setWrapped() {
    this.wrapped = true;
  }

  protected boolean wrapped;

  public String getField() {
    return _field;
  }

  public void setField(String field) {
    if (_field != null)
      throw new IllegalArgumentException("Field mismatch error, Unable to execute query.");
    _field = field;
  }

  public static void setParams(SolrParams params) {
    sentenceMarker = params.get("sm");
    paragraphMarker = params.get("pm");
    fieldStemming  = params.get("fieldStemming") != null && params.get("fieldStemming").toLowerCase().equals("true");

      if (params.get("qf") == null) {
      throw new RuntimeException("Query fields \"qf\" must be specified");
    }

    defaultFields = params.get("qf").split("[\\s,]+", -1);

    if(sentenceMarker == null) throw new RuntimeException("Sentence marker \"sm\" must be specified");
    if(paragraphMarker == null) throw new RuntimeException("Paragraph marker \"pm\" must be specified");
      //todo: must this be a required param?
  }

  public static String getSentenceMarker() {
    return sentenceMarker;
  }

  public static String getParagraphMarker() {
    return paragraphMarker;
  }

  public static Boolean isFieldStemming() {
    return fieldStemming;
  }

  public Query getQuery() {
    if (isFielded())
      return getQuery(getField());

    return getQuery(defaultFields);
  }

  public boolean isFielded() {
    return getField() != null;
  }

  public abstract Query getQuery(String field);
  public abstract SpanQuery getSpanQuery(String field);

  public Query getQuery(String[] fields) {
    BooleanQuery query = new BooleanQuery();
    for (int x = 0; x < fields.length; x++) {
      query.add(getQuery(fields[x]), BooleanClause.Occur.SHOULD);
    }
    return query;
  }

  @Override
  public abstract String toString();

  public void setSchema(IndexSchema schema) {
    this.schema = schema;
  }

  public void setParser(QParser _parser) {
    this._parser = _parser;
  }
}
