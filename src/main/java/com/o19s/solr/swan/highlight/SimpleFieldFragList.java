package com.o19s.solr.swan.highlight;

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

import java.util.ArrayList;
import java.util.List;

import com.o19s.solr.swan.highlight.FieldFragList.WeightedFragInfo.SubInfo;
import com.o19s.solr.swan.highlight.FieldPhraseList.WeightedPhraseInfo;

/**
 * A simple implementation of {@link FieldFragList}.
 */
public class SimpleFieldFragList extends FieldFragList {

  /**
   * a constructor.
   * 
   * @param fragCharSize the length (number of chars) of a fragment
   */
  public SimpleFieldFragList( int fragCharSize ) {
    super( fragCharSize );
  }

  /* (non-Javadoc)
   * @see com.o19s.solr.swan.highlight.FieldFragList#add( int startOffset, int endOffset, List<WeightedPhraseInfo> phraseInfoList )
   */
  @Override
  public void add( int startOffset, int endOffset, List<WeightedPhraseInfo> phraseInfoList ) {
    float totalBoost = 0;
    List<SubInfo> subInfos = new ArrayList<SubInfo>();
    for( WeightedPhraseInfo phraseInfo : phraseInfoList ){
      subInfos.add( new SubInfo( phraseInfo.getText(), phraseInfo.getTermsOffsets(), phraseInfo.getSeqnum() ) );
      totalBoost += phraseInfo.getBoost();
    }
    getFragInfos().add( new WeightedFragInfo( startOffset, endOffset, subInfos, totalBoost ) );
  }
  
}
