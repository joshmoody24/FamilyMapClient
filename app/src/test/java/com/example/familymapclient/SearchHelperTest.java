package com.example.familymapclient;

import org.junit.Test;

import model.Event;
import model.Person;

import static org.junit.Assert.*;

import com.example.familymapclient.data.SearchHelper;


public class SearchHelperTest {

    @Test
    public void searchNormal(){
        Event event = new Event("id", "p1", "p1", 0f, 0f, "USA", "Spanish Fork", "Ate a bagel", 2010);
        assertTrue(SearchHelper.eventMatchesQuery(event, "US"));
        assertTrue(SearchHelper.eventMatchesQuery(event, "2010"));
        assertTrue(SearchHelper.eventMatchesQuery(event, "bagel"));

        Person person = new Person("p1", "p1", "Josh", "Moody", 'm', null, null, null);
        assertTrue(SearchHelper.personMatchesQuery(person, "Jo"));
        assertTrue(SearchHelper.personMatchesQuery(person, "oody"));
        assertTrue(SearchHelper.personMatchesQuery(person, "Josh Moody"));
    }

    @Test
    public void searchAbnormal(){
        Event event = new Event("id", "p1", "p1", 0f, 0f, "USA", "Spanish Fork", "Ate a bagel", 2010);
        assertFalse(SearchHelper.eventMatchesQuery(event, "USX"));
        assertFalse(SearchHelper.eventMatchesQuery(event, "2011"));
        assertFalse(SearchHelper.eventMatchesQuery(event, ""));

        Person person = new Person("p1", "p1", "Josh", "Moody", 'm', null, null, null);
        assertFalse(SearchHelper.personMatchesQuery(person, ""));
        assertFalse(SearchHelper.personMatchesQuery(person, null));
        assertFalse(SearchHelper.personMatchesQuery(person, "JoshMoody"));
        assertFalse(SearchHelper.personMatchesQuery(person, "Js"));
    }
}
