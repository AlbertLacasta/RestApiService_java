package com.sample.sampleapiservice.domain;

public class User {

    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    /**
     *
     * @param userId
     * @param firstName
     * @param lastName
     * @param email
     * @param password
     */
    public User(Integer userId, String firstName, String lastName, String email, String password) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    /****************************************************************/
    /** Getters                                                   **/
    /****************************************************************/

    /**
     *
     * @return
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     *
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    /**
     *
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /****************************************************************/
    /** Setters                                                   **/
    /****************************************************************/

    /**
     *
     * @param userId
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     *
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
