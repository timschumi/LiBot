package com.mz.libot.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.TimeZone;

public class Timestamp {

	public static String formatTimestamp(long timestamp, String pattern) {
		DateFormat formatter = new SimpleDateFormat(pattern);
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		return formatter.format(new Date(timestamp));
	}

	public static String formatTimestamp(OffsetDateTime timestamp, String pattern) {
		return formatTimestamp(timestamp.toInstant().toEpochMilli(), pattern);
	}

	private Timestamp() {}

}
