package dev.matheusmisumoto.workoutloggerapi.type;

public enum OAuthProviderType {
	GITHUB("GitHub"),
	GOOGLE("Google");
	
	private String provider;
	
	OAuthProviderType(String provider){
		this.provider = provider;
	}
	
	public String getProvider() {
		return provider;
	}

}
