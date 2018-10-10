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
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.Version;
import org.junit.Test;

/**
 * This class is for demonstration purposes only. No warranty, guarantee, etc.
 * is implied.
 * 
 * This is not production quality code!
 * 
 * 
 **/
public class TermVectorFun {
	public static String[] DOCS = {
			"The quick red fox jumped over the lazy brown dogs.",
			"Mary had a little lamb whose fleece was white as snow.",
			"Moby Dick is a story of a whale and a man obsessed.",
			"The robber wore a black fleece jacket and a baseball cap.",
			"The English Springer Spaniel is the best of all dogs." };

	@Test
	public void testBlah() throws IOException {
		RAMDirectory ramDir = new RAMDirectory();
		// Index some made up content
		IndexWriterConfig iwf = new IndexWriterConfig(Version.LUCENE_47,
				new StandardAnalyzer(Version.LUCENE_47));
		IndexWriter writer = new IndexWriter(ramDir, iwf);
		FieldType ft = new FieldType();
	    ft.setIndexed(true);
	    ft.setTokenized(true);
	    ft.setStored(true);
		ft.setStoreTermVectorOffsets(true);
		ft.setStoreTermVectors(true);
		ft.setStoreTermVectorPositions(true);
	    ft.freeze();
		for (int i = 0; i < DOCS.length; i++) {
			Document doc = new Document();
			StringField id = new StringField("id", "doc_" + i, StringField.Store.YES);
			doc.add(id);
			// Store both position and offset information
			Field text = new Field("content", DOCS[i], ft);
//					Field.Index.ANALYZED,
//					Field.TermVector.WITH_POSITIONS_OFFSETS);
			doc.add(text);
			writer.addDocument(doc);
		}
		//writer.close();
		// Get a searcher
		AtomicReader dr = SlowCompositeReaderWrapper.wrap(DirectoryReader.open(
				writer, true));
		IndexSearcher searcher = new IndexSearcher(dr);
		// Do a search using SpanQuery
		SpanTermQuery fleeceQ = new SpanTermQuery(new Term("content", "fleece"));
		TopDocs results = searcher.search(fleeceQ, 10);
		for (int i = 0; i < results.scoreDocs.length; i++) {
			ScoreDoc scoreDoc = results.scoreDocs[i];
			System.out.println("Score Doc: " + scoreDoc);
		}
		IndexReader reader = searcher.getIndexReader();
		Bits acceptDocs = null;
		Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
		Spans spans = fleeceQ.getSpans(dr.getContext(), acceptDocs,
				termContexts);
		
		while (spans.next()) {
			System.out.println("Doc: " + spans.doc() + " Start: "
					+ spans.start() + " End: " + spans.end());
			DocumentStoredFieldVisitor visitor = new DocumentStoredFieldVisitor("content");
			reader.document(spans.doc(), visitor);
			Terms terms = reader.getTermVector(spans.doc(), "content");
			TermsEnum tenum = terms.iterator(null);
//			AttributeSource as = tenum.attributes();

			while (tenum.next() != null) {
				System.out.println(tenum.term().utf8ToString());
			}
			for (long pos = 0L; pos < spans.end(); pos++) {
//				tenum.next();
//				if (tenum.ord()<pos) continue;
//				System.out.println(tenum.term());
//				
			}
			
			reader.document(spans.doc(), visitor);
//			String[] values = visitor.getDocument().getValues("content");
//			List<String> a = new ArrayList<String>();
//			// build up the window
//			tvm.start = spans.start() - window;
//			tvm.end = spans.end() + window;
//			reader.getTermFreqVector(spans.doc(), "content", tvm);
//			for (WindowEntry entry : tvm.entries.values()) {
//				System.out.println("Entry: " + entry);
//			}
//			// clear out the entries for the next round
//			tvm.entries.clear();
		}
	}

}
