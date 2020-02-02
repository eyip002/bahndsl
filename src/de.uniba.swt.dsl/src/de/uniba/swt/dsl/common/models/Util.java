package de.uniba.swt.dsl.common.models;

public class Util {
	
	public static String SINGLE_INDENT = "  ";
	
	public static String LINE_BREAK = "\n";
	
	public static String toHexString(long value) {
		return String.format("0x%04X", value);
	}
	
	public static String increaseIndent(String value) {
		return value + SINGLE_INDENT;
	}
	
	public static StringBuilder appendLine(StringBuilder builder) {
		return builder.append(Util.LINE_BREAK);
	}

	public static Long parseHex(String strValue) throws Exception {
		try	{
			return Long.parseLong(strValue.substring(2), 16);
		} catch (NumberFormatException ex) {
			throw new Exception("Invalid hex value: " + strValue);
		}
	}
}