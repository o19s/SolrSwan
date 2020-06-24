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

/**
 * Simple HTML Striper Encoder
 * Removes html from the highlighted text
 */
public class SimpleHtmlStriperEncoder implements Encoder {
    private boolean in_tag;

    public SimpleHtmlStriperEncoder() { in_tag = false; }

    @Override
    public String encodeText(String originalText) {
        StringBuilder sb = new StringBuilder();
        for(char c : originalText.toCharArray()) {
            if(!in_tag) {
                if(c == '<') in_tag ^= true; else sb.append(c);
            } else {
                if(c == '>') { in_tag ^= true; sb.append(" "); }
            }
        }
        return sb.toString().replaceAll("\\s+", " ");
//        return originalText.replaceAll("<[^>]*>|^[^<>]+>|<[^<>]+$", " ").replaceAll("\\s+", " ");
    }
}
