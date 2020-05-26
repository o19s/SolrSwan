package com.o19s.solr.swan.highlight.spanawarevectorhighlight;

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

//import com.o19s.solr.swan.highlight.FieldFragList.WeightedFragInfo;
//import com.o19s.solr.swan.highlight.vectorhighlight.SpanAwareFieldPhraseList;
//import com.o19s.solr.swan.highlight.vectorhighlight.SpanAwareFastVectorHighlighter;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.highlight.Encoder;

//import org.apache.lucene.search.vectorhighlight.*;
//import org.apache.lucene.search.vectorhighlight.SABoundaryScanner;
//import org.apache.lucene.search.vectorhighlight.SAScoreOrderFragmentsBuilder;
import com.o19s.solr.swan.highlight.spanawarevectorhighlight.SAFieldFragList.WeightedFragInfo;
import com.o19s.solr.swan.highlight.spanawarevectorhighlight.SAFieldPhraseList.WeightedPhraseInfo;
import org.apache.lucene.search.vectorhighlight.BoundaryScanner;

/**
 * An implementation of FragmentsBuilder that outputs score-order fragments, but hightlights
 * terms based on a hash value.
 */
public class WordHashFragmentsBuilder extends SAScoreOrderFragmentsBuilder {

    private static final int    MAX_HIGHLIGHT = 360;

    /**
     * a constructor.
     */
    public WordHashFragmentsBuilder() {
        super();
    }

    public WordHashFragmentsBuilder(BoundaryScanner bs) {
        super(bs);
    }

    /**
     * Use an alternate pre and post tag method, based on the word being highlighted.  In this way
     * each time the same phrase is highlighted in the same css class always.
     * @return
     */
    @Override
    protected String makeFragment(StringBuilder buffer,
                                  int[] index,
                                  Field[] values,
                                  WeightedFragInfo fragInfo,
                                  String[] preTags,
                                  String[] postTags,
                                  Encoder encoder) {
        StringBuilder fragment = new StringBuilder();
        String term;
        final int s = fragInfo.getStartOffset();
        int[] modifiedStartOffset = {s};
        String src = getFragmentSourceMSO(buffer, index, values, s, fragInfo.getEndOffset(), modifiedStartOffset);
        int srcIndex = 0;
        for (WeightedFragInfo.SubInfo subInfo : fragInfo.getSubInfos()) {
            for (WeightedPhraseInfo.Toffs to : subInfo.getTermsOffsets()) {
                term = encoder.encodeText(src.substring(to.getStartOffset() - modifiedStartOffset[0], to.getEndOffset() - modifiedStartOffset[0]));
                fragment
                        .append(encoder.encodeText(src.substring(srcIndex, to.getStartOffset() - modifiedStartOffset[0])))
                        .append(highlightTerm(term));
                srcIndex = to.getEndOffset() - modifiedStartOffset[0];
            }
        }
        fragment.append(encoder.encodeText(src.substring(srcIndex)));
        return fragment.toString();
    }

    public static int toHash(String key) {
        return Math.abs(key.toLowerCase().hashCode() % MAX_HIGHLIGHT);
    }

    /**
     * Calculates the opening tag to use.
     * @return
     */
    String preTag(String originalText) {
        return("<span class=\"highlight" + toHash(originalText) + "\">");
    }

    /**
     * Calculates the closing tag to use.
     * @return
     */
    String postTag() {
        return("</span>");
    }

    /**
     * Highlights a term in a consistent way, but hashing the text to be hightlighted.
     * @param originalText
     * @return
     */
    public String highlightTerm(String originalText) {

        // Allocate StringBuilder with the right number of characters from the
        // beginning, to avoid char[] allocations in the middle of appends.
        StringBuilder returnBuffer = new StringBuilder(preTag(originalText).length() + originalText.length() + postTag().length());
        returnBuffer.append(preTag(originalText));
        returnBuffer.append(originalText);
        returnBuffer.append(postTag());
        return returnBuffer.toString();
    }
}
