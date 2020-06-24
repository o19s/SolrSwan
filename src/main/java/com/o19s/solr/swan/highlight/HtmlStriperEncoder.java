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

import org.apache.lucene.search.highlight.Encoder;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.highlight.HighlightingPluginBase;
import org.apache.solr.highlight.SolrEncoder;

/**
 * Html Striper Encoder
 * Loads Simple Html Striper Encoder
 */
public class HtmlStriperEncoder extends HighlightingPluginBase implements SolrEncoder {
    @Override
    public String getDescription() {
        return "Html Striper";
    }

    public String getSource() {
        return "";
    }

    @Override
    public Encoder getEncoder(String fieldName, SolrParams params) {
        return new SimpleHtmlStriperEncoder();
    }
}
