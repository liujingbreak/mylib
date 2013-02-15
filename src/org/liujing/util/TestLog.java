package org.liujing.util;

import java.util.*;
import java.util.logging.*;
import org.liujing.util.logging.*;

public class TestLog
{
	private static Logger log=Logger.getLogger(TestLog.class.getName());
	
	public static void main(String[] args)
	{
		
		Logger.getLogger("org").addHandler(new MyLogHandler());
		Logger.getLogger("org").setLevel(Level.ALL);
		log.finest("here------------------------------- 112121212121");
		log.finer("here------------------------------- 112121212121");
		log.fine("here------------------------------- 112121212121");
		log.config("here------------------------------- 112121212121");
		log.info("here------------------------------- 112121212121");
		log.warning("here------------------------------- 112121212121");
		log.log(Level.SEVERE,"here------------------------------- 112121212121",new Exception("test error"));
		for(int i=0;i<50;i++)
			log.info("FINE	2008-10-16 15:58:42.552 T-11 restlettest.SoapApi.issueSOAPRequest() <SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body><pws:RetrieveMycompanyRequest xmlns:pws=\"http://servicecenter.peregrine.com/PWS\"><pws:model><pws:keys query=\"\"/><pws:instance/></pws:model></pws:RetrieveMycompanyRequest></SOAP-ENV:Body></SOAP-ENV:Envelope>");
	}
	
	
}
