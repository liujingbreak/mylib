package liujing;

import org.liujing.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.logging.*;
import java.io.*;

public class BufferModifierTest{
	static Logger log = Logger.getLogger(BufferModifierTest.class.getName());
	@Test
	public void testStringModifier()throws Exception{
		String t = "0123456789";
		BufferModifier bean = new BufferModifier();
		bean.deleteText(3,3);
		bean.replaceText(8,2," REPLACE ");
		bean.insertText(2," INSERT ");
		BufferModifier.StringModifier m = new BufferModifier.StringModifier(t);
		bean.process(m);
		log.info("\""+ m.getResult() +"\"");
		assertEquals("01 INSERT 267 REPLACE ", m.getResult());
	}
	
	@Test
	public void testStringModifier2()throws Exception{
		log.info("-----------------------------");
		String t = "0123456789\n0123456789\n";
		BufferModifier bean = new BufferModifier();
		bean.deleteText(2,4,3);
		bean.replaceText(2,9,2," REPLACE ");
		bean.insertText(2,3," INSERT ");
		BufferModifier.StringModifier m = new BufferModifier.StringModifier(t);
		bean.process(m);
		log.info("\""+ m.getResult() +"\"");
		assertEquals("0123456789\n01 INSERT 267 REPLACE \n", m.getResult());
	}
}