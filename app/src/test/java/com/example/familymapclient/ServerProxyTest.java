package com.example.familymapclient;
import static org.junit.Assert.*;

import com.example.familymapclient.network.ServerProxy;

import model.AuthToken;
import model.Event;
import model.Person;
import model.User;
import request.GetAllEventsResult;
import request.GetAllPersonsResult;
import request.LoginRequest;
import request.LoginResult;
import request.RegisterRequest;
import request.RegisterResult;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.ArrayList;

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

    @Test
    public void loginSuccess(){
        // register a new user
        RegisterRequest registerRequest = new RegisterRequest("validuser123", "password123", "joshmoody24@gmail.com", "Josh", "Moody", "M");
        server.register(registerRequest);

        // log in with valid credentials
        LoginRequest loginRequest = new LoginRequest("validuser123", "password123");
        LoginResult result = server.login(loginRequest);
        assertTrue(result.isSuccess());
        assertNotNull(result.getAuthtoken());
        assertNotNull(result.getPersonID());
    }

    @Test
    public void loginFail(){
        // try to log in with non-existent username
        LoginRequest loginRequest = new LoginRequest("nonexistentuser", "password123");
        LoginResult result = server.login(loginRequest);
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());

        // try to log in with incorrect password
        RegisterRequest registerRequest = new RegisterRequest("validuser123", "password123", "joshmoody24@gmail.com", "Josh", "Moody", "M");
        server.register(registerRequest);
        loginRequest = new LoginRequest("validuser123", "wrongpassword");
        result = server.login(loginRequest);
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
    }

    @Test
    public void getEventsSuccess(){
        // register a new user and add some events
        RegisterRequest registerRequest = new RegisterRequest("validuser123", "password123", "joshmoody24@gmail.com", "Josh", "Moody", "M");
        RegisterResult registerResult = server.register(registerRequest);

        // get events with valid auth token
        GetAllEventsResult result = server.getEvents(registerResult.getAuthtoken());
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(92, result.getData().length); // 3 events per person (ish)
    }

    @Test
    public void getEventsFail(){
        // try to get events with invalid auth token
        AuthToken authToken = new AuthToken("invaliduser", "token123");
        GetAllEventsResult result = server.getEvents(authToken.getAuthtoken());
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
    }

    @Test
    public void getPeopleSuccess(){
        // register a user
        String username = "validuser123";
        RegisterRequest request = new RegisterRequest(username, "password123", "joshmoody24@gmail.com", "Josh", "Moody", "M");
        server.register(request);

        // login
        LoginRequest loginRequest = new LoginRequest(username, "password123");
        LoginResult loginResult = server.login(loginRequest);
        String authToken = loginResult.getAuthtoken();

        // get people
        GetAllPersonsResult peopleResult = server.getPeople(authToken);
        assertTrue(peopleResult.isSuccess());
        assertNotNull(peopleResult.getData());
        assertEquals(31, peopleResult.getData().length); // there should be 31 people in the database
    }

    @Test
    public void getPeopleFail(){
        // try to get people without logging in
        GetAllPersonsResult peopleResult = server.getPeople(null);
        assertFalse(peopleResult.isSuccess());
        assertNotNull(peopleResult.getMessage());
    }
}