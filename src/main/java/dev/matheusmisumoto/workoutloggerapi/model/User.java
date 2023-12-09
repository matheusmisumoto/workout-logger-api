package dev.matheusmisumoto.workoutloggerapi.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonFormat;

import dev.matheusmisumoto.workoutloggerapi.type.OAuthProviderType;
import dev.matheusmisumoto.workoutloggerapi.type.UserRoleType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User implements Serializable, UserDetails {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private UUID id;
	
	private String name;
	private String login;
	private int oauthId;
	
	@Enumerated(EnumType.STRING)
	private OAuthProviderType oauthProvider;

	private String avatarUrl;
	private String password;
	
	@Enumerated(EnumType.STRING)
	private UserRoleType role;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime joinedAt;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.ALL, orphanRemoval=true)
	private List<Workout> workout;

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

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public int getOauthId() {
		return oauthId;
	}

	public void setOauthId(int oauthId) {
		this.oauthId = oauthId;
	}

	public OAuthProviderType getOauthProvider() {
		return oauthProvider;
	}

	public void setOauthProvider(OAuthProviderType oauthProvider) {
		this.oauthProvider = oauthProvider;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRole(UserRoleType role) {
		this.role = role;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if(this.role == UserRoleType.ADMIN) return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_MEMBER"), new SimpleGrantedAuthority("ROLE_DEMO"));
		if(this.role == UserRoleType.MEMBER) return List.of(new SimpleGrantedAuthority("ROLE_MEMBER"), new SimpleGrantedAuthority("ROLE_DEMO"));
		else return List.of(new SimpleGrantedAuthority("ROLE_DEMO"));
		
	}

	public LocalDateTime getJoinedAt() {
		return joinedAt;
	}

	public void setJoinedAt(LocalDateTime joinedAt) {
		this.joinedAt = joinedAt;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return login;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	
}
