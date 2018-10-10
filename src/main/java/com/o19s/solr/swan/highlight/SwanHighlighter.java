package com.o19s.solr.swan.highlight;

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

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.OffsetLimitTokenFilter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.util.AttributeSource.State;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.highlight.DefaultEncoder;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.solr.highlight.GapFragmenter;
import org.apache.solr.highlight.HtmlFormatter;
import org.apache.solr.highlight.ScoreOrderFragmentsBuilder;
import org.apache.solr.highlight.SimpleBoundaryScanner;
import org.apache.solr.highlight.SimpleFragListBuilder;
import org.apache.solr.highlight.SolrBoundaryScanner;
import org.apache.solr.highlight.SolrEncoder;
import org.apache.solr.highlight.SolrFormatter;
import org.apache.solr.highlight.SolrFragListBuilder;
import org.apache.solr.highlight.SolrFragmenter;
import org.apache.solr.highlight.SolrFragmentsBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwanHighlighter extends DefaultSolrHighlighter {
  public static Logger log = LoggerFactory.getLogger(SwanHighlighter.class);

  private SolrCore solrCore;

  // Need to override the DefaultSolrHighlighter because its solrCore is private
  public SwanHighlighter(SolrCore solrCore) {
    this.solrCore = solrCore;
  }


  // Need to override the DefaultSolrHighlighter because its solrCore is private
  public void init(PluginInfo info) {
    formatters.clear();
    encoders.clear();
    fragmenters.clear();
    fragListBuilders.clear();
    fragmentsBuilders.clear();
    boundaryScanners.clear();

    // Load the fragmenters
    SolrFragmenter frag = solrCore.initPlugins(info.getChildren("fragmenter") , fragmenters,SolrFragmenter.class,null);
    if (frag == null) frag = new GapFragmenter();
    fragmenters.put("", frag);
    fragmenters.put(null, frag);

    // Load the formatters
    SolrFormatter fmt = solrCore.initPlugins(info.getChildren("formatter"), formatters,SolrFormatter.class,null);
    if (fmt == null) fmt = new HtmlFormatter();
    formatters.put("", fmt);
    formatters.put(null, fmt);

    // Load the encoders
    SolrEncoder enc = solrCore.initPlugins(info.getChildren("encoder"), encoders,SolrEncoder.class,null);
    if (enc == null) enc = new DefaultEncoder();
    encoders.put("", enc);
    encoders.put(null, enc);

    // Load the FragListBuilders
    SolrFragListBuilder fragListBuilder = solrCore.initPlugins(info.getChildren("fragListBuilder"),
      fragListBuilders, SolrFragListBuilder.class, null );
    if( fragListBuilder == null ) fragListBuilder = new SimpleFragListBuilder();
    fragListBuilders.put( "", fragListBuilder );
    fragListBuilders.put( null, fragListBuilder );

    // Load the FragmentsBuilders
    SolrFragmentsBuilder fragsBuilder = solrCore.initPlugins(info.getChildren("fragmentsBuilder"),
      fragmentsBuilders, SolrFragmentsBuilder.class, null);
    if( fragsBuilder == null ) fragsBuilder = new ScoreOrderFragmentsBuilder();
    fragmentsBuilders.put( "", fragsBuilder );
    fragmentsBuilders.put( null, fragsBuilder );

    // Load the BoundaryScanners
    SolrBoundaryScanner boundaryScanner = solrCore.initPlugins(info.getChildren("boundaryScanner"),
      boundaryScanners, SolrBoundaryScanner.class, null);
    if(boundaryScanner == null) boundaryScanner = new SimpleBoundaryScanner();
    boundaryScanners.put("", boundaryScanner);
    boundaryScanners.put(null, boundaryScanner);
  }



  /**
   * Generates a list of Highlighted query fragments for each item in a list
   * of documents, or returns null if highlighting is disabled.
   *
   * @param docs query results
   * @param query the query
   * @param req the current request
   * @param defaultFields default list of fields to summarize
   *
   * @return NamedList containing a NamedList for each document, which in
   * turns contains sets (field, summary) pairs.
   */
  @Override
  @SuppressWarnings("unchecked")
  public NamedList<Object> doHighlighting(DocList docs, Query query, SolrQueryRequest req, String[] defaultFields) throws IOException {

    NamedList fragments = new SimpleOrderedMap();

    SolrParams params = req.getParams();
    if (!isHighlightingEnabled(params))
      return null;

    SolrIndexSearcher searcher = req.getSearcher();
    IndexSchema schema = searcher.getSchema();
    String[] fieldNames = getHighlightFields(query, req, defaultFields);
    Set<String> fset = new HashSet<String>();

    {
      // pre-fetch documents using the Searcher's doc cache
      Collections.addAll(fset, fieldNames);
      // fetch unique key if one exists.
      SchemaField keyField = schema.getUniqueKeyField();
      if(null != keyField)
        fset.add(keyField.getName());
    }

    //CHANGE start
//	    int[] docIds = new int[docs.swordize()];
    TreeSet<Integer> docIds = new TreeSet<Integer>();
    DocIterator iterator = docs.iterator();
    for (int i = 0; i < docs.size(); i++) {
      docIds.add(iterator.nextDoc());
    }
    // Get Frag list builder
    String fragListBuilderString = params.get(HighlightParams.FRAG_LIST_BUILDER).toLowerCase();
    FragListBuilder fragListBuilder;
    if (fragListBuilderString.equals("single")) {
      fragListBuilder = new SingleFragListBuilder();
    } else {
      fragListBuilder = new com.o19s.solr.swan.highlight.SimpleFragListBuilder();
    }

    // get FastVectorHighlighter instance out of the processing loop
    SpanAwareFastVectorHighlighter safvh = new SpanAwareFastVectorHighlighter(
      // FVH cannot process hl.usePhraseHighlighter parameter per-field basis
      params.getBool( HighlightParams.USE_PHRASE_HIGHLIGHTER, true ),
      // FVH cannot process hl.requireFieldMatch parameter per-field basis
      params.getBool( HighlightParams.FIELD_MATCH, false ),
      fragListBuilder,
      //new com.o19s.solr.swan.highlight.ScoreOrderFragmentsBuilder(),
      new WordHashFragmentsBuilder(),
      // List of docIds to filter spans
      docIds);
    safvh.setPhraseLimit(params.getInt(HighlightParams.PHRASE_LIMIT, Integer.MAX_VALUE));
    SpanAwareFieldQuery fieldQuery = safvh.getFieldQuery( query, searcher.getIndexReader(), docIds );

    // Highlight each document
    for (int docId : docIds) {
      Document doc = searcher.doc(docId, fset);
      NamedList docSummaries = new SimpleOrderedMap();
      for (String fieldName : fieldNames) {
        fieldName = fieldName.trim();
        if( useFastVectorHighlighter( params, schema, fieldName ) )
          doHighlightingByFastVectorHighlighter( safvh, fieldQuery, req, docSummaries, docId, doc, fieldName );
        else
          doHighlightingByHighlighter( query, req, docSummaries, docId, doc, fieldName );
      }
      String printId = schema.printableUniqueKey(doc);
      fragments.add(printId == null ? null : printId, docSummaries);
    }
    //CHANGE end
    return fragments;
  }


  /*
   * If fieldName is undefined, this method returns false, then
   * doHighlightingByHighlighter() will do nothing for the field.
   */
  private boolean useFastVectorHighlighter( SolrParams params, IndexSchema schema, String fieldName ){
    SchemaField schemaField = schema.getFieldOrNull( fieldName );
    if( schemaField == null ) return false;
    boolean useFvhParam = params.getFieldBool( fieldName, HighlightParams.USE_FVH, false );
    if( !useFvhParam ) return false;
    boolean termPosOff = schemaField.storeTermPositions() && schemaField.storeTermOffsets();
    if( !termPosOff ) {
      log.warn( "Solr will use Highlighter instead of FastVectorHighlighter because {} field does not store TermPositions and TermOffsets.", fieldName );
    }
    return termPosOff;
  }

  private void doHighlightingByHighlighter( Query query, SolrQueryRequest req, NamedList docSummaries,
                                            int docId, Document doc, String fieldName ) throws IOException {
    final SolrIndexSearcher searcher = req.getSearcher();
    final IndexSchema schema = searcher.getSchema();

    // TODO: Currently in trunk highlighting numeric fields is broken (Lucene) -
    // so we disable them until fixed (see LUCENE-3080)!
    // BEGIN: Hack
    final SchemaField schemaField = schema.getFieldOrNull(fieldName);
    if (schemaField != null && (
      (schemaField.getType() instanceof org.apache.solr.schema.TrieField) ||
        (schemaField.getType() instanceof org.apache.solr.schema.TrieDateField)
    )) return;
    // END: Hack

    SolrParams params = req.getParams();
    IndexableField[] docFields = doc.getFields(fieldName);
    List<String> listFields = new ArrayList<String>();
    for (IndexableField field : docFields) {
      listFields.add(field.stringValue());
    }

    String[] docTexts = listFields.toArray(new String[listFields.size()]);

    // according to Document javadoc, doc.getValues() never returns null. check empty instead of null
    if (docTexts.length == 0) return;

    TokenStream tokenStream;
    int numFragments = getMaxSnippets(fieldName, params);
    boolean mergeContiguousFragments = isMergeContiguousFragments(fieldName, params);

    List<TextFragment> frags = new ArrayList<TextFragment>();

    TermOffsetsTokenStream tots = null; // to be non-null iff we're using TermOffsets optimization
    try {
//      TokenStream tvStream = TokenSources.getTokenStream(searcher.getIndexReader(), docId, fieldName);
//      if (tvStream != null) {
//        tots = new TermOffsetsTokenStream(tvStream);
//      }
    }
    catch (IllegalArgumentException e) {
      // No problem. But we can't use TermOffsets optimization.
    }

    for (int j = 0; j < docTexts.length; j++) {
      if( tots != null ) {
        // if we're using TermOffsets optimization, then get the next
        // field value's TokenStream (i.e. get field j's TokenStream) from tots:
        tokenStream = tots.getMultiValuedTokenStream( docTexts[j].length() );
      } else {
        // fall back to analyzer
        tokenStream = createAnalyzerTStream(schema, fieldName, docTexts[j]);
      }

      int maxCharsToAnalyze = params.getFieldInt(fieldName,
        HighlightParams.MAX_CHARS,
        Highlighter.DEFAULT_MAX_CHARS_TO_ANALYZE);

      Highlighter highlighter;
      if (Boolean.valueOf(req.getParams().get(HighlightParams.USE_PHRASE_HIGHLIGHTER, "true"))) {
        if (maxCharsToAnalyze < 0) {
          tokenStream = new CachingTokenFilter(tokenStream);
        } else {
          tokenStream = new CachingTokenFilter(new OffsetLimitTokenFilter(tokenStream, maxCharsToAnalyze));
        }

        // get highlighter
        highlighter = getPhraseHighlighter(query, fieldName, req, (CachingTokenFilter) tokenStream);

        // after highlighter initialization, reset tstream since construction of highlighter already used it
        tokenStream.reset();
      }
      else {
        // use "the old way"
        highlighter = getHighlighter(query, fieldName, req);
      }

      if (maxCharsToAnalyze < 0) {
        highlighter.setMaxDocCharsToAnalyze(docTexts[j].length());
      } else {
        highlighter.setMaxDocCharsToAnalyze(maxCharsToAnalyze);
      }

      try {
        TextFragment[] bestTextFragments = highlighter.getBestTextFragments(tokenStream, docTexts[j], mergeContiguousFragments, numFragments);
        for (int k = 0; k < bestTextFragments.length; k++) {
          if ((bestTextFragments[k] != null) && (bestTextFragments[k].getScore() > 0)) {
            frags.add(bestTextFragments[k]);
          }
        }
      } catch (InvalidTokenOffsetsException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
      }
    }
    // sort such that the fragments with the highest score come first
    Collections.sort(frags, new Comparator<TextFragment>() {
      public int compare(TextFragment arg0, TextFragment arg1) {
        return Math.round(arg1.getScore() - arg0.getScore());
      }
    });

    // convert fragments back into text
    // TODO: we can include score and position information in output as snippet attributes
    String[] summaries = null;
    if (frags.size() > 0) {
      ArrayList<String> fragTexts = new ArrayList<String>();
      for (TextFragment fragment: frags) {
        if ((fragment != null) && (fragment.getScore() > 0)) {
          fragTexts.add(fragment.toString());
        }
        if (fragTexts.size() >= numFragments) break;
      }
      summaries = (String[]) fragTexts.toArray();
      if (summaries.length > 0)
        docSummaries.add(fieldName, summaries);
    }
    // no summeries made, copy text from alternate field
    if (summaries == null || summaries.length == 0) {
      alternateField( docSummaries, params, doc, fieldName );
    }
  }

  private SolrFragmentsBuilder getSolrFragmentsBuilder( String fieldName, SolrParams params ){
    String fb = params.getFieldParam( fieldName, HighlightParams.FRAGMENTS_BUILDER );
    SolrFragmentsBuilder solrFb = fragmentsBuilders.get( fb );
    if( solrFb == null ){
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Unknown fragmentsBuilder: " + fb );
    }
    return solrFb;
  }

  private void doHighlightingByFastVectorHighlighter( SpanAwareFastVectorHighlighter highlighter, SpanAwareFieldQuery fieldQuery,
                                                      SolrQueryRequest req, NamedList docSummaries, int docId, Document doc,
                                                      String fieldName ) throws IOException {
    SolrParams params = req.getParams();
    SolrFragmentsBuilder solrFb = getSolrFragmentsBuilder( fieldName, params );
    String[] snippets = highlighter.getBestFragments( fieldQuery, req.getSearcher().getIndexReader(), docId, fieldName,
      params.getFieldInt( fieldName, HighlightParams.FRAGSIZE, 100 ),
      params.getFieldInt( fieldName, HighlightParams.SNIPPETS, 1 ),
      getFragListBuilderOverride( fieldName, params ),
      getFragmentsBuilder( fieldName, params ),
      solrFb.getPreTags( params, fieldName ),
      solrFb.getPostTags( params, fieldName ),
      getEncoder( fieldName, params ) );
    if( snippets != null && snippets.length > 0 )
      docSummaries.add( fieldName, snippets );
    else
      alternateField( docSummaries, params, doc, fieldName );
  }

  private FragListBuilder getFragListBuilderOverride(String fieldName, SolrParams params) {

    FragListBuilder fragListBuilder;
      String fb = params.getFieldParam( fieldName, HighlightParams.FRAG_LIST_BUILDER ).toLowerCase();
      if (fb.equals("single")) {
          fragListBuilder = new SingleFragListBuilder();
      } else {
          fragListBuilder = new com.o19s.solr.swan.highlight.SimpleFragListBuilder();
      }
      return fragListBuilder;
  }


  private void alternateField( NamedList docSummaries, SolrParams params, Document doc, String fieldName ){
    String alternateField = params.getFieldParam(fieldName, HighlightParams.ALTERNATE_FIELD);
    if (alternateField != null && alternateField.length() > 0) {
      IndexableField[] docFields = doc.getFields(alternateField);
      List<String> listFields = new ArrayList<String>();
      for (IndexableField field : docFields) {
        if (field.binaryValue() == null)
          listFields.add(field.stringValue());
      }

      String[] altTexts = listFields.toArray(new String[listFields.size()]);

      if (altTexts != null && altTexts.length > 0){
        Encoder encoder = getEncoder(fieldName, params);
        int alternateFieldLen = params.getFieldInt(fieldName, HighlightParams.ALTERNATE_FIELD_LENGTH,0);
        List<String> altList = new ArrayList<String>();
        int len = 0;
        for( String altText: altTexts ){
          if( alternateFieldLen <= 0 ){
            altList.add(encoder.encodeText(altText));
          }
          else{
            altList.add( len + altText.length() > alternateFieldLen ?
              encoder.encodeText(altText.substring(0, alternateFieldLen - len)) :
              encoder.encodeText(altText) );
            len += altText.length();
            if( len >= alternateFieldLen ) break;
          }
        }
        docSummaries.add(fieldName, altList);
      }
    }
  }

  private TokenStream createAnalyzerTStream(IndexSchema schema, String fieldName, String docText) throws IOException {

    TokenStream tstream;
    TokenStream ts = schema.getAnalyzer().tokenStream(fieldName, new StringReader(docText));
    ts.reset();
    tstream = new TokenOrderingFilter(ts, 10);
    return tstream;
  }
}

/** Orders Tokens in a window first by their startOffset ascending.
 * endOffset is currently ignored.
 * This is meant to work around fickleness in the highlighter only.  It
 * can mess up token positions and should not be used for indexing or querying.
 */
final class TokenOrderingFilter extends TokenFilter {
  private final int windowSize;
  private final LinkedList<OrderedToken> queue = new LinkedList<OrderedToken>();
  private boolean done=false;
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

  protected TokenOrderingFilter(TokenStream input, int windowSize) {
    super(input);
    this.windowSize = windowSize;
  }

  @Override
  public boolean incrementToken() throws IOException {
    while (!done && queue.size() < windowSize) {
      if (!input.incrementToken()) {
        done = true;
        break;
      }

      // reverse iterating for better efficiency since we know the
      // list is already sorted, and most token start offsets will be too.
      ListIterator<OrderedToken> iter = queue.listIterator(queue.size());
      while(iter.hasPrevious()) {
        if (offsetAtt.startOffset() >= iter.previous().startOffset) {
          // insertion will be before what next() would return (what
          // we just compared against), so move back one so the insertion
          // will be after.
          iter.next();
          break;
        }
      }
      OrderedToken ot = new OrderedToken();
      ot.state = captureState();
      ot.startOffset = offsetAtt.startOffset();
      iter.add(ot);
    }

    if (queue.isEmpty()) {
      return false;
    } else {
      restoreState(queue.removeFirst().state);
      return true;
    }
  }
}

// for TokenOrderingFilter, so it can easily sort by startOffset
class OrderedToken {
  State state;
  int startOffset;
}

class TermOffsetsTokenStream {

  TokenStream bufferedTokenStream = null;
  OffsetAttribute bufferedOffsetAtt;
  State bufferedToken;
  int bufferedStartOffset;
  int bufferedEndOffset;
  int startOffset;
  int endOffset;

  public TermOffsetsTokenStream( TokenStream tstream ){
    bufferedTokenStream = tstream;
    bufferedOffsetAtt = bufferedTokenStream.addAttribute(OffsetAttribute.class);
    startOffset = 0;
    bufferedToken = null;
  }

  public TokenStream getMultiValuedTokenStream( final int length ){
    endOffset = startOffset + length;
    return new MultiValuedStream(length);
  }

  final class MultiValuedStream extends TokenStream {
    private final int length;
    OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    MultiValuedStream(int length) {
      super(bufferedTokenStream.cloneAttributes());
      this.length = length;
    }

    @Override
    public boolean incrementToken() throws IOException {
      while( true ){
        if( bufferedToken == null ) {
          if (!bufferedTokenStream.incrementToken())
            return false;
          bufferedToken = bufferedTokenStream.captureState();
          bufferedStartOffset = bufferedOffsetAtt.startOffset();
          bufferedEndOffset = bufferedOffsetAtt.endOffset();
        }

        if( startOffset <= bufferedStartOffset &&
          bufferedEndOffset <= endOffset ){
          restoreState(bufferedToken);
          bufferedToken = null;
          offsetAtt.setOffset( offsetAtt.startOffset() - startOffset, offsetAtt.endOffset() - startOffset );
          return true;
        }
        else if( bufferedEndOffset > endOffset ){
          startOffset += length + 1;
          return false;
        }
        bufferedToken = null;
      }
    }
  }
}
