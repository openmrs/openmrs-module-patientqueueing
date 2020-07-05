package org.openmrs.module.patientqueueing.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QueueingUtil {
	
	public static Date dateFormtter(Date date, String time) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		SimpleDateFormat formatterExt = new SimpleDateFormat("dd/MM/yyyy");
		
		String formattedDate = formatterExt.format(date) + " " + time;
		
		return formatter.parse(formattedDate);
	}
}
