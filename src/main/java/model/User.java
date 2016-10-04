package model;

public class User {
    private String userId;
    private String password;
    private String name;
    private String email;

    public User(String userId, String password, String name, String email) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
    
    /**
     * 
     * 파라미터로 넘어온 비밀번호와 저장된 유저의 비밀번호를 비교.
     * 
     * @param password 파라미터로 넘어온 비밀번호
     * @return	비밀번호 일치여부
     */
    public boolean login(String password) {
    	return this.password.equals(password);
    }
    
    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + ", email=" + email + "]";
    }
}
