package dev.matheusmisumoto.workoutloggerapi.type;

public enum WorkoutSetType {
	STANDARD,
	DROP,
	NEGATIVE,
	REST_PAUSE,
	DEAD_STOP,
	WARM_UP;
	
	public static WorkoutSetType valueOfDescription(String description) {
		for (WorkoutSetType setType : WorkoutSetType.values()) {
			if (setType.toString().equalsIgnoreCase(description)) {
				return setType;
	        }
	    }
	    throw new IllegalArgumentException("No matching WorkoutSetType for description: " + description);
	}

}
