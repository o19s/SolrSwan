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

import org.parboiled.Parboiled;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.parboiled.errors.ErrorUtils.printParseErrors;

public class SwanParserServlet extends HttpServlet {

	private static final long serialVersionUID = -1956540902145964776L;
	private static StringStubSwanSearcher stringSearcher = new StringStubSwanSearcher();
	private static JsonStubSwanSearcher jsonSearcher = new JsonStubSwanSearcher();

	/**
	 * 
	 */
    public void service(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
        String q = req.getParameter("q");
        PrintWriter out = res.getWriter();

    	@SuppressWarnings("unchecked")
		SwanParser<String> parser = Parboiled.createParser(SwanParser.class);
        if (req.getHeader("Accept") == null || 
        		req.getHeader("Accept").contains("text/html")) {
    		parser.setSearcher(stringSearcher);
        	res.setContentType("text/plain");
        }
        else {
        	parser.setSearcher(jsonSearcher);
        	res.setContentType("application/json");
        }
        
        ParsingResult<?> result = new RecoveringParseRunner<String>(parser.Query()).run(q);
        if (result.hasErrors()) {
        	res.setContentType("text/plain");
        	out.println(printParseErrors(result));
        }
        else {
        	out.println(result.parseTreeRoot.getValue().toString());
        }
    }
    
}
