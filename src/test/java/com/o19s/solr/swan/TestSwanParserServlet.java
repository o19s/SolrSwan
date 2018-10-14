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

import org.junit.Test;
import org.mockito.Mockito;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestSwanParserServlet extends Mockito {

    @Test
    public void testServlet() throws Exception {
      HttpServletRequest request = mock(HttpServletRequest.class);
      HttpServletResponse response = mock(HttpServletResponse.class);

      when(request.getParameter("q")).thenReturn("search OR string.x.");

      StringWriter stringWriter = new StringWriter();
      PrintWriter writer = new PrintWriter(stringWriter);
      when(response.getWriter()).thenReturn(writer);

      new SwanParserServlet().service(request, response);

      writer.flush(); // it may not have been flushed yet...
      assertTrue(stringWriter.toString().contains("OR(TERM(search),FIELDED_EXPRESSION(x,TERM(string)))"));
    }
    
}
