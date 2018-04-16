package com.example.elisl.mylab1;

import com.google.firebase.auth.FirebaseAuth;

public class User
{
    /**
     * The user's name
     */
    private String name = null;

    /**
     * The user's email
     */
    private String email = null;

    /**
     * The user's gender.
     * Todo: check if this is actually useful
     */
    private String gender = null;

    /**
     * The user's app locale.
     * Todo: check the value of this
     */
    private String locale = "en_US";

    /**
     *  The user's timezone
     */
    private Integer timezone = 0;

    /**
     * The user's phone
     */
    private String phone = null;

    /**
     * Is user verified?
     */
    private Boolean isVerified = false;

    User()
    {

    }

    public static Boolean logged()
    {
        return FirebaseAuth.getInstance().getCurrentUser() == null;
    }

    /**
     * Set the user's name
     * @param name the name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the user's email
     * @return the email
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Sets the user's email
     * @param email
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * Gets user's gender.
     * @return the gender
     */
    public String getGender()
    {
        return gender;
    }

    /**
     * Sets the user's gender
     * @param gender the gender
     */
    public void setGender(String gender)
    {
        this.gender = gender;
    }

    /**
     * Gets the user's locale
     * @return the locale
     */
    public String getLocale()
    {
        return locale;
    }

    /**
     * Sets the user's locale
     * @param locale
     */
    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    /**
     * Gets the user's timezone
     * @return the timezone in difference from UTC
     */
    public Integer getTimezone()
    {
        return timezone;
    }

    /**
     * Sets the user's Timezone
     * @param timezone the difference from UTC in hours
     */
    public void setTimezone(Integer timezone)
    {
        this.timezone = timezone;
    }

    /**
     * Gets the user's name
     * @return the user's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the user's phone
     * @return the user's phone
     */
    public String getPhone()
    {
        return phone;
    }

    /**
     * Sets the user's phone
     * @param phone the user's phone
     */
    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    /**
     * Gets whether the user is verified
     * @return true or false
     */
    public Boolean getVerified()
    {
        return isVerified;
    }

    /**
     * Sets users verification
     * @param verified true or false
     */
    public void setVerified(Boolean verified)
    {
        isVerified = verified;
    }

}
