package dev.matheusmisumoto.workoutloggerapi.type;

public enum ExerciseTargetType {
	CHEST("Chest"),
	FOREARMS("Forearms"),
	LATS("Lats"),
	MIDDLE_BACK("Middle Back"),
	LOWER_BACK("Lower Back"),
	NECK("Neck"),
	QUADRICEPS("Quadriceps"),
	HAMSTRINGS("Hamstrings"),
	CALVES("Calves"),
	TRICEPS("Triceps"),
	TRAPS("Traps"),
	SHOULDERS("Shoulders"),
	ABDOMINALS("Abdominals"),
	GLUTES("Glutes"),
	BICEPS("Biceps"),
	ADDUCTORS("Adductors"),
	ABDUCTORS("Abductors"),
	OTHER("Other");
	
	private final String description;
	
	private ExerciseTargetType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	 public static ExerciseTargetType valueOfDescription(String description) {
		 for (ExerciseTargetType muscleType : ExerciseTargetType.values()) {
			 if (muscleType.getDescription().equalsIgnoreCase(description)) {
				 return muscleType;
	         }
	     }
	     throw new IllegalArgumentException("No matching ExerciseMuscleType for description: " + description);
	 }
}
