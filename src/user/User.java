package user;

public abstract class User {
	public static final String NURSE = "nurse";
	public static final String DOCTOR = "doctor";
	public static final String GOV = "government";
	public static final String PATIENT = "patient";
	public final String role;
	public final int ID;
	public String password;

	public User(String password, String role, int ID) {
		this.role = role;
		this.ID = ID;
		this.password = password;
	}

	public void authenticate() {
		
	}

	public boolean isAuthenticated() {
		return true;
	}
	
	public String getPassword() {
		return password;
	}
}
