package com.example.familymapclient;

import com.example.familymapclient.data.DataCache;
import com.example.familymapclient.data.RelationshipHelper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Event;
import model.Person;

public class RelationshipHelperTest {

    private DataCache cache;

    @Before
    public void setUp(){
        Person person = new Person("p1", "p1", "Joe", "Person", 'm', null, null, null);
        Person father = new Person("p2", "p2", "Bob", "Person", 'f', null, null, null);
        Person mother = new Person("p3", "p3", "Jane", "Person", 'm', null, null, null);
        Person spouse = new Person("p4", "p4", "Sally", "Person", 'f', null, null, null);
        person.setFatherID(father.getPersonID());
        person.setMotherID(mother.getPersonID());
        person.setSpouseID(spouse.getPersonID());
        father.setSpouseID(mother.getPersonID());
        mother.setSpouseID(father.getPersonID());

        DataCache cache = DataCache.getInstance();
        Map<String, Person> people = new HashMap<>();
        people.put(person.getPersonID(), person);
        people.put(father.getPersonID(), father);
        people.put(mother.getPersonID(), mother);
        people.put(spouse.getPersonID(), spouse);
        cache.setPeople(people);

        Event e1 = new Event("eventId1", "p1", "p4", 0f, 0f, "USA", "Provo", "Ate a bagel", 2010);
        Event e2 = new Event("eventId2", "p1", "p2", 1f, 1f, "Mexico", "Mexico City", "Ate a taco", 2015);
        Event e3 = new Event("eventId3", "p1", "p1", 2f, 2f, "USA", "Los Angeles", "Visited the beach", 2018);
        Map<String, Event> events = new HashMap<>();
        events.put(e1.getEventID(), e1);
        events.put(e2.getEventID(), e2);
        events.put(e3.getEventID(), e3);
        cache.setEvents(events);

        cache.setPersonId(person.getPersonID());

        this.cache = cache;
    }

    @Test
    public void familyRelationshipsNormal(){
        Person person = cache.getPeople().get("p1");
        Person father = cache.getPeople().get("p2");
        Person mother = cache.getPeople().get("p3");
        Person spouse = cache.getPeople().get("p4");
        assertEquals("Spouse", RelationshipHelper.calculateRelationship(person, spouse));
        assertEquals("Father", RelationshipHelper.calculateRelationship(father, person));
        assertEquals("Mother", RelationshipHelper.calculateRelationship(mother, person));
        assertEquals("Child", RelationshipHelper.calculateRelationship(person, mother));
        assertEquals("Spouse", RelationshipHelper.calculateRelationship(mother, father));
    }

    @Test
    public void familyRelationshipsAbnormal(){
        Person person1 = new Person("p1", "p1", "Joe", "Person", 'm', null, null, null);
        Person person2 = new Person("p2", "p2", "Bob", "Person", 'f', null, null, null);
        // technically all people are extended family members
        assertEquals("Extended family member", RelationshipHelper.calculateRelationship(person1, person2));
    }

    @Test
    public void eventFilterPositive(){
        Event e1 = cache.getEvents().get("eventId1");
        Event e2 = cache.getEvents().get("eventId2");
        Event e3 = cache.getEvents().get("eventId3");
        List<Event> filteredEvents = RelationshipHelper.filterEvents(cache.getEvents().values(), false, true, true, true);
        assertTrue(filteredEvents.contains(e1));
        filteredEvents = RelationshipHelper.filterEvents(cache.getEvents().values(), true, true, false, true);
        assertTrue(filteredEvents.contains(e2));
        filteredEvents = RelationshipHelper.filterEvents(cache.getEvents().values(), true, false, true, true);
        assertTrue(filteredEvents.contains(e3));
    }

    @Test
    public void eventFilterNegative(){
        Event e1 = cache.getEvents().get("eventId1");
        Event e2 = cache.getEvents().get("eventId2");
        Event e3 = cache.getEvents().get("eventId3");
        List<Event> filteredEvents = RelationshipHelper.filterEvents(cache.getEvents().values(), true, false, true, true);
        assertFalse(filteredEvents.contains(e1));
        filteredEvents = RelationshipHelper.filterEvents(cache.getEvents().values(), true, true, true, false);
        assertFalse(filteredEvents.contains(e2));
        filteredEvents = RelationshipHelper.filterEvents(cache.getEvents().values(), false, true, true, true);
        assertFalse(filteredEvents.contains(e3));
    }
}
