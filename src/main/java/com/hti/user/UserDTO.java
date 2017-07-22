/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.user;

/**
 *
 * @author Administrator
 */
public class UserDTO {
    
    private String systemid;
    private String password;
    private int timeout;
    private double forceDelay;

    public String getSystemid() {
        return systemid;
    }

    public void setSystemid(String systemid) {
        this.systemid = systemid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public double getForceDelay() {
        return forceDelay;
    }

    public void setForceDelay(double forceDelay) {
        this.forceDelay = forceDelay;
    }
    
    
    
}
