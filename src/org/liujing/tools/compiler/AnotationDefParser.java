package org.liujing.tools.compiler;

import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;
import java.util.regex.Pattern;
import java.io.*;

public class AnotationDefParser{
	private static Logger log = Logger.getLogger(AnotationDefParser.class.getName());
	private LLLACompiler compiler;
	private Expansion bnfRoot;
	private AnotationTranslater translater;
	private List<String> errorMessages = new ArrayList();
	protected Set<String> assginedVariables = new HashSet<String>();
	protected Set<String> addedVariables = new HashSet<String>();
	
	public AnotationDefParser(AnotationTranslater translater){
		this.translater = translater;
		
		
		compiler = new LLLACompiler();
		bnfRoot = new BnfRoot();
		BnfExpChoices bnfExpChoices = new BnfExpChoices();
		BnfExpansion bnfExpansion = new BnfExpansion();
		BnfLocalLookAhead bnfLocalLookAhead = new BnfLocalLookAhead();
		BnfExpansionUnit bnfExpUnit = new BnfExpansionUnit();
		Expansion choices2 = new BnfExpansionChoices2();
		Expansion choices3 = new BnfExpansionChoices3();
		ComplexUnit complexUnit = new ComplexUnit();
		SubBnfCall subBnf = new SubBnfCall();
		RegExpRef regExpRef = new RegExpRef();
		
		TokenPattern integerLiteral = compiler.defineToken("[0-9]+");
		TokenPattern identifier = compiler.defineToken("[a-zA-Z_$][a-zA-Z_0-9$]*");
		TokenPattern quoteStr = compiler.defineToken("'([^'\\\\\\n\\r]|(\\\\([ntbrf\\\\'\"]|[0-7]([0-7])?)))*'");
		TokenPattern blank = compiler.defineToken(" |\\n|\\r|\\t|\\f",Pattern.MULTILINE);
		TokenPattern repeatSymbol = compiler.defineToken("[*+?]");
		
		compiler.setSkip(blank);
		bnfRoot.addUnit(new Assign("root",bnfExpChoices)).addUnit(LLLACompiler.EOF);
		
		// ------------------------------------------------------
		//-- expansion_choices ::= expansion ( "|" expansion )* 
		bnfExpChoices.addUnit(new Add("choice",bnfExpansion));		
		bnfExpChoices.addUnit(new ExpansionMore(ExpansionMore.TYPE_STAR,new Expansion().addUnit("|").addUnit(new Add("choice",bnfExpansion))));
		// ------------------------------------------------------
		//-- expansion ::= ( "LOOKAHEAD" "(" local_lookahead ")" )? ( expansion_unit )+ 
		Expansion lookAheadClause = new Expansion().addUnit(new ExpansionMore(new Assign("negative","!"))).addUnit("LOOKAHEAD").addUnit("(").addUnit(bnfLocalLookAhead).addUnit(")");
		bnfExpansion.addUnit(new ExpansionMore(lookAheadClause));
		bnfExpansion.addUnit(new ExpansionMore(ExpansionMore.TYPE_PLUS,bnfExpUnit));
		// ------------------------------------------------------
		//local_lookahead ::= ( IntegerLiteral )? ( "," )? ( expansion_choices )? 
		bnfLocalLookAhead.addUnit(new ExpansionMore(new Assign("laNum",integerLiteral)));
		bnfLocalLookAhead.addUnit(new ExpansionMore(","));
		bnfLocalLookAhead.addUnit(new ExpansionMore(new Assign("laProd",bnfExpChoices)));
		// ------------------------------------------------------
		// expansion_unit ::= "LOOKAHEAD" "(" local_lookahead ")" 
		// 				 | Block 
		// 				 | "[" expansion_choices "]" 
		// 				 | "try" "{" expansion_choices "}" ( "catch" "(" Name <IDENTIFIER> ")" Block )* ( "finally" Block )? 
		// 				 | ( PrimaryExpression "=" )? ( identifier Arguments | regular_expression ( "." <IDENTIFIER> )? ) 
		// 				 | "(" expansion_choices ")" ( "+" | "*" | "?" )? 
		bnfExpUnit.addChoice(lookAheadClause);
		bnfExpUnit.addChoice(new Add("units",choices2));
		bnfExpUnit.addChoice(new Add("units",complexUnit));
		bnfExpUnit.addChoice(new Add("units",choices3));
		choices2.addUnit("[").addUnit(new Assign("bnfExpChoices",bnfExpChoices)).addUnit("]");
		complexUnit.addUnit(new ExpansionMore(new Expansion()
			.addUnit(new Assign("variable",identifier))
			.addUnit(new ExpansionChoices()
					.addChoice(new Assign("operator","="))
					.addChoice(new Assign("operator","ADD"))
				).setLookAhead(2)
			));
		complexUnit.addUnit(new ExpansionChoices()
			.addChoice(new Assign("bnf",subBnf))
			.addChoice(new Assign("str",quoteStr))
			.addChoice(new Assign("tokenPattern",regExpRef)) );
		subBnf.addUnit(new Assign("bnf",identifier)).addUnit("(").addUnit(")");
		regExpRef.addUnit("<").addUnit(new Assign("tokenPattern",identifier)).addUnit(">");
		choices3.addUnit("(").addUnit(new Assign("bnfExpChoices",bnfExpChoices)).addUnit(")").addUnit(new ExpansionMore(new Assign("repeat",repeatSymbol)));
		
		//try{
		//	StringWriter sw = new StringWriter();
		//	bnfRoot.writeTo(sw);
		//	log.fine(sw.toString());
		//}catch(Exception e){
		//	log.log(Level.SEVERE,"",e);
		//}
	}
	
	public LLLAProduction parse(AnotationTranslater.BNF bnf)throws LLLAParserException{
		compiler.reInit(bnf.bnfStr);
		log.fine("parse starts method:"+bnf.method.getName());
		LLLAProduction p = (LLLAProduction)compiler.parse(bnfRoot);
		//p = translater.wrapProduction(bnf,p,assginedVariables,addedVariables);
		
		return p;
	}
	
	
	
	protected class BnfRoot extends Expansion{
		@Override
		public void before(LLLACompiler compiler)throws LLLAParserException{
			compiler.declareVariable("root");
		}
		@Override
		public Object after(LLLACompiler compiler)throws LLLAParserException{
			LLLAProduction p = (LLLAProduction)compiler.getVariable("root");
			log.fine("root = "+ p);
			
			return p;
		}
	}
	
	protected class BnfExpChoices extends Expansion{
		@Override
		public void before(LLLACompiler compiler)throws LLLAParserException{
			compiler.declareVariable("choice",new ArrayList());
		}
		@Override
		public Object after(LLLACompiler compiler)throws LLLAParserException{
			
			List choiceList = (List)compiler.getVariable("choice");
			if(choiceList.size()>1){
				ExpansionChoices choices = new ExpansionChoices();
				Iterator it = choiceList.iterator();
				while(it.hasNext()){
					Object choice = it.next();
					if(choice instanceof String)
						choices.addChoice((String)choice);
					else
						choices.addChoice((LLLAProduction)choice);
				}
				return choices;
			}else{
				log.fine("1 choice "+choiceList.get(0));
				return choiceList.get(0);
			}
		}
	}
	
	protected class BnfExpansion extends Expansion{
		@Override
		public void before(LLLACompiler compiler)throws LLLAParserException{
			compiler.declareVariable("units",new ArrayList());
			compiler.declareVariable("negative");
			compiler.declareVariable("laNum");
			compiler.declareVariable("laProd");
		}
		
		@Override
		public Object after(LLLACompiler compiler)throws LLLAParserException{
			LLLAProduction p = null;
			List units = (List)compiler.getVariable("units");
			if(units.size()>1){
				Expansion exp = new Expansion();
				Iterator it = units.iterator();
				while(it.hasNext()){
					LLLAProduction unit = (LLLAProduction)it.next();
					log.fine("add unit:"+unit);
					exp.addUnit(unit);
				}
				p = exp;
			}else{
				p = (LLLAProduction)units.get(0);
			}
			LLLAProduction lookAheadPd = (LLLAProduction)compiler.getVariable("laProd");
			boolean isNegative = compiler.getVariable("negative")!=null;
			if(lookAheadPd!=null){				
				log.fine("set lookahead = "+lookAheadPd);
				if(isNegative){
					p.setNegativeLookAhead(lookAheadPd);
				}else{
					p.setLookAhead(lookAheadPd);
				}
			}else{
				Token lookAheadNumTk = (Token)compiler.getVariable("laNum");
				if(lookAheadNumTk!=null){
					int num = Integer.parseInt(lookAheadNumTk.image);
					log.fine("set lookahead = "+num);
					p.setLookAhead(num);
				}
			}
			return p;
		}
	}
	
	protected class BnfLocalLookAhead extends Expansion{
		@Override
		public Object after(LLLACompiler compiler)throws LLLAParserException{
			return super.after(compiler);
		}
	}
	
	protected class BnfExpansionUnit extends ExpansionChoices{
		@Override
		public void before(LLLACompiler compiler)throws LLLAParserException{
		}
		
		@Override
		public Object after(LLLACompiler compiler)throws LLLAParserException{
			//LLLAProduction p = (LLLAProduction)compiler.getVariable("unit");
			//log.fine("return "+p);
			return null;
		}
	}
	
	/**
	"[" expansion_choices "]" 
	*/
	protected class BnfExpansionChoices2 extends Expansion{
		@Override
		public void before(LLLACompiler parser)throws LLLAParserException{
			parser.declareVariable("bnfExpChoices");
		}
		public Object after(LLLACompiler compiler)throws LLLAParserException{
			Object v = compiler.getVariable("bnfExpChoices");
			if(v instanceof String)
				return new ExpansionMore((String)v);
			else
				return new ExpansionMore((LLLAProduction)v);
		}
	}
	
	/**
	"(" expansion_choices ")" ( "+" | "*" | "?" )? 
	*/
	protected class BnfExpansionChoices3 extends Expansion{
		@Override
		public void before(LLLACompiler parser)throws LLLAParserException{
			parser.declareVariable("repeat");
			parser.declareVariable("bnfExpChoices");
		}
		@Override
		public Object after(LLLACompiler compiler)throws LLLAParserException{
			Token symbol = (Token)compiler.getVariable("repeat");
			if(symbol== null){
				log.fine("match (...)");
				return compiler.getVariable("bnfExpChoices");
			}else{
				log.fine("match (...)"+symbol.image);
				Object v = compiler.getVariable("bnfExpChoices");
				short type = 0;
				if("+".equals(symbol.image)){
					type = ExpansionMore.TYPE_PLUS;					
				}else if("*".equals(symbol.image)){
					type = ExpansionMore.TYPE_STAR;
				}else if("?".equals(symbol.image)){
					type = ExpansionMore.TYPE_DEFAULT;
				}
				if(v instanceof String)
					return new ExpansionMore(type,(String)v);
				else
					return new ExpansionMore(type,(LLLAProduction)v);
			}
		}
	}
	
	//}
	
	protected class ComplexUnit extends Expansion{
		@Override
		public void before(LLLACompiler compiler)throws LLLAParserException{
			compiler.declareVariable("bnf");
			compiler.declareVariable("tokenPattern");
			compiler.declareVariable("str");
			compiler.declareVariable("operator");
			compiler.declareVariable("variable");
		}
		@Override
		public Object after(LLLACompiler compiler)throws LLLAParserException{
			LLLAProduction production = (LLLAProduction)compiler.getVariable("tokenPattern");
			if(production==null){
				Token keyword = (Token)compiler.getVariable("str");
				if(keyword!=null){
					production = new Keyword(keyword.image.substring(1,keyword.image.length()-1));
				}else{
					production = (LLLAProduction)compiler.getVariable("bnf");
				}
			}
			Token vNameToken = (Token)compiler.getVariable("variable");
			if(vNameToken!=null){
				Token opToken = (Token)compiler.getVariable("operator");
				if(opToken.image.equals("=")){
					production = new Assign(vNameToken.image,production);
					assginedVariables.add(vNameToken.image);
				}else if(opToken.image.equals("ADD")){					
					production = new Add(vNameToken.image,production);
					addedVariables.add(vNameToken.image);
				}
				log.fine("assign BNF variable: " +vNameToken.image);
			}			
			return production;
		}
	}
	
	protected class SubBnfCall extends Expansion{
		@Override
		public void before(LLLACompiler compiler)throws LLLAParserException{
			compiler.declareVariable("bnf");
		}
		@Override
		public Object after(LLLACompiler compiler)throws LLLAParserException{
			Token t = (Token)compiler.getVariable("bnf");
			log.fine("sub BNF: "+t.image);
			AnotationTranslater.BNF bnf = translater.bnfs.get(t.image);
			if(bnf == null){
				LLLAProduction prod = translater.custProds.get(t.image);
				if(prod!=null){
					return prod;
				}else{
					throw new LLLAParserException("BNF "+ t.image+" is not defined");
				}
			}
			if(bnf.prod == null){
				translater.createProductions(bnf);
			}
			return bnf.prod;
		}
	}
	
	protected class RegExpRef extends Expansion{
		@Override
		public void before(LLLACompiler compiler)throws LLLAParserException{
			compiler.declareVariable("tokenPattern");
		}
		@Override
		public Object after(LLLACompiler compiler)throws LLLAParserException{
			Token token = (Token)compiler.getVariable("tokenPattern");
			log.fine("match <"+token+">");
			if("EOF".equals(token.image)){
				return LLLACompiler.EOF;
			}
			TokenPattern tkPattern = translater.regs.get(token.image);
			if(tkPattern == null){
				addCompileError("token pattern:<"+token.image+"> is not defined. Position:"+token.getStartPos());
			}
			return tkPattern;
		}
	}
	
	private void addCompileError(String msg){
		log.warning(msg);
		errorMessages.add(msg);
	}
}
