package com.o19s.solr.swan;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.handler.component.ShardResponse;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.o19s.solr.swan.highlight.WordHashFragmentsBuilder;

/**
 * Provides an end point for listing all the terms related to a search.
 *
 */
public class TermHighlightComponent extends SearchComponent implements SolrCoreAware {


    public static final Logger LOG = LoggerFactory.getLogger(TermHighlightComponent.class);

    // The maximum number of different matching terms accumulated from any one MultiTermQuery
    private static final int MAX_MTQ_TERMS = 1024;

    public static final String COMPONENT_NAME = "th";
    public static final String TERM_FREQUENCY = "termFrequency";
    public static final String TERM_ID = "id";


    protected NamedList initParams;
    public static final String RESPONSE_SECTION = "termHighlights";

    volatile long numRequests;
    volatile long totalRequestsTime;


    /**
     * This flatten method is taken directly from the Solr/Lucene core code base, but isn't directly
     * accessible as it is a private method.  While I recognize there is high cyclomatic complexity
     * in this method, given it's source, I think we are better served using this code as is, rather
     * than attempting to re-engineer it to reduce complexity and risk introducing errors.
     * @param sourceQuery
     * @param reader
     * @param flatQueries
     * @throws IOException
     */
    private void flatten( Query sourceQuery, IndexReader reader, Collection<Query> flatQueries ) throws IOException {
        if( sourceQuery instanceof BooleanQuery){
            BooleanQuery bq = (BooleanQuery)sourceQuery;
            for( BooleanClause clause : bq.getClauses() ){
                if( !clause.isProhibited() )
                    flatten( clause.getQuery(), reader, flatQueries );
            }
        }
        else if( sourceQuery instanceof DisjunctionMaxQuery ){
            DisjunctionMaxQuery dmq = (DisjunctionMaxQuery)sourceQuery;
            for( Query query : dmq ){
                flatten( query, reader, flatQueries );
            }
        }
        else if( sourceQuery instanceof TermQuery ){
            if( !flatQueries.contains( sourceQuery ) )
                flatQueries.add( sourceQuery );
        }
        else if (sourceQuery instanceof MultiTermQuery && reader != null) {
            MultiTermQuery copy = (MultiTermQuery) sourceQuery.clone();
            copy.setRewriteMethod(new MultiTermQuery.TopTermsScoringBooleanQueryRewrite(MAX_MTQ_TERMS));
            BooleanQuery mtqTerms = (BooleanQuery) copy.rewrite(reader);
            flatten(mtqTerms, reader, flatQueries);
        }
        else if( sourceQuery instanceof PhraseQuery ){
            if( !flatQueries.contains( sourceQuery ) ){
                PhraseQuery pq = (PhraseQuery)sourceQuery;
                if( pq.getTerms().length > 1 )
                    flatQueries.add( pq );
                else if( pq.getTerms().length == 1 ){
                    flatQueries.add( new TermQuery( pq.getTerms()[0] ) );
                }
            }
        }
        else if (sourceQuery instanceof SpanQuery) {
            //TODO Note that the way we are doing phrases, they become SpanQueries - thus we loose
            //all of the corner case fixes for the phrases already in highlighing - the result will be
            //phrases that have different color highlights for each term
            Set<Term> terms = new LinkedHashSet<Term>();
            List<AtomicReaderContext> readerContexts = reader.getContext().leaves();

            if(readerContexts.size() < 1) {
                return;
            }

            //TODO it is necessary to call getSpans first so that if there is a MultiTerm query it get's rewritten by com.o19s.solr.swan.nodes.SwanTermNode.SwanSpanMultiTermQueryWrapper
            //no easy way around this
            sourceQuery.extractTerms(terms);
            for(Term t : terms ) {
                flatQueries.add(new SpanTermQuery(t));//TODO need to check that this isn't already in the flatQueries (see example above)
            }

        }
    }

    /**
     * Returns the terms within a query.
     */
    private Set<Term> getTerms(ResponseBuilder rb) throws IOException {

        Set<Query> flatQueries = new LinkedHashSet<Query>();
        HashSet<Term> terms = new HashSet<Term>();

        flatten(rb.getQuery(), rb.req.getSearcher().getIndexReader(), flatQueries);

        for(Query q: flatQueries) {
            q.extractTerms(terms);
        }
        return terms;
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {

        NamedList<Object> termHighlights = new NamedList<Object>();
        NamedList<Object> termData;
        SolrIndexSearcher searcher = rb.req.getSearcher();
        DocSet docs = rb.getResults().docSet;
        TermQuery tq = null;

        numRequests ++;
        long lstartTime = System.currentTimeMillis();
        long count;

        // For each term in the search, look up it's term frequency.
        for(Term t : getTerms(rb)) {
            tq = new TermQuery(t);
            termData = new NamedList<Object>();
            count = Integer.valueOf(searcher.numDocs(tq, docs)).longValue();
            if (count == 0l) {
              continue;
            }
            termData.add(TERM_FREQUENCY, count);
            termData.add(TERM_ID, WordHashFragmentsBuilder.toHash(t.text()));
            termHighlights.add(t.text(), termData);
        };

        rb.rsp.add(RESPONSE_SECTION, termHighlights);
        totalRequestsTime += System.currentTimeMillis() - lstartTime;
    }

    /**
     * This is how we support multi-core distributed searches, this will
     * merge the results so that the final return call will contain all the
     * content.
     */
    @Override
    public void finishStage(ResponseBuilder rb) {
        if (rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {

            NamedList<Object> termHighlights    = new NamedList<Object>();

            for (ShardRequest sreq : rb.finished) {
                if ((sreq.purpose & ShardRequest.PURPOSE_GET_FIELDS) == 0 || !sreq.params.getBool(COMPONENT_NAME,
                false)) {
                    continue;
                }
                for (ShardResponse srsp : sreq.responses) {
                    NamedList<Object> nl = (NamedList<Object>)srsp.getSolrResponse().getResponse().get(RESPONSE_SECTION);
                    merge(termHighlights, nl);
                }
            }
            rb.rsp.add(RESPONSE_SECTION, termHighlights);
        }
    }

    /**
     * merges the contents of nl into all, summing the termFrequencies. This is useful for
     * distributed searches and is called by the "finishStage"
     * @param all
     * @param nl
     */
    void merge(NamedList all, NamedList nl) {

        NamedList<Object> termDataAll;
        NamedList<Object> termDataNl;
        Long total;
        String  key;

        for (int i=0; i < nl.size(); i++) {
            key        = nl.getName(i);
            termDataNl = (NamedList)nl.getVal(i);
            total = (Long)termDataNl.get(TERM_FREQUENCY);
            if (total == 0l) {
              continue;
            }
            if(all.get(key) == null) {
                all.add(key, termDataNl);
            } else {
                termDataAll = (NamedList)all.get(key);
                total += (Long)termDataAll.get(TERM_FREQUENCY);
                termDataAll.setVal((termDataAll.indexOf(TERM_FREQUENCY,0)), total);
            }

        }
    }



    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        rb.setNeedDocSet(true);
    }


    //////////////////////// NamedListInitializedPlugin methods //////////////////////

    @Override
    public void init(NamedList args) {
        super.init(args);
        this.initParams = args;
    }

    @Override
    public void inform(SolrCore core) {

    }

    @Override
    public Category getCategory() {
        return Category.HIGHLIGHTING;
    }

    @Override
    public String getName() {
        return "Term Highlight Component (SWAN)";
    }

    @Override
    public String getSource() {
        return "https://github.com/o19s/SolrSwan";
    }

    @Override
    public NamedList getStatistics() {
        NamedList all = new SimpleOrderedMap<Object>();

        all.add("requests", numRequests);
        all.add("totalTime(ms)", totalRequestsTime);
        return all;
    }

    @Override
    public String getDescription() {
        return "A Component for returning the total number of times a highlighted term occurs in the index.";
    }



}

