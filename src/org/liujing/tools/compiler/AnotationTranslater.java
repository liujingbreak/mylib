package org.liujing.tools.compiler;

import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;
import java.util.regex.Pattern;
import java.io.*;
import java.lang.annotation.*;

public class AnotationTranslater{
	private Logger log = Logger.getLogger(AnotationTranslater.class.getName());
	protected LLLACompiler compiler;
	protected HashMap<String,TokenPattern> regs = new HashMap();
	protected HashMap<String,BNF> bnfs = new HashMap();
	protected HashMap<String,LLLAProduction> custProds = new HashMap();
	protected Method rootMethod = null;
	protected BNF rootBnf;
	Object target;
	LLLAProduction prodCompiled;
	
	public AnotationTranslater(Object anototationObj)throws Exception{
		compiler = new LLLACompiler();
		prodCompiled = translate(anototationObj);
	}

	public AnotationTranslater(LLLACompiler compiler){
		this.compiler = compiler;
	}
	
	public void parse(CharSequence text)throws LLLAParserException{
		try{
			compiler.setSlient(false);
			compiler.reInit(text);
			compiler.parse(prodCompiled);
		}catch(LLLAParserException pe){
			if(compiler.lastExpansionMoreFail!=null){
				log.warning("last parse failed @ "+compiler.lastExpansionMoreFail.getPosition());
				log.warning("this parse error @ "+pe.getPosition());
				log.log(Level.WARNING,"Possible cause is ",compiler.lastExpansionMoreFail);
			}
			throw pe;
		}
	}
	
	public LLLAProduction translate(Object atProduction)throws Exception{
		target = atProduction;
		Class cls = atProduction.getClass();
		Field[] fields = cls.getFields();
		for(int i=0;i<fields.length;i++){
			Field field = fields[i];
			RegularExpDef regDef = field.getAnnotation(RegularExpDef.class);
			if(regDef!=null){
				String name = null;
				
				if(regDef.name()!=null)
					name = regDef.name();
				else
					name = field.getName();
				//log.fine("Pattern define:"+name);
				TokenPattern tp = compiler.defineToken((Pattern)field.get(atProduction));
				tp.setName(name);
				regs.put(name,tp);
			}
		}
		//regs.put("EOF",LLLACompiler.EOF);
		//Method rootMethod = null;
		Method[] methods = cls.getMethods();
		BNF skipBnf = null;
		StringBuilder buf = new StringBuilder();
		for(Method method:methods){
			BnfMethod bnf = method.getAnnotation(BnfMethod.class);
			RootBnf root = method.getAnnotation(RootBnf.class);
			SkipBnf skip = method.getAnnotation(SkipBnf.class);
			Customized cp = method.getAnnotation(Customized.class);
			if(bnf!=null){
				String[] bnfStrs = bnf.value();
				buf.setLength(0);
				for(String subStr:bnfStrs){
					buf.append(subStr);
				}
				bnfs.put(method.getName(),new BNF(buf.toString(),method,null));
				//bnfMethods.put(method.getName(),method);
			}
			if(root!=null){
				String[] bnfStrs = root.value();
				buf.setLength(0);
				for(String subStr:bnfStrs){
					buf.append(subStr);
				}
				rootBnf = new BNF(buf.toString(),method,null);
				bnfs.put(method.getName(),rootBnf);
			}
			if(skip!=null){
				String[] bnfStrs = skip.value();
				buf.setLength(0);
				for(String subStr:bnfStrs){
					buf.append(subStr);
				}
				skipBnf = new BNF(buf.toString(),method,null);
				
			}
			if(cp!=null){
				custProds.put(method.getName(),new CustomizedProduction(method,target));
			}
		}
		
		LLLAProduction rootProd = createProductions(rootBnf);
		compiler.setSkip(createProductions(skipBnf));
		CompilerNodesScanner scanner = new CompilerNodesScanner();
		//log.fine("------------tree-------------");
		if(log.isLoggable(Level.FINEST))
			log.finest(scanner.drawTree(rootProd));
		//log.fine("-----------------------------");
		return rootProd;
	}
	
	protected LLLAProduction createProductions(BNF bnf)throws LLLAParserException{
		_Expansion wrapper = new _Expansion(bnf.method,target);
		bnf.prod = wrapper;
		AnotationDefParser adp = new AnotationDefParser(this);
		
		LLLAProduction p = adp.parse(bnf);
		wrapper.addUnit(p);
		wrapper.setDeclareVariables(adp.assginedVariables,adp.addedVariables);
		return wrapper;
	}
	
	
	
	public static class BNF{
		public String bnfStr;
		public Method method;
		public LLLAProduction prod;
		public BNF(String bnf,Method m,LLLAProduction p){
			bnfStr = bnf;
			method = m;
			prod = p;
		}
	}
	
	protected class CustomizedProduction extends LLLAProduction{
		private Method method;
		private Object target;
		
		public CustomizedProduction(Method method,Object targetObj){
			this.method = method;
			target = targetObj;
		}
		
		public Object consume(LLLACompiler compiler, int[] limit)throws LLLAParserException{
			Class<?>[] types = method.getParameterTypes();
			Object[] arguments = new Object[types.length];
			for(int i=0;i<types.length;i++){
				if(types[i] == LLLACompiler.class){
					arguments[i] = compiler;
				}else if(types[i] == int[].class){
					arguments[i] = limit;
				}else if(types[i].isAssignableFrom(Map.class)){
					arguments[i] = regs;
				}else if(types[i] == boolean.class){
					arguments[i] = compiler.evaluating;
				}
				else{
					arguments[i] = null;
				}
			}
			try{
				return method.invoke(target,arguments);
			}catch(IllegalAccessException ae){
				throw new LLLAParserException(ae.getMessage());
			}catch(InvocationTargetException ie){
				throw new LLLAParserException(ie.getMessage());
			}
		}
	}
	private static int recursionLevel = 0;
	public class _Expansion extends Expansion{
		private String name;
		private Method method;
		private Object userDefinedObj;
		private Set<String> assginedVariables;
		private Set<String> addedVariables;
		
		
		public _Expansion(Method m,Object userDefinedObj){
			this.name = m.getName();
			super.setName(name);
			this.method = m;
			this.userDefinedObj = userDefinedObj;
			//Annotation[][] annotations = method.getParameterAnnotations();
			
		}
		
		public String getName(){
			return name;
		}
		
		public void setDeclareVariables(Set<String> assginedVariables,Set<String> addedVariables){
			this.assginedVariables = assginedVariables;
			this.addedVariables = addedVariables;
		}
		
		@Override
		public void before(LLLACompiler parser)throws LLLAParserException{
			if(log.isLoggable(Level.FINER))
				log.finer("pos "+parser.token.endPos+ " in BNF "+name);
			Annotation[][] annotations = method.getParameterAnnotations();
			if(annotations.length<1){
				throw new LLLAParserException("BNF method must contain at least 1 argument of type LLLACompiler");
			}
			for(int i = 0;i<annotations.length;i++){
				for(Annotation an:annotations[i]){
					if(an instanceof Variable){
						Variable vb = (Variable)an;
						compiler.declareVariable(vb.name());
					}
				}
			}
		}
		@Override
		public Object consume(LLLACompiler compiler, int[] limit)throws LLLAParserException{
			StringBuilder buf = null;
			int level = recursionLevel;
			try{
				
				
				if(log.isLoggable(Level.FINEST) && !compiler.isSlient()){
				buf = new StringBuilder();
					for(int i = 0;i<level;i++)
						buf.append(" ");
					buf.append("+++");
					buf.append(compiler.evaluating?"ev":"co");
					buf.append(" Lv");
					buf.append(compiler.evaLevel);
					
					buf.append(" @");
					buf.append(compiler.token.endPos);
					buf.append(" limit=");
					buf.append(String.valueOf(limit[0]));
					buf.append(" [");
					buf.append(name);
					buf.append("] after tk ");
					buf.append(compiler.token.getImage());
					
					log.finest(buf.toString());
				}
				recursionLevel++;
				Object result = super.consume(compiler,limit);
				//if(log.isLoggable(Level.FINEST) && !compiler.isSlient()){
				//	buf.setLength(0);
				//	for(int i = 0;i<level;i++)
				//		buf.append(" ");
				//	buf.append("---");
				//	buf.append(compiler.evaluating?"ev":"co");
				//	buf.append("L");
				//	buf.append(level);
				//	//
				//	//
				//	buf.append(" @");
				//	buf.append(compiler.token.endPos);
				//	buf.append(" [");
				//	buf.append(name);
				//	buf.append("] ");
				//	buf.append(compiler.token.getImage());
				//	
				//	log.finest(buf.toString());
				//}
				return result;
			}catch(LLLAParserException pe){
				
				if(!(compiler.skipping|| compiler.evaluating))
					log.severe("Parse failed in BNF: "+name);
				throw pe;
			}finally{
				recursionLevel--;
				
			}
		}
		@Override
		public Object after(LLLACompiler parser)throws LLLAParserException{
			try{
				
				Annotation[][] annotations = method.getParameterAnnotations();
				Object[] arguments = new Object[annotations.length];
				if(annotations.length<1){
					throw new LLLAParserException("BNF method must contain at least 1 argument of type LLLACompiler");
				}
				arguments[0] = parser;
				for(int i = 0;i<annotations.length;i++){
					for(Annotation an:annotations[i]){
						if(an instanceof Variable){
							Variable vb = (Variable)an;
							//log.info("parameter:"+vb.value());
							Object obj = parser.getVariable(vb.name());
							//obj.getClass().cast(obj);
							arguments[i] = obj;
						}
					}
				}
				//log.info("len of param ="+arguments.length);
				return method.invoke(userDefinedObj,arguments);
			}catch(IllegalAccessException ae){
				log.log(Level.SEVERE,"",ae);
				throw new LLLAParserException("BNFMethod: "+method.getName()+" "+ae.getMessage());
			}catch(InvocationTargetException ie){
				log.log(Level.SEVERE,"",ie);
				throw new LLLAParserException("BNFMethod: "+method.getName()+" "+ie.getMessage());
			}catch(IllegalArgumentException are){
				log.log(Level.SEVERE,"",are);
				throw new LLLAParserException("BNFMethod: "+method.getName()+" "+are.getMessage());
			}
		}
	}
}
