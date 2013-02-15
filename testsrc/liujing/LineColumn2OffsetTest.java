package liujing;

import java.util.*;
import java.io.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.liujing.util.*;

public class LineColumn2OffsetTest{
	Reader target;
	public LineColumn2OffsetTest(){
	}
	@Before
	public void setup(){
		target = new BufferedReader(new InputStreamReader(
			LineColumn2OffsetTest.class.getResourceAsStream("LineColumn2Offset.txt")));
	}
	
	@Test
	public void test()throws Exception{
		LineColumn2Offset bean = new LineColumn2Offset(target);
		LineColumn2Offset.LCLocation[] testLocations = new LineColumn2Offset.LCLocation[]{
			new LineColumn2Offset.LCLocation(5,1),
			new LineColumn2Offset.LCLocation(1,3),
			new LineColumn2Offset.LCLocation(2,3),
			new LineColumn2Offset.LCLocation(2,2)
		};
		for(LineColumn2Offset.LCLocation loc : testLocations){
			
			bean.addLocation(loc);
		}
		bean.convert();
		assertEquals(20,testLocations[0].getOffset());
		assertEquals(2,testLocations[1].getOffset());
		assertEquals(7,testLocations[2].getOffset());
		assertEquals(6,testLocations[3].getOffset());
	}
}