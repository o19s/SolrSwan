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

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.support.StringVar;
import org.parboiled.support.Var;

/**
 * Syntax precedence taken from LLDiscoveryServer9_0UNIXUsersGuide.pdf p.50
 * the only difference is that the NOT operator is assumed to have highest precedence instead of near lowest
 * Left associativity assumed for all operators
 * @author berryman
 *
 * @param <V>
 */
@BuildParseTree
public class SwanParser<V> extends BaseParser<V> {
  public Rule Query() {
    return Sequence(OrExpression(),EOI);
  }

  public Rule OrExpression() {
    return Sequence(
      XorExpression(),
      ZeroOrMore(
        Sequence(
          FirstOf(OR(), OR_SYMBOL()),
          XorExpression(),
          push(searcher.or(pop(1),pop())))
      )
    );
  }

  public Rule XorExpression() {
    return Sequence(
      AndExpression(),
      ZeroOrMore(
        Sequence(XOR(),AndExpression(),push(searcher.xor(pop(1),pop())))
      )
    );
  }


  public Rule AndExpression() {
    return Sequence(
      SameExpression(),
      ZeroOrMore(
        Sequence(
          FirstOf(AND(), AND_SYMBOL()),
          SameExpression(),push(searcher.and(pop(1),pop()))
        )
      )
    );
  }

  public Rule SameExpression() {
    Var<Integer> n = new Var<Integer>();
    return Sequence(
      WithExpression(),
      ZeroOrMore(
        Sequence(SAME(n), WithExpression(), push(searcher.same(pop(1),pop(),n.get())))
      )
    );
  }

  public Rule WithExpression() {
    Var<Integer> n = new Var<Integer>();
    return Sequence(
      AdjNearExpression(),
      ZeroOrMore(
        Sequence(WITH(n), AdjNearExpression(), push(searcher.with(pop(1),pop(),n.get())))
      )
    );
  }

  public Rule AdjNearExpression() {
    Var<Integer> n = new Var<Integer>();
    return Sequence(
      NotExpression(),
      ZeroOrMore(FirstOf(
        Sequence(NEAR(n), NotExpression(), push(searcher.near(pop(1),pop(),n.get()))),
        Sequence(ADJ(n), NotExpression(), push(searcher.adj(pop(1),pop(),n.get()))),
        Sequence(DEFAULT_OPERATOR(), NotExpression(), push(searcher.defaultOp(pop(1),pop()))))
      )
    );
  }

  public Rule NotExpression() {
    return FirstOf(
      Sequence(Clause(), NOT(), Clause(), push(searcher.not(pop(1), pop()))),
      ClassificationOr(),
      Clause()
    );
  }

  
  // this is the high level rule to define a classification range
  // an example would look like this (123/456-789,11,800-900).range.
  public Rule ClassificationOr() {
	    StringVar mainClassification = new StringVar();
	    StringVar subClassification1 = new StringVar();
	    StringVar subClassification2 = new StringVar();
	    StringVar n = new StringVar();
		return Sequence(
		        Optional("("),
				OptionalWhiteSpace(),
				WordCharNoDashes(), mainClassification.set(match()),
				OptionalWhiteSpace(),
				"/",
				OptionalWhiteSpace(),
				FirstOf(
						PushDashRange(mainClassification, subClassification1, subClassification2),
						PushWordNoDashes(mainClassification)
						),
				ZeroOrMore(
						OptionalWhiteSpace(),
						",",
						OptionalWhiteSpace(),
						FirstOf(
								PushOrWithRange(mainClassification, subClassification1, subClassification2),
								PushOrWithFloatDigits(mainClassification))),
				OptionalWhiteSpace(),
				ZeroOrMore(
				    OneOrMore(AnyOf(";")),
                    WordCharNoDashes(), mainClassification.set(match()),
                    OptionalWhiteSpace(),
                    "/",
                    OptionalWhiteSpace(),
                    FirstOf(
                            PushOrWithRange(mainClassification, subClassification1, subClassification2),
                            PushOrWithFloatDigits(mainClassification)
                            ),
                    ZeroOrMore(
                            OptionalWhiteSpace(),
                            ",",
                            OptionalWhiteSpace(),
                            FirstOf(
                                    PushOrWithRange(mainClassification, subClassification1, subClassification2),
                                    PushOrWithFloatDigits(mainClassification))),
                    OptionalWhiteSpace()),
                Optional(")"),
				DotField(n), push(searcher.fieldedSubExpressions(n.get(), pop())), 
				Test(EOI)
				);
	}

  
  public Rule DashRange(StringVar subClassification1, StringVar subClassification2){
	  return Sequence(
			  WordCharNoDashes(), subClassification1.set(match()),
			  OptionalWhiteSpace(),
			  "-",
			  OptionalWhiteSpace(),
			  WordCharNoDashes(), subClassification2.set(match()));
  }
  
  public Rule PushDashRange(StringVar mainClassification, StringVar subClassification1, StringVar subClassification2){
	  return Sequence(
			  DashRange(subClassification1, subClassification2),
			  push(searcher.classRange(null, mainClassification.get(), subClassification1.get(), subClassification2.get())));
			  
			  
  }

  public Rule PushWordNoDashes(StringVar mainClassification){
		return Sequence(WordCharNoDashes(),
		push(searcher.term(mainClassification.get() + "/" + match())));
  }
  
  public Rule PushOrWithRange(StringVar mainClassification, StringVar subClassification1, StringVar subClassification2){
	  return Sequence(			  
			  DashRange(subClassification1, subClassification2),
			  push(searcher.or(
				pop(),
				searcher.classRange(null,mainClassification.get(), subClassification1.get(), subClassification2.get()))));
  }

  public Rule PushOrWithFloatDigits(StringVar mainClassification){
	  return Sequence(FloatChar(),
		push(searcher.or(
				pop(),
				searcher.term(mainClassification.get() + "/" + match()))));
  }
  

  //@SuppressSubnodes
  public Rule Clause() {
    Var<String> n = new Var<String>();
    return Sequence(
      FirstOf(
        Term(n),
        Phrase(),
        Parens(),
        BoundRange(),
        Range()
      ),
      Optional(
        Field(n), push(searcher.fieldedExpression(n.get(), pop()))
      )
    );
  }

  public Rule Field(Var<String> n) {
    return Sequence(
      FirstOf(
        DotField(n),
        BracketField(n)
      ),
      FirstOf(
        WhiteSpace(),
        Test(")"),
        Test(EOI)
      )
    );
  }
  public Rule DotField(Var<String> n) {
    return Sequence(
      ".",
      OneOrMore(WordChar()),n.set(match()),
      "."
    );
  }
  public Rule BracketField(Var<String> n) {
    return Sequence(
      "[",
      OneOrMore(WordChar()),n.set(match()),
      "]"
    );
  }

  public Rule Term(Var<String> n) {
    return Sequence(
      Sequence(
        OneOrMore(Char()),
        ZeroOrMore(
          TestNot(Field(n)),
          ".",
          OneOrMore(Char())
        )
      ), push(searcher.term(match())),
      FirstOf(//compact this TODO
        WhiteSpace(),
        Test(")"),
        Test(EOI),
        Test("."),
        Test("[")
      )
    );
  }

  public Rule Phrase() {
    return Sequence(
      FirstOf("\"", "'"),
      OneOrMore(
        NoneOf("\"'")
      ), push(searcher.phrase(match())),
      FirstOf("\"","'"),
      OptionalWhiteSpace()
    );
  }

  public Rule Parens() {
    return Sequence("(", OptionalWhiteSpace(), OrExpression(), push(searcher.wrap(pop())), OptionalWhiteSpace(), ")", ZeroOrMore("!"), OptionalWhiteSpace());
  }

  
 
  public Rule Range() {
    StringVar field = new StringVar();
    StringVar inequality = new StringVar();
    return Sequence(
      "@",
      OneOrMore(WordChar()),field.set(match()),
      OptionalWhiteSpace(),
      RangeOp(),inequality.set(match()),
      OptionalWhiteSpace(),
      ZeroOrMore("\""),
      OneOrMore(Char()),push(searcher.range(field.get(), inequality.get(), match())),
      ZeroOrMore("\""),
      FirstOf(
        WhiteSpace(),
        Test("("),
        Test(")"),
        Test(EOI)
      )
    );
  }

  public Rule BoundRange() {
    StringVar field = new StringVar();
    StringVar inequality1 = new StringVar();
    StringVar inequality2 = new StringVar();
    StringVar match1 = new StringVar();
    return Sequence(
      "@",
      OneOrMore(WordChar()),field.set(match()),
      OptionalWhiteSpace(),
      RangeOp(),inequality1.set(match()),
      OptionalWhiteSpace(),
      ZeroOrMore("\""),
      OneOrMore(Char()),match1.set(match()),
      ZeroOrMore("\""),
      OptionalWhiteSpace(),
      RangeOp(),inequality2.set(match()),
      OptionalWhiteSpace(),
      ZeroOrMore("\""),
      OneOrMore(Char()),push(searcher.boundRange(field.get(), inequality1.get(), match1.get(), inequality2.get(), match())),
      ZeroOrMore("\""),
      FirstOf(
        WhiteSpace(),
        Test("("),
        Test(")"),
        Test(EOI)
      )
    );
  }

  //////////////////////////////////////////////////

  public Rule Char() {
    //TODO consider using NoneOf() so that we can capture UNICODE chars
    return FirstOf(
      WordChar(),
      AnyOf("/,*?$")
    );
  }

  public Rule WordChar() {
    return AnyOf("0123456789" +
      "abcdefghijklmnopqrstuvwxyz" +
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
      "-_"
    );
  }
  public Rule RangeOp() {
    return OneOrMore(AnyOf("<>="));
  }

  public Rule ClassOp() {
    return OneOrMore(AnyOf(",-"));
  }

  public Rule ClassSeparator() {
    return OneOrMore(AnyOf("/"));
  }
  
  public Rule FloatChar(){
      return Sequence(
              OneOrMore(AnyOf("0123456789")),
              ZeroOrMore(
                      ".",
                      OneOrMore(AnyOf("0123456789"))
                  )
              );
  }

  public Rule WordCharNoDashes(){
	  return OneOrMore(AnyOf("0123456789."+
		      "abcdefghijklmnopqrstuvwxyz" +
		      "ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
  }
  
  //@SuppressNode
  public Rule OptionalDigits() {
    return ZeroOrMore(CharRange('0', '9'));
  }

  Rule WhiteSpace() {
    return OneOrMore(AnyOf(" \t\f"));
  }

  Rule OptionalWhiteSpace() {
    return ZeroOrMore(AnyOf(" \t\f"));
  }


  //////////////////////////////////////////////////
  public Rule OR() {
    return Sequence(IgnoreCase("OR"), CharAfterOp());
  }
  public Rule OR_SYMBOL() {
    return Sequence("|", OptionalWhiteSpace());
  }
  public Rule XOR() {
    return Sequence(IgnoreCase("XOR"), CharAfterOp());
  }
  public Rule	AND() {
    return Sequence(IgnoreCase("AND"), CharAfterOp());
  }
  public Rule	AND_SYMBOL() {
    return Sequence("&", OptionalWhiteSpace());
  }
  public Rule SAME(Var<Integer> n) {
    return Sequence(IgnoreCase("SAME"), OptionalDigits(), n.set(Integer.parseInt(matchOrDefault("1"))), CharAfterOp());
  }
  public Rule WITH(Var<Integer> n) {
    return Sequence(IgnoreCase("WITH"), OptionalDigits(), n.set(Integer.parseInt(matchOrDefault("1"))), CharAfterOp());
  }
  public Rule NEAR(Var<Integer> n) {
    return Sequence(IgnoreCase("NEAR"), OptionalDigits(), n.set(Integer.parseInt(matchOrDefault("1"))), CharAfterOp());
  }
  public Rule ADJ(Var<Integer> n) {
    return Sequence(IgnoreCase("ADJ"), OptionalDigits(), n.set(Integer.parseInt(matchOrDefault("1"))), CharAfterOp());
  }
  public Rule NOT() {
    return Sequence(IgnoreCase("NOT"), WhiteSpace());
  }
  public Rule CharAfterOp() {
    //TODO there are a couple of these rules that contain EOI - I feel like that's an anti pattern
    return FirstOf(
      WhiteSpace(),
      Test("("),
      Test(EOI)
    );
  }
  public Rule DEFAULT_OPERATOR() {
    return TestNot(FirstOf(
      OR(),
      XOR(),
      AND(),
      SAME(new Var<Integer>()),
      WITH(new Var<Integer>()),
      NEAR(new Var<Integer>()),
      ADJ(new Var<Integer>())
      //NOT() <-- should never be included in DEFAULT_OPERATOR b/c a unary operator should never be default - ALSO it causes an infinite _parser loop
    )
    );
  }



  //////////////////////////////////////////////////
  protected ISwanSearcher<V> searcher;
  public void setSearcher( ISwanSearcher<V> searcher ) {
    this.searcher = searcher;
  }

////////////////////////////////////////////////////
//// Keep this here for now. It's useful in prototyping and quickly checking changes
////////////////////////////////////////////////////
//	public static void main(String[] args) {
//		SwanParser<String> _parser = Parboiled.createParser(SwanParser.class);
//		_parser.setSearcher(new StringStubSwanSearcher());
//
//		String input = "apple AND banana NEAR3 cocon*";
//
//		ParsingResult<?> result = new RecoveringParseRunner<String>(
//				_parser.Query()).run(input);
//
//		if (result.hasErrors()) {
//			System.out.println("\nParse Errors:\n" + printParseErrors(result));
//		}
//
//		// Object value = result.parseTreeRoot.getValue();
//		// System.out.println(value);
//
//		System.out.println(printNodeTree(result));
//	}
}
