package org.openmrs.module.patientqueueing.model;

import org.openmrs.Location;
import org.openmrs.BaseOpenmrsData;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import java.io.Serializable;

@Entity(name = "patientqueueing.QueueRoom")
@Table(name = "queue_room")
public class QueueRoom extends BaseOpenmrsData implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "queue_room_id")
	private Integer queueRoomId;

	@ManyToOne
	@JoinColumn(name = "location")
	private Location location;

	@Column(name = "name", length = 255)
	private String name;

	@Column(name = "description", length = 255)
	private String description;

	@Override
	public Integer getId() {
		return queueRoomId;
	}

	@Override
	public void setId(Integer integer) {
		queueRoomId = integer;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
