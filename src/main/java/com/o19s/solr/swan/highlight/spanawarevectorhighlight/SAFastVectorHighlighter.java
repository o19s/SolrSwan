/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.o19s.solr.swan.highlight.spanawarevectorhighlight;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Encoder;
//import org.apache.lucene.search.vectorhighlight.SAScoreOrderFragmentsBuilder;
//import org.apache.lucene.search.vectorhighlight.SAFragmentsBuilder;
//import com.o19s.solr.swan.highlight.vectorhighlight.SpanAwareSimpleFragListBuilder;
//import com.o19s.solr.swan.highlight.vectorhighlight.SpanAwareFieldFragList;
//import com.o19s.solr.swan.highlight.vectorhighlight.SpanAwareFieldPhraseList;

/**
 * Another highlighter implementation.
 *
 */
public class SAFastVectorHighlighter {
  public static final boolean DEFAULT_PHRASE_HIGHLIGHT = true;
  public static final boolean DEFAULT_FIELD_MATCH = true;
  protected final boolean phraseHighlight;
  protected final boolean fieldMatch;
  private final SAFragListBuilder fragListBuilder;
  private final SAFragmentsBuilder fragmentsBuilder;
  private int phraseLimit = Integer.MAX_VALUE;

  /**
   * the default constructor.
   */
  public SAFastVectorHighlighter(){
    this( DEFAULT_PHRASE_HIGHLIGHT, DEFAULT_FIELD_MATCH );
  }

  /**
   * a constructor. Using {@link SASimpleFragListBuilder} and {@link SAScoreOrderFragmentsBuilder}.
   * 
   * @param phraseHighlight true or false for phrase highlighting
   * @param fieldMatch true of false for field matching
   */
  public SAFastVectorHighlighter(boolean phraseHighlight, boolean fieldMatch ){
    this( phraseHighlight, fieldMatch, new SASimpleFragListBuilder(), new SAScoreOrderFragmentsBuilder() );
  }

  /**
   * a constructor. A {@link SAFragListBuilder} and a {@link SAFragmentsBuilder} can be specified (plugins).
   * 
   * @param phraseHighlight true of false for phrase highlighting
   * @param fieldMatch true of false for field matching
   * @param fragListBuilder an instance of {@link SAFragListBuilder}
   * @param fragmentsBuilder an instance of {@link SAFragmentsBuilder}
   */
  public SAFastVectorHighlighter(boolean phraseHighlight, boolean fieldMatch,
                                 SAFragListBuilder fragListBuilder, SAFragmentsBuilder fragmentsBuilder ){
    this.phraseHighlight = phraseHighlight;
    this.fieldMatch = fieldMatch;
    this.fragListBuilder = fragListBuilder;
    this.fragmentsBuilder = fragmentsBuilder;
  }

  /**
   * create a {@link SAFieldQuery} object.
   * 
   * @param query a query
   * @return the created {@link SAFieldQuery} object
   */
  public SAFieldQuery getFieldQuery(Query query ) {
    // TODO: should we deprecate this? 
    // because if there is no reader, then we cannot rewrite MTQ.
    try {
      return getFieldQuery(query, null);
    } catch (IOException e) {
      // should never be thrown when reader is null
      throw new RuntimeException (e);
    }
  }
  
  /**
   * create a {@link SAFieldQuery} object.
   * 
   * @param query a query
   * @return the created {@link SAFieldQuery} object
   */
  public SAFieldQuery getFieldQuery(Query query, IndexReader reader ) throws IOException {
    return new SAFieldQuery( query, reader, phraseHighlight, fieldMatch );
  }

  /**
   * return the best fragment.
   * 
   * @param spanAwareFieldQuery {@link SAFieldQuery} object
   * @param reader {@link IndexReader} of the index
   * @param docId document id to be highlighted
   * @param fieldName field of the document to be highlighted
   * @param fragCharSize the length (number of chars) of a fragment
   * @return the best fragment (snippet) string
   * @throws IOException If there is a low-level I/O error
   */
  public final String getBestFragment(final SAFieldQuery spanAwareFieldQuery, IndexReader reader, int docId,
                                      String fieldName, int fragCharSize ) throws IOException {
    SAFieldFragList fieldFragList =
      getFieldFragList( fragListBuilder, spanAwareFieldQuery, reader, docId, fieldName, fragCharSize );
    return fragmentsBuilder.createFragment( reader, docId, fieldName, fieldFragList );
  }

  /**
   * return the best fragments.
   * 
   * @param spanAwareFieldQuery {@link SAFieldQuery} object
   * @param reader {@link IndexReader} of the index
   * @param docId document id to be highlighted
   * @param fieldName field of the document to be highlighted
   * @param fragCharSize the length (number of chars) of a fragment
   * @param maxNumFragments maximum number of fragments
   * @return created fragments or null when no fragments created.
   *         size of the array can be less than maxNumFragments
   * @throws IOException If there is a low-level I/O error
   */
  public final String[] getBestFragments(final SAFieldQuery spanAwareFieldQuery, IndexReader reader, int docId,
                                         String fieldName, int fragCharSize, int maxNumFragments ) throws IOException {
    SAFieldFragList fieldFragList =
      getFieldFragList( fragListBuilder, spanAwareFieldQuery, reader, docId, fieldName, fragCharSize );
    return fragmentsBuilder.createFragments( reader, docId, fieldName, fieldFragList, maxNumFragments );
  }

  /**
   * return the best fragment.
   * 
   * @param spanAwareFieldQuery {@link SAFieldQuery} object
   * @param reader {@link IndexReader} of the index
   * @param docId document id to be highlighted
   * @param fieldName field of the document to be highlighted
   * @param fragCharSize the length (number of chars) of a fragment
   * @param fragListBuilder {@link SAFragListBuilder} object
   * @param fragmentsBuilder {@link SAFragmentsBuilder} object
   * @param preTags pre-tags to be used to highlight terms
   * @param postTags post-tags to be used to highlight terms
   * @param encoder an encoder that generates encoded text
   * @return the best fragment (snippet) string
   * @throws IOException If there is a low-level I/O error
   */
  public final String getBestFragment(final SAFieldQuery spanAwareFieldQuery, IndexReader reader, int docId,
                                      String fieldName, int fragCharSize,
                                      SAFragListBuilder fragListBuilder, SAFragmentsBuilder fragmentsBuilder,
                                      String[] preTags, String[] postTags, Encoder encoder ) throws IOException {
    SAFieldFragList fieldFragList =
            getFieldFragList( fragListBuilder, spanAwareFieldQuery, reader, docId, fieldName, fragCharSize );
    return fragmentsBuilder.createFragment( reader, docId, fieldName, fieldFragList, preTags, postTags, encoder );
  }

  /**
   * return the best fragments.
   * 
   * @param spanAwareFieldQuery {@link SAFieldQuery} object
   * @param reader {@link IndexReader} of the index
   * @param docId document id to be highlighted
   * @param fieldName field of the document to be highlighted
   * @param fragCharSize the length (number of chars) of a fragment
   * @param maxNumFragments maximum number of fragments
   * @param fragListBuilder {@link SAFragListBuilder} object
   * @param fragmentsBuilder {@link SAFragmentsBuilder} object
   * @param preTags pre-tags to be used to highlight terms
   * @param postTags post-tags to be used to highlight terms
   * @param encoder an encoder that generates encoded text
   * @return created fragments or null when no fragments created.
   *         size of the array can be less than maxNumFragments
   * @throws IOException If there is a low-level I/O error
   */
  public final String[] getBestFragments(final SAFieldQuery spanAwareFieldQuery, IndexReader reader, int docId,
                                         String fieldName, int fragCharSize, int maxNumFragments,
                                         SAFragListBuilder fragListBuilder, SAFragmentsBuilder fragmentsBuilder,
                                         String[] preTags, String[] postTags, Encoder encoder ) throws IOException {
    SAFieldFragList fieldFragList =
      getFieldFragList( fragListBuilder, spanAwareFieldQuery, reader, docId, fieldName, fragCharSize );
    return fragmentsBuilder.createFragments( reader, docId, fieldName, fieldFragList, maxNumFragments,
        preTags, postTags, encoder );
  }

  /**
   * Return the best fragments.  Matches are scanned from matchedFields and turned into fragments against
   * storedField.  The highlighting may not make sense if matchedFields has matches with offsets that don't
   * correspond features in storedField.  It will outright throw a {@code StringIndexOutOfBoundsException}
   * if matchedFields produces offsets outside of storedField.  As such it is advisable that all
   * matchedFields share the same source as storedField or are at least a prefix of it.
   * 
   * @param spanAwareFieldQuery {@link SAFieldQuery} object
   * @param reader {@link IndexReader} of the index
   * @param docId document id to be highlighted
   * @param storedField field of the document that stores the text
   * @param matchedFields fields of the document to scan for matches
   * @param fragCharSize the length (number of chars) of a fragment
   * @param maxNumFragments maximum number of fragments
   * @param fragListBuilder {@link SAFragListBuilder} object
   * @param fragmentsBuilder {@link SAFragmentsBuilder} object
   * @param preTags pre-tags to be used to highlight terms
   * @param postTags post-tags to be used to highlight terms
   * @param encoder an encoder that generates encoded text
   * @return created fragments or null when no fragments created.
   *         size of the array can be less than maxNumFragments
   * @throws IOException If there is a low-level I/O error
   */
  public final String[] getBestFragments(final SAFieldQuery spanAwareFieldQuery, IndexReader reader, int docId,
                                         String storedField, Set< String > matchedFields, int fragCharSize, int maxNumFragments,
                                         SAFragListBuilder fragListBuilder, SAFragmentsBuilder fragmentsBuilder,
                                         String[] preTags, String[] postTags, Encoder encoder ) throws IOException {
    SAFieldFragList fieldFragList =
      getFieldFragList( fragListBuilder, spanAwareFieldQuery, reader, docId, matchedFields, fragCharSize );
    return fragmentsBuilder.createFragments( reader, docId, storedField, fieldFragList, maxNumFragments,
        preTags, postTags, encoder );
  }

  /**
   * Build a FieldFragList for one field.
   */
  private SAFieldFragList getFieldFragList(SAFragListBuilder fragListBuilder,
                                           final SAFieldQuery fieldQuery, IndexReader reader, int docId,
                                           String matchedField, int fragCharSize ) throws IOException {
    SAFieldTermStack fieldTermStack = new SAFieldTermStack( reader, docId, matchedField, fieldQuery);
    SAFieldPhraseList fieldPhraseList = new SAFieldPhraseList(fieldTermStack, fieldQuery, phraseLimit );
    return fragListBuilder.createFieldFragList( fieldPhraseList, fragCharSize );
    //return null;
  }

  /**
   * Build a FieldFragList for more than one field.
   */
  private SAFieldFragList getFieldFragList(SAFragListBuilder fragListBuilder,
                                           final SAFieldQuery fieldQuery, IndexReader reader, int docId,
                                           Set< String > matchedFields, int fragCharSize ) throws IOException {
    Iterator< String > matchedFieldsItr = matchedFields.iterator();
    if ( !matchedFieldsItr.hasNext() ) {
      throw new IllegalArgumentException( "matchedFields must contain at least on field name." );
    }
    SAFieldPhraseList[] toMerge = new SAFieldPhraseList[ matchedFields.size() ];
    int i = 0;
    while ( matchedFieldsItr.hasNext() ) {
      SAFieldTermStack stack = new SAFieldTermStack( reader, docId, matchedFieldsItr.next(), fieldQuery);
      toMerge[ i++ ] = new SAFieldPhraseList( stack, fieldQuery, phraseLimit );
    } 
    return fragListBuilder.createFieldFragList( new SAFieldPhraseList( toMerge ), fragCharSize );
    //return null;
  }

  /**
   * return whether phraseHighlight or not.
   * 
   * @return whether phraseHighlight or not
   */
  public boolean isPhraseHighlight(){ return phraseHighlight; }

  /**
   * return whether fieldMatch or not.
   * 
   * @return whether fieldMatch or not
   */
  public boolean isFieldMatch(){ return fieldMatch; }
  
  /**
   * @return the maximum number of phrases to analyze when searching for the highest-scoring phrase.
   */
  public int getPhraseLimit () { return phraseLimit; }
  
  /**
   * set the maximum number of phrases to analyze when searching for the highest-scoring phrase.
   * The default is unlimited (Integer.MAX_VALUE).
   */
  public void setPhraseLimit (int phraseLimit) { this.phraseLimit = phraseLimit; }
}
