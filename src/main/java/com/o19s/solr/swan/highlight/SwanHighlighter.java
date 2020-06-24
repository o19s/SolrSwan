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
import java.util.*;

import com.o19s.solr.swan.highlight.spanawarevectorhighlight.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.OffsetLimitTokenFilter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.vectorhighlight.BoundaryScanner;
import com.o19s.solr.swan.highlight.spanawarevectorhighlight.SAFragmentsBuilder;
import org.apache.lucene.util.AttributeSource.State;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
//import org.apache.solr.highlight.*;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.solr.highlight.SolrBoundaryScanner;
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

  final String ADDITIONAL_FIELDS = "hl.addFields";
  final String SMARKER = "hl.sm";
  final String PMARKER = "hl.pm";

  // Need to override the DefaultSolrHighlighter because its solrCore is private
  public SwanHighlighter(SolrCore solrCore) {
    super(solrCore);
  }

  // Need to override the DefaultSolrHighlighter because its solrCore is private
  @Override
  public void init(PluginInfo info) {
    super.init(info);
    //SolrFormatter fmt = new HtmlFormatter();
    //formatters.put("html", fmt);
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
  public NamedList<Object> doHighlighting(DocList docs, Query query, SolrQueryRequest req, String[] defaultFields)
          throws IOException
  {
    int maxNumFragments = 10000;
    NamedList fragments = new SimpleOrderedMap();

    SolrParams params = req.getParams();
    if (!isHighlightingEnabled(params))
      return null;

    SolrIndexSearcher searcher = req.getSearcher();
    IndexSchema schema = searcher.getSchema();
    Analyzer analyzer = schema.getQueryAnalyzer();

    String[] fieldNames = getHighlightFields(query, req, defaultFields);
    String[] addFieldNames = req.getParams().getParams(ADDITIONAL_FIELDS);
    String smMarker = req.getParams().get(SMARKER);
    String pmMarker = req.getParams().get(PMARKER);

    Set<String> fset = new HashSet<>(Arrays.asList(fieldNames));

    SchemaField keyField = schema.getUniqueKeyField();
    if(null != keyField)
      fset.add(keyField.getName());

    Set<Integer> docIds = new HashSet<>();
    DocIterator iterator = docs.iterator();
    for (int i = 0; i < docs.size(); i++) {
      docIds.add(iterator.nextDoc());
    }

    for (int docId : docIds) {
      Document doc = searcher.doc(docId, fset);
      NamedList docSummaries = new SimpleOrderedMap();
      Fields tvFields = searcher.getIndexReader().getTermVectors(docId);

      for (String fieldName : fieldNames) {
//        fieldName = fieldName.trim();
        if(StringUtils.isBlank(fieldName))
          continue;

        IndexableField[] docFields = doc.getFields(fieldName);
        List<String> listFields = new ArrayList<>();
        for (IndexableField field : docFields) {
          listFields.add(field.stringValue());
        }

        //String[] docTexts = listFields.toArray(new String[listFields.size()]);
        String[] docTexts = listFields.toArray(new String[0]);
        if (docTexts.length == 0) break;

        int numFragments = getMaxSnippets(fieldName, params);
        boolean mergeContiguousFragments = isMergeContiguousFragments(fieldName, params);

        List<TextFragment> frags = new ArrayList<>();
        for (int j = 0; j < docTexts.length; j++) {
          int maxCharsToAnalyze = docTexts[j].length();
          TokenStream tokenStream;
          TokenStream ts = TokenSources.getTokenStream(fieldName, tvFields, docTexts[j], analyzer, maxCharsToAnalyze - 1);
          TokenStream tstream = new TokenOrderingFilter(ts, 100);

          Highlighter highlighter = null;
          if (Boolean.valueOf(req.getParams().get(HighlightParams.USE_PHRASE_HIGHLIGHTER, "true"))) {
            if (maxCharsToAnalyze < 0) {
              tokenStream = new CachingTokenFilter(tstream);
            } else {
              tokenStream = new CachingTokenFilter(new OffsetLimitTokenFilter(tstream, maxCharsToAnalyze));
            }

            // get highlighter
            highlighter = getPhraseHighlighter(query, fieldName, req, tokenStream);
          }
          else {
            // use "the old way"
            highlighter = getHighlighter(query, fieldName, req);
            tokenStream = tstream;
          }

          highlighter.setMaxDocCharsToAnalyze(docTexts[j].length());

          try {
            TextFragment[] bestTextFragments = highlighter.getBestTextFragments(tokenStream, docTexts[j], mergeContiguousFragments, maxNumFragments);
            for (int k = 0; k < bestTextFragments.length; k++) {
              if ((bestTextFragments[k] != null) && (bestTextFragments[k].getScore() > 0)) {
                frags.add(bestTextFragments[k]);
              }
            }
          } catch (InvalidTokenOffsetsException e) {
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
          }
          tokenStream.close();
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
          List<String> fragTexts = new ArrayList<>();
          for (TextFragment fragment: frags) {
            if ((fragment != null) && (fragment.getScore() > 0)) {

              String sfragment = fragment.toString();
              if(smMarker != null)
                sfragment = sfragment.replace(smMarker, "");
              if(pmMarker != null)
                sfragment = sfragment.replace(pmMarker, "");
              fragTexts.add(sfragment);
            }
            if (fragTexts.size() >= numFragments) break;
          }
          docSummaries.add("total_found", frags.size());
          docSummaries.add("showed", fragTexts.size());
          summaries = fragTexts.toArray(new String[0]);
          if (summaries.length > 0) {
            docSummaries.add(fieldName, summaries);
            if(addFieldNames != null) {
              for(String fn : addFieldNames) {
                fn = fn.trim();
                if(schema.getField(fn) == null)
                  continue;
                IndexableField[] df = doc.getFields(fn);
                if(!schema.getField(fn).multiValued())
                  docSummaries.add(fn, df[0]);
                else
                  docSummaries.add(fn, df);
              }
            }
          }
        }
        // no summeries made, copy text from alternate field
        if (summaries == null || summaries.length == 0) {
          alternateField( docSummaries, params, doc, fieldName );
        }

      }
      String printId = schema.printableUniqueKey(doc);
      fragments.add(printId == null ? null : printId, docSummaries);
    }

    return fragments;
  }

/*
  @Override
  @SuppressWarnings("unchecked")
  public NamedList<Object> doHighlighting(DocList docs, Query query, SolrQueryRequest req, String[] defaultFields)
          throws IOException
  {
    int maxNumFragments = 10000;
    NamedList fragments = new SimpleOrderedMap();

    SolrParams params = req.getParams();
    if (!isHighlightingEnabled(params))
      return null;

    SolrIndexSearcher searcher = req.getSearcher();
    IndexSchema schema = searcher.getSchema();
    Analyzer analyzer = schema.getQueryAnalyzer();

    String[] fieldNames = getHighlightFields(query, req, defaultFields);
    //String[] addFieldNames = req.getParams().getParams(ADDITIONAL_FIELDS);
    String smMarker = req.getParams().get(SMARKER);
    String pmMarker = req.getParams().get(PMARKER);

    Set<String> fset = new HashSet<>(Arrays.asList(fieldNames));

    SchemaField keyField = schema.getUniqueKeyField();
    if(null != keyField)
      fset.add(keyField.getName());

    Set<Integer> docIds = new HashSet<>();
    DocIterator iterator = docs.iterator();
    for (int i = 0; i < docs.size(); i++) {
      docIds.add(iterator.nextDoc());
    }

    // get FastVectorHighlighter instance out of the processing loop
    SAFastVectorHighlighter safvh = new SAFastVectorHighlighter(
            // FVH cannot process hl.usePhraseHighlighter parameter per-field basis
            params.getBool( HighlightParams.USE_PHRASE_HIGHLIGHTER, true ),
            // FVH cannot process hl.requireFieldMatch parameter per-field basis
            params.getBool( HighlightParams.FIELD_MATCH, false ),
            getFragListBuilder(params),
            new WordHashFragmentsBuilder());

    safvh.setPhraseLimit(params.getInt(HighlightParams.PHRASE_LIMIT, Integer.MAX_VALUE));
    SAFieldQuery fieldQuery = new SAFieldQuery(query, searcher, searcher.getIndexReader(),
            params.getBool( HighlightParams.USE_PHRASE_HIGHLIGHTER, true ),
            params.getBool( HighlightParams.FIELD_MATCH, false ));
    //FieldQuery fieldQuery = safvh.getFieldQuery(query, searcher.getIndexReader());

    for (int docId : docIds) {
      Document doc = searcher.doc(docId, fset);
      NamedList docSummaries = new SimpleOrderedMap();
      for (String fieldName : fieldNames) {
        if(StringUtils.isBlank(fieldName))
          continue;
        if( useFastVectorHighlighter( params, schema, fieldName ) )
          doHighlightingByFastVectorHighlighter( safvh, fieldQuery, req, docSummaries, docId, doc, fieldName );
        else
          doHighlightingByHighlighter( query, req, docSummaries, docId, doc, fieldName, smMarker, pmMarker );
      }
      String printId = schema.printableUniqueKey(doc);
      fragments.add(printId == null ? null : printId, docSummaries);
    }
    //CHANGE end
    return fragments;
  }
*/
  /*
   * If fieldName is undefined, this method returns false, then
   * doHighlightingByHighlighter() will do nothing for the field.
   */

  private boolean useFastVectorHighlighter( SolrParams params, IndexSchema schema, String fieldName ){
    SchemaField schemaField = schema.getFieldOrNull( fieldName );
    if( schemaField == null ) return false;
    boolean useFvhParam = params.getFieldBool( fieldName, HighlightParams.USE_PHRASE_HIGHLIGHTER, false );
    if( !useFvhParam ) return false;
    boolean termPosOff = schemaField.storeTermPositions() && schemaField.storeTermOffsets();
    if( !termPosOff ) {
      log.warn( "Solr will use Highlighter instead of FastVectorHighlighter because {} field does not store TermPositions and TermOffsets.", fieldName );
    }
    return termPosOff;
  }

  private void doHighlightingByHighlighter( Query query, SolrQueryRequest req, NamedList docSummaries,
                                            int docId, Document doc, String fieldName, String smMarker, String pmMarker)
          throws IOException
  {
    int maxNumFragments = 10000;

    SolrIndexSearcher searcher = req.getSearcher();
    IndexSchema schema = searcher.getSchema();
    Analyzer analyzer = schema.getQueryAnalyzer();
    Fields tvFields = searcher.getIndexReader().getTermVectors(docId);

    SolrParams params = req.getParams();
    IndexableField[] docFields = doc.getFields(fieldName);
    List<String> listFields = new ArrayList<>();
    for (IndexableField field : docFields) {
      listFields.add(field.stringValue());
    }

    String[] docTexts = listFields.toArray(new String[0]);

    // according to Document javadoc, doc.getValues() never returns null. check empty instead of null
    if (docTexts.length == 0) return;

    int numFragments = getMaxSnippets(fieldName, params);
    boolean mergeContiguousFragments = isMergeContiguousFragments(fieldName, params);

    List<TextFragment> frags = new ArrayList<>();
    for (int j = 0; j < docTexts.length; j++) {
      int maxCharsToAnalyze = docTexts[j].length();
      TokenStream tokenStream;
      TokenStream ts = TokenSources.getTokenStream(fieldName, tvFields, docTexts[j], analyzer, maxCharsToAnalyze - 1);
      TokenStream tstream = new TokenOrderingFilter(ts, 100);

      Highlighter highlighter = null;
      if (Boolean.valueOf(req.getParams().get(HighlightParams.USE_PHRASE_HIGHLIGHTER, "true"))) {
        if (maxCharsToAnalyze < 0) {
          tokenStream = new CachingTokenFilter(tstream);
        } else {
          tokenStream = new CachingTokenFilter(new OffsetLimitTokenFilter(tstream, maxCharsToAnalyze));
        }

        // get highlighter
        highlighter = getPhraseHighlighter(query, fieldName, req, tokenStream);
      }
      else {
        // use "the old way"
        highlighter = getHighlighter(query, fieldName, req);
        tokenStream = tstream;
      }

      highlighter.setMaxDocCharsToAnalyze(docTexts[j].length());
      try {
        TextFragment[] bestTextFragments = highlighter.getBestTextFragments(tokenStream, docTexts[j], mergeContiguousFragments, maxNumFragments);
        for (int k = 0; k < bestTextFragments.length; k++) {
          if ((bestTextFragments[k] != null) && (bestTextFragments[k].getScore() > 0)) {
            frags.add(bestTextFragments[k]);
          }
        }
      } catch (InvalidTokenOffsetsException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
      }
      tokenStream.close();
    }

    // sort such that the fragments with the highest score come first
    Collections.sort(frags, new Comparator<TextFragment>() {
      public int compare(TextFragment arg0, TextFragment arg1) {
        return Math.round(arg1.getScore() - arg0.getScore());
      }
    });

    String[] summaries = null;
    if (frags.size() > 0) {
      List<String> fragTexts = new ArrayList<>();
      for (TextFragment fragment: frags) {
        if ((fragment != null) && (fragment.getScore() > 0)) {

          String sfragment = fragment.toString();
          if(smMarker != null)
            sfragment = sfragment.replace(smMarker, "");
          if(pmMarker != null)
            sfragment = sfragment.replace(pmMarker, "");
          fragTexts.add(sfragment);
        }
        if (fragTexts.size() >= numFragments)
          break;
        summaries = fragTexts.toArray(new String[0]);
        if (summaries.length > 0)
          docSummaries.add(fieldName, summaries);
      }
    }
    // no summeries made, copy text from alternate field
    if (summaries == null || summaries.length == 0) {
      alternateField( docSummaries, params, doc, fieldName );
    }
  }

  protected SolrFragmentsBuilder getSolrFragmentsBuilder(String fieldName, SolrParams params )
  {
    String fb = params.getFieldParam( fieldName, HighlightParams.FRAGMENTS_BUILDER );
    SolrFragmentsBuilder solrFb = fragmentsBuilders.get( fb );
    if( solrFb == null ){
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Unknown fragmentsBuilder: " + fb );
    }
    return solrFb;
  }

  private void doHighlightingByFastVectorHighlighter(SAFastVectorHighlighter highlighter, SAFieldQuery spanAwareFieldQuery,
                                                     SolrQueryRequest req, NamedList docSummaries, int docId, Document doc,
                                                     String fieldName )
          throws IOException
  {
    SolrParams params = req.getParams();
    SolrFragmentsBuilder solrFb = getSolrFragmentsBuilder( fieldName, params );
    String[] snippets = highlighter.getBestFragments(spanAwareFieldQuery, req.getSearcher().getIndexReader(), docId, fieldName,
            params.getFieldInt( fieldName, HighlightParams.FRAGSIZE, 100 ),
            params.getFieldInt( fieldName, HighlightParams.SNIPPETS, 1 ),
            getFragListBuilderOverride( fieldName, params ),
            getFragmentsBuilderOverride( fieldName, params ),
            solrFb.getPreTags( params, fieldName ),
            solrFb.getPostTags( params, fieldName ),
            getEncoder( fieldName, params ) );
    if( snippets != null && snippets.length > 0 )
      docSummaries.add( fieldName, snippets );
    else
      alternateField( docSummaries, params, doc, fieldName );
  }

  private SAFragListBuilder getFragListBuilder(SolrParams params)
  {
    SAFragListBuilder fragListBuilder;
    String fb = params.get(HighlightParams.FRAG_LIST_BUILDER).toLowerCase();
    if (fb.equals("single")) {
      fragListBuilder = new SASingleFragListBuilder();
    } else {
      fragListBuilder = new SASimpleFragListBuilder();
    }
    return fragListBuilder;
  }


  private SAFragListBuilder getFragListBuilderOverride(String fieldName, SolrParams params)
  {
    SAFragListBuilder fragListBuilder;
    String fb = params.getFieldParam( fieldName, HighlightParams.FRAG_LIST_BUILDER ).toLowerCase();
    if (fb.equals("single")) {
      fragListBuilder = new SASingleFragListBuilder();
    } else {
      fragListBuilder = new SASimpleFragListBuilder();
    }
    return fragListBuilder;
  }

  protected SAFragmentsBuilder getFragmentsBuilderOverride(String fieldName, SolrParams params ){
    SAFragmentsBuilder fragmentsBuilder;
    BoundaryScanner bs = getBoundaryScanner(fieldName, params);
    String fb = params.getFieldParam(fieldName, HighlightParams.FRAGMENTS_BUILDER).toLowerCase();
    if("scoreorder".equals(fb)) {
      fragmentsBuilder = new SAScoreOrderFragmentsBuilder(bs);
    } else if("wordhash".equals(fb)) {
      fragmentsBuilder = new WordHashFragmentsBuilder(bs);
    } else {
      fragmentsBuilder = new SASimpleFragmentsBuilder(bs);
    }
    return fragmentsBuilder;
    //return getSolrFragmentsBuilderOverride( fieldName, params ).getFragmentsBuilder(params, bs);
  }
  /*
  protected SolrFragmentsBuilder getSolrFragmentsBuilderOverride( String fieldName, SolrParams params ){
    String fb = params.getFieldParam( fieldName, HighlightParams.FRAGMENTS_BUILDER );
    SolrFragmentsBuilder solrFb = fragmentsBuilders.get(fb);
    if( solrFb == null ){
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Unknown fragmentsBuilder: " + fb );
    }
    return solrFb;
  }*/

  /*
  protected BoundaryScanner getBoundaryScannerOverride(String fieldName, SolrParams params){
    String bs = params.getFieldParam(fieldName, HighlightParams.BOUNDARY_SCANNER);
    SolrBoundaryScanner solrBs = boundaryScanners.get(bs);
    if(solrBs == null){
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Unknown boundaryScanner: " + bs);
    }
    return solrBs.getBoundaryScanner(fieldName, params);
  }
   */

  private void alternateField( NamedList docSummaries, SolrParams params, Document doc, String fieldName ){
    String alternateField = params.getFieldParam(fieldName, HighlightParams.ALTERNATE_FIELD);
    if (alternateField != null && alternateField.length() > 0) {
      IndexableField[] docFields = doc.getFields(alternateField);
      List<String> listFields = new ArrayList<String>();
      for (IndexableField field : docFields) {
        if (field.binaryValue() == null)
          listFields.add(field.stringValue());
      }

      String[] altTexts = listFields.toArray(new String[0]);

      if (altTexts.length > 0){
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

  @Override
  public void reset() throws IOException {
    super.reset();
  }

  @Override
  public void end() throws IOException {
    super.end();
  }

  @Override
  public void close() throws IOException {
    super.close();
  }
}

// for TokenOrderingFilter, so it can easily sort by startOffset
class OrderedToken {
  State state;
  int startOffset;
}

/*
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
*/