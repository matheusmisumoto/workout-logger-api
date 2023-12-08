package dev.matheusmisumoto.workoutloggerapi.type;

public enum UserRoleType {
	DEMO("Demo"),
	MEMBER("Member"),
	ADMIN("Admin");
	
	private String role;
	
	UserRoleType(String role){
		this.role = role;
	}
	
	public String getRole() {
		return role;
	}
}
