package dev.matheusmisumoto.workoutloggerapi.type;

public enum WorkoutStatusType {
	IN_PROGRESS, COMPLETED;
	
	private WorkoutStatusType() {
	}
	
	public static WorkoutStatusType valueOfDescription(String description) {
		for (WorkoutStatusType status : WorkoutStatusType.values()) {
			if (status.toString().equalsIgnoreCase(description)) {
				return status;
	        }
	    }
	    throw new IllegalArgumentException("No matching WorkoutStatusType for description: " + description);
	}
}
