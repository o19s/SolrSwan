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

import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParser;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.o19s.solr.swan.nodes.SwanNode;

public class SwanQParser extends QParser {
	private IndexSchema schema;

	public static ListMultimap<String, String> fieldAliases = ArrayListMultimap
			.create();

	public SwanQParser(String qstr, SolrParams localParams, SolrParams params,
			SolrQueryRequest req, ListMultimap<String, String> fieldAliases) {
		super(qstr, localParams, params, req);
		SwanQParser.fieldAliases = fieldAliases;
		schema = getReq().getSchema();
	}

	@Override
	public Query parse() {

		// TODO stick this in a static factory method in LuceneSwanSearcher and
		// make the constructor private
		@SuppressWarnings("unchecked")
		SwanParser<SwanNode> parser = Parboiled.createParser(SwanParser.class);
		parser.setSearcher(new LuceneSwanSearcher(params, fieldAliases));

		ParsingResult<?> result = new RecoveringParseRunner<Query>(
				parser.Query()).run(this.qstr);

		if (result.hasErrors()) {
			// System.out.println("\nParse Errors:\n" +
			// printParseErrors(result));
		}

		SwanNode node = (SwanNode) result.parseTreeRoot.getValue();
		node.setSchema(schema);
		node.setParser(this);
		Query query = node.getQuery();

		if (query.toString().equals(""))
			throw new IllegalArgumentException("Invalid Query");

		return query;
	}

}
