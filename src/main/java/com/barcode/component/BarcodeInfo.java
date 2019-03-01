package com.barcode.component;

public class BarcodeInfo {

	public BarcodeInfo(String text, String format) {
		this.text = text;
		this.format = format;
	}
	
    private  String text;
    private  String format;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	@Override
	public String toString() {
		return "BarcodeInfo [text=" + text + ", format=" + format + "]";
	}
	
}
