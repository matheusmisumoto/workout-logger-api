package dev.matheusmisumoto.workoutloggerapi.model;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.hateoas.RepresentationModel;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercises")
public class Exercise extends RepresentationModel<Exercise> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private UUID id;
	
	private String name;
	
	@Enumerated(EnumType.STRING)
	private ExerciseTargetType target;
	
	@Enumerated(EnumType.STRING)
	private ExerciseEquipmentType equipment;
	
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTarget() {
		return target.getDescription();
	}
	public void setTarget(ExerciseTargetType target) {
		this.target = target;
	}
	public String getEquipment() {
		return equipment.getDescription();
	}
	public void setEquipment(ExerciseEquipmentType equipment) {
		this.equipment = equipment;
	}

}
