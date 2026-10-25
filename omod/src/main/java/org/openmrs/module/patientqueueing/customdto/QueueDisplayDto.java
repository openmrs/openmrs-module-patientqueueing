package org.openmrs.module.patientqueueing.customdto;

import java.util.Date;
import java.util.List;

public class QueueDisplayDto {
	
	private QueueDisplayContextDto context;
	
	private List<QueueDisplayRowDto> nowServing;
	
	private List<QueueDisplayRowDto> upNext;
	
	private QueueDisplayStatsDto stats;
	
	private Date lastUpdated;
	
	public QueueDisplayDto() {
	}
	
	public QueueDisplayContextDto getContext() {
		return context;
	}
	
	public void setContext(QueueDisplayContextDto context) {
		this.context = context;
	}
	
	public List<QueueDisplayRowDto> getNowServing() {
		return nowServing;
	}
	
	public void setNowServing(List<QueueDisplayRowDto> nowServing) {
		this.nowServing = nowServing;
	}
	
	public List<QueueDisplayRowDto> getUpNext() {
		return upNext;
	}
	
	public void setUpNext(List<QueueDisplayRowDto> upNext) {
		this.upNext = upNext;
	}
	
	public QueueDisplayStatsDto getStats() {
		return stats;
	}
	
	public void setStats(QueueDisplayStatsDto stats) {
		this.stats = stats;
	}
	
	public Date getLastUpdated() {
		return lastUpdated;
	}
	
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
