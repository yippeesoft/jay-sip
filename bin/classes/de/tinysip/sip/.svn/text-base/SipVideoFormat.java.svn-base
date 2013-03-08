/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian Rösch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.sip;

/**
 * Contains the information about a supported video format.
 * 
 * @author Sebastian
 * 
 */
public class SipVideoFormat {
	private int format;
	private String formatName;
	private int sampleRate;

	/**
	 * Create a SipVideoFormat which is supported by the application. This is needed for the sip session. Specify the integer format code, the format name and the sample rate.
	 * 
	 * @param format an integer value specifying the video format. Use SDPConstants for values
	 * @param formatName a string representation for the video format
	 * @param sampleRate the supported sample rate of the video format, in Hz
	 */
	public SipVideoFormat(int format, String formatName, int sampleRate) {
		this.format = format;
		this.formatName = formatName;
		this.sampleRate = sampleRate;
	}

	/**
	 * @return the format code
	 */
	public int getFormat() {
		return format;
	}

	/**
	 * @return the format name
	 */
	public String getFormatName() {
		return formatName;
	}

	/**
	 * @return the format sample rate, in Hz
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * @return concatenated format data used by the sdp rtpmap field. Example: "26 JPEG/90000"
	 */
	public String getSdpField() {
		return format + " " + formatName + "/" + sampleRate;
	}

	@Override
	public boolean equals(Object o) {
		SipVideoFormat other = (SipVideoFormat)o;
		return this.format == other.format && this.sampleRate == other.sampleRate;
	}
	
	/**
	 * Parse a SipVideoFormat from an video format string. Example: "26 JPEG/90000" or "rtpmap:26 JPEG/90000""
	 * 
	 * @param videoFormat the string to parse
	 * @return the parsed SipVideoFormat
	 */
	public static SipVideoFormat parseVideoFormat(String videoFormat) {
		try {
			String trim = videoFormat.replace("rtpmap:", "");
			String format = trim.split(" ")[0];
			String formatName = trim.split(" ")[1].split("/")[0];
			String sampleRate = trim.split(" ")[1].split("/")[1];

			return new SipVideoFormat(Integer.parseInt(format), formatName, Integer.parseInt(sampleRate));
		} catch (Exception e) {
			return null;
		}
	}

}
