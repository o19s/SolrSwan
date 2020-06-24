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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class SwanQParserPlugin extends QParserPlugin implements ResourceLoaderAware {
  public static ListMultimap<String,String> fieldAliases =  ArrayListMultimap.create();
	public static final Logger LOG = LoggerFactory.getLogger(SwanQParserPlugin.class);

	private String fieldAliasesFileName;

	@SuppressWarnings("rawtypes")
	@Override
	public void init(NamedList args) {
		super.init(args);
		fieldAliasesFileName = (String) args.get("fieldAliases");
	}

	@Override
	public QParser createParser(String qstr, SolrParams localParams,
			SolrParams params, SolrQueryRequest req) {	
		return new SwanQParser(qstr, localParams, params, req, fieldAliases);
	}

	@Override
	public void inform(ResourceLoader loader) throws IOException {
		if (fieldAliasesFileName != null) {
			InputStream is = loader.openResource(fieldAliasesFileName);
			DataInputStream in = new DataInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] keysVal = strLine.split("=>");
				if(keysVal.length != 2) {
					throw new IOException("keysVal.length != 2 after split");
				}
				String[] keys = keysVal[0].trim().split("\\s*,\\s*");
				String val = keysVal[1].trim().toLowerCase();
				if(!val.matches("[a-zA-Z0-9_-]+")) {
					throw new IOException("value doesn't match regex [a-zA-Z0-9_]+");
				}
				for(String k : keys) {
					if(!k.matches("[a-zA-Z0-9_-]+")) {
						throw new IOException("key doesn't match regex [a-zA-Z0-9_]+");
					}
					fieldAliases.put(k.trim().toLowerCase(), val);
				}
			}
			in.close();
		}
	}

}
