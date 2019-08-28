package org.openmrs.module.patientqueueing.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QueueingUtil {
	
	public static Date dateFormtterDate(Date date, String time) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		SimpleDateFormat formatterExt = new SimpleDateFormat("dd/MM/yyyy");
		
		String formattedDate = formatterExt.format(date) + " " + time;
		
		return formatter.parse(formattedDate);
	}
	
	public static String dateFormtterString(Date date, String time) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		SimpleDateFormat formatterExt = new SimpleDateFormat("yyyy-MM-dd");
		
		String formattedDate = formatterExt.format(date) + " " + time;
		
		return formattedDate;
	}
}
