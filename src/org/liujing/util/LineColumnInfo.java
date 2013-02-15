package org.liujing.util;

import java.io.*;
import java.util.logging.*;
import java.util.*;

public interface LineColumnInfo{
	public int getLine();
	public int getColumn();
	public void setOffset(int offset);
}
