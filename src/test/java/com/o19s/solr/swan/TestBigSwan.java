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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner.TimeoutException;
import org.parboiled.support.ParsingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(value = Parameterized.class)
public class TestBigSwan {
	public static final Logger log = LoggerFactory.getLogger(TestBigSwan.class);

	@SuppressWarnings("unchecked")
	SwanParser<String> parser = Parboiled.createParser(SwanParser.class);
	StringStubSwanSearcher searcher = new StringStubSwanSearcher();
	ParseRunner<String> parseRunner = new RecoveringParseRunner<String>(parser.Query(), 9000);
	TextInfo textInfo;
	
	public TestBigSwan(TextInfo textInfo) {
		this.textInfo = textInfo;
		parser.setSearcher(searcher);	
	}
	
	@Test(timeout=20000) // Timeout in 20s
	public void testLogEntry() {
		try {
			ParsingResult<?> result = parseRunner.run(textInfo.text);
			if (result.hasErrors()) {
				log.error(textInfo.text);
			}
			if(result.hasErrors()) {
				if(textInfo.result == Result.OK)
					Assert.fail(textInfo.text);
			} else {
				if(textInfo.result == Result.FAIL) 
					log.error("Swan parsed when BRS did not: [" + textInfo.text + "]");
			}
		} catch(TimeoutException e) {
			log.error("QUERY TIMEOUT: [" + textInfo.text + "]");
			Assert.fail("QUERY TIMEOUT: [" + textInfo.text + "]");
		}
	}
	
	@Parameters
	public static Collection<Object[]> data() throws IOException {
        Collection<Object[]> suiteArray = new ArrayList<Object[]>();

        BufferedReader in = new BufferedReader(new InputStreamReader(
        		TestBigSwan.class.getClassLoader().getResourceAsStream("j1searches_2.test"),
                Charset.forName("UTF8")));
        while (in.ready()) {
            if (in.read() == '<' && in.ready() && in.read() == '<') {
                in.readLine();
                ArrayList<TextInfo> argArray = new ArrayList<TextInfo>();
                argArray.add(parseText(in));
                suiteArray.add(argArray.toArray());
            }
        }
        return suiteArray;
    }

    private static TextInfo parseText(BufferedReader in) throws IOException {
        Pattern unicodes = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

        StringBuffer text = new StringBuffer();
        while (in.ready()) {
            String line = in.readLine();
            if (line.startsWith(">>")) {
                return new TextInfo(line.toLowerCase().contains("ok") ? Result.OK : Result.FAIL, text.toString());
            } else {
                Matcher matcher = unicodes.matcher(line);
                while (matcher.find()) {
                    matcher.appendReplacement(text, Character.toString((char) Integer.parseInt(matcher.group(1), 16)));
                }
                matcher.appendTail(text);
                text.append('\n');
            }
        }
        throw new NoSuchElementException("Expected \">>\"");
    }

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		//_parser = Parboiled.createParser(SwanParser.class);
		//_parser.setSearcher(new StringStubSwanSearcher());
	}
	
    static enum Result {
        OK, FAIL
    }

    static class TextInfo {
        Result result;
        String text;

        public TextInfo(Result result, String text) {
            super();
            this.result = result;
            this.text = text.trim();
        }
    }

}
