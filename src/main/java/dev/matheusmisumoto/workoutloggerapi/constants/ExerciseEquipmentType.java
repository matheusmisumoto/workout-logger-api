package dev.matheusmisumoto.workoutloggerapi.constants;

public enum ExerciseEquipmentType {
	BANDS("Bands"),
	FOAM_ROLL("Foam Roll"),
	BARBELL("Barbell"),
	KETTLEBELLS("Kettlebells"),
	BODY_ONLY("Body Only"),
	MACHINE("Machine"),
	CABLE("Cable"),
	MEDICINE_BALL("Medicine Ball"),
	DUMBBELL("Dumbbell"),
	EZ_CURL_BAR("E-Z Curl Bar"),
	EXERCISE_BALL("Exercise Ball"),
	OTHER("Other");
	
	private final String description;

	private ExerciseEquipmentType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	 public static ExerciseEquipmentType valueOfDescription(String description) {
		 for (ExerciseEquipmentType equipment : ExerciseEquipmentType.values()) {
			 if (equipment.getDescription().equalsIgnoreCase(description)) {
				 return equipment;
	         }
	     }
	     throw new IllegalArgumentException("No matching ExerciseEquipmentType for description: " + description);
	 }
	

}
