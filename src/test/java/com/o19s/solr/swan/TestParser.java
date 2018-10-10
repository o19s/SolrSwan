package com.o19s.solr.swan;

import org.junit.Test;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.o19s.solr.swan.TestBigSwan.TextInfo;

public class TestParser {
  public static final Logger log = LoggerFactory.getLogger(TestBigSwan.class);

  @SuppressWarnings("unchecked")
  SwanParser<String> parser = Parboiled.createParser(SwanParser.class);
  StringStubSwanSearcher searcher = new StringStubSwanSearcher();
  ParseRunner<String> parseRunner = new RecoveringParseRunner<String>(parser.Query(), 9000);
  TextInfo textInfo;
  

@Test
public void testParsing(){
	SwanParser<String> _parser = Parboiled.createParser(SwanParser.class);
	_parser.setSearcher(new StringStubSwanSearcher());

	String input = "123/45;234/43,1.range.";

	ParsingResult<?> result = new RecoveringParseRunner<String>(
			_parser.Query()).run(input);

//	if (result.hasErrors()) {
//		System.out.println("\nParse Errors:\n" + (result));
//	}

	Object value = result.parseTreeRoot.getValue();
	System.out.println(value);

	ParseTreeUtils.printNodeTree(result);
}
  
}
