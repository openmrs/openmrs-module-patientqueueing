package org.openmrs.module.patientqueueing.customdto;

public class QueueDisplayStatsDto {
	
	private int nowServingCount;
	
	private int upNextCount;
	
	private int totalCount;
	
	public QueueDisplayStatsDto() {
	}
	
	public QueueDisplayStatsDto(int nowServingCount, int upNextCount, int totalCount) {
		this.nowServingCount = nowServingCount;
		this.upNextCount = upNextCount;
		this.totalCount = totalCount;
	}
	
	public int getNowServingCount() {
		return nowServingCount;
	}
	
	public void setNowServingCount(int nowServingCount) {
		this.nowServingCount = nowServingCount;
	}
	
	public int getUpNextCount() {
		return upNextCount;
	}
	
	public void setUpNextCount(int upNextCount) {
		this.upNextCount = upNextCount;
	}
	
	public int getTotalCount() {
		return totalCount;
	}
	
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
}
