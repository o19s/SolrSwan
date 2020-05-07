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
import java.util.List;

import com.google.common.base.MoreObjects;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.HighlightComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.solr.util.plugin.SolrCoreAware;

public class SwanHighlightComponent extends HighlightComponent implements PluginInfoInitialized, SolrCoreAware {
  private PluginInfo info = PluginInfo.EMPTY_INFO;
  private SwanHighlighter swanHighlighter;

  @Override
  public String getDescription() {
    return "SWAN Highlighting";
  }

  @Override
  public void init(PluginInfo info) {
    this.info = info;
  }

  @Override
  public void inform(SolrCore core) {
    List<PluginInfo> children = info.getChildren("highlighting");
    if(children.isEmpty()) {
      PluginInfo pluginInfo = core.getSolrConfig().getPluginInfo(SwanHighlighter.class.getName()); //TODO deprecated configuration remove later
      if (pluginInfo != null) {
        swanHighlighter = core.createInitInstance(pluginInfo, SwanHighlighter.class, null, SwanHighlighter.class.getName());
        swanHighlighter.init(pluginInfo);
      } else {
        SwanHighlighter defHighlighter = new SwanHighlighter(core);
        defHighlighter.init(PluginInfo.EMPTY_INFO);
        swanHighlighter = defHighlighter;
      }
    } else {
      swanHighlighter = core.createInitInstance(children.get(0),SwanHighlighter.class,null, SwanHighlighter.class.getName());
    }

  }


  @Override
  public void process(ResponseBuilder rb) throws IOException {
    if (rb.doHighlights) {
      SolrQueryRequest req = rb.req;
      String[] defaultHighlightFields = rb.getQparser() != null ? rb.getQparser().getDefaultHighlightFields() : null;
      Query highlightQuery = rb.getHighlightQuery();
      if(highlightQuery==null) {
        if (rb.getQparser() != null) {
          try {
            highlightQuery = rb.getQparser().getHighlightQuery();
            rb.setHighlightQuery( highlightQuery );
          } catch (Exception e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
          }
        } else {
          highlightQuery = rb.getQuery();
          rb.setHighlightQuery( highlightQuery );
        }
      }

      if( highlightQuery != null ) {
        NamedList ocrHighlights = swanHighlighter.doHighlighting(
                rb.getResults().docList,
                highlightQuery,
                req, defaultHighlightFields);
        if (ocrHighlights != null) {
          rb.rsp.add(highlightingResponseField(), ocrHighlights);
        }
      }

      // Disable further highlighting if fields are not set to prevent the default highlighter
      // from highlighting our OCR fields, which will break.
      ModifiableSolrParams params = new ModifiableSolrParams(rb.req.getParams());
      if (params.get("hl.fl") == null) {
        params.set("hl", "false");
        rb.doHighlights = false;
        // Set the highlighting result to an empty list
        rb.rsp.add("highlighting", new SimpleOrderedMap<>());
      }
      rb.req.setParams(params);
    }
  }

  @Override
  public void prepare(ResponseBuilder rb) throws IOException {
    SolrParams params = rb.req.getParams();
    rb.doHighlights = this.swanHighlighter.isHighlightingEnabled(params);
    if (rb.doHighlights) {
      rb.setNeedDocList(true);
      String hlq = params.get("hl.q");
      String hlparser = (String) MoreObjects.firstNonNull(params.get("hl.qparser"), params.get("defType", "lucene"));
      if (hlq != null) {
        try {
          QParser parser = QParser.getParser(hlq, hlparser, rb.req);
          rb.setHighlightQuery(parser.getHighlightQuery());
        } catch (SyntaxError var6) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, var6);
        }
      }
    }
  }
}
