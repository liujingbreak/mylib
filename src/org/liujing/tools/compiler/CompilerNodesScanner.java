package org.liujing.tools.compiler;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.text.*;
import java.util.regex.*;

public class CompilerNodesScanner{
	private static Logger log = Logger.getLogger(CompilerNodesScanner.class.getName());
	private Set<String> bnfNames = new HashSet();
	private Writer output;
	public CompilerNodesScanner(){}
	
	public String drawTree(LLLAProduction root)throws IOException{
		output = new StringWriter();
		output.write("---------------tree--start-------------\r\n");
		drawNode(root,1);
		return output.toString();
	}
	
	protected void drawNode(LLLAProduction node,int level)throws IOException{
		if(node instanceof TokenPattern){
			TokenPattern n = (TokenPattern)node;
			for(int i=0,l=level-1;i<l;i++)
				output.write(" |  ");
			output.write(" |- ");
			output.write("<"+n.name+">\r\n");
		}else if(node instanceof Keyword){
			Keyword n = (Keyword)node;
			for(int i=0,l=level-1;i<l;i++)
				output.write(" |  ");
			output.write(" |- ");
			output.write("\""+(n.word==null?"<EOF>":n.word)+"\"\r\n");
		}		
		if(node instanceof AnotationTranslater._Expansion){
			AnotationTranslater._Expansion _expansion = (AnotationTranslater._Expansion)node;
			String name = _expansion.getName();
			
			for(int i=0,l=level-1;i<l;i++)
				output.write(" |  ");
			output.write(" |- ");
			output.write(name+"(){{{ la: "+_expansion.lookAheadNum(null)+"\r\n");
			if(!bnfNames.contains(name)){
				bnfNames.add(name);			
				drawNode(_expansion.getUnits().get(0),level+1);
			}
			for(int i=0,l=level-1;i<l;i++)
				output.write(" |  ");
			output.write(" |  ");
			output.write("}}}\r\n");
		}
		else if(node instanceof Expansion){
			for(int i=0,l=level-1;i<l;i++)
				output.write(" |  ");
			output.write(" |-<"+node.hashCode()+">la: "+node.lookAheadNum(null)+"\r\n");
			++level;
			int i=0;
			for(LLLAProduction unit:((Expansion)node).getUnits()){
				//if(i==0)
				//	drawNode(unit,1);
				//else
					drawNode(unit,level);
				i++;
			}
		}else if(node instanceof ExpansionChoices){
			int newLevel = level+1;
			for(int i=0,l=level-1;i<l;i++)
				output.write(" |  ");
			output.write(" |- choice<"+node.hashCode());
			//output.write("");
			output.write("> :{{{ la: "+node.lookAheadNum(null)+"\r\n");
			for(LLLAProduction unit:((ExpansionChoices)node).getChoices()){
				//if(i!=0){
				//	for(int i=0;i<level;i++)
				//		write("    ");
				//	output.write("|\r\n");
				//}
				drawNode(unit,newLevel);
			}
			for(int i=0,l=level-1;i<l;i++)
				output.write(" |  ");
			output.write(" |  ");
			output.write("}}}\r\n");
		}else if(node instanceof ExpansionMore){
			for(int i=0,l=level-1;i<l;i++)
				output.write(" |  ");
			output.write(" |- more<"+node.hashCode());
			//output.write(node.hashCode());
			output.write(">: {{{la: "+node.lookAheadNum(null)+"\r\n");
			ExpansionMore n = (ExpansionMore)node;
			drawNode(n.unit,level+1);
			for(int i=0,l=level-1;i<l;i++)
				output.write(" |  ");
			output.write(" |  ");
			output.write("}}}\r\n");
		}else if(node instanceof Add){
			Add n = (Add)node;
			drawNode(n.getProduction(),level);
		}else if(node instanceof Assign){
			Assign n = (Assign)node;
			drawNode(n.getProduction(),level);
		}
	}
}
