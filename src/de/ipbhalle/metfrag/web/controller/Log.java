package de.ipbhalle.metfrag.web.controller;

public class Log {
	
	private StringBuilder log;
	
	public Log()
	{
		log = new StringBuilder();
	}

	public synchronized void addtoLog(String logEntry) {
		this.log.append(logEntry + "<br />");
	}

	public String getLog() {
		return log.toString();
	}
	
	

}
