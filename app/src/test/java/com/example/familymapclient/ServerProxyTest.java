package com.example.familymapclient;
import static org.junit.Assert.*;

import com.example.familymapclient.network.ServerProxy;

import request.RegisterRequest;
import request.RegisterResult;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * All of these tests assume that FamilyMapServer is running on localhost on port 8080
 */
public class ServerProxyTest {

    ServerProxy server;

    @Before
    public void setUp(){
        server = new ServerProxy("localhost", 8080);
        server.clear();
    }

    @After
    public void takeDown(){
        server.clear();
    }

    @Test
    public void registerSuccess() {
        String username = "validuser123";
        RegisterRequest request = new RegisterRequest(username, "password123", "joshmoody24@gmail.com", "Josh", "Moody", "M");
        RegisterResult result = server.register(request);
        assertTrue(result.isSuccess());
        assertNull(result.getMessage());
        assertEquals(username, result.getUsername());
    }

    @Test
    public void registerFail() {
        // try to register when username already exists
        RegisterRequest request = new RegisterRequest("joshmoody24", "password123", "joshmoody24@gmail.com", "Josh", "Moody", "M");
        server.register(request);
        RegisterResult result = server.register(request); // register again
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
    }
}