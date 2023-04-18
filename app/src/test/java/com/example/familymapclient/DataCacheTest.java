package com.example.familymapclient;

import static org.junit.Assert.assertEquals;

import com.example.familymapclient.data.DataCache;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Event;
import model.Person;

public class DataCacheTest {

    @Test
    public void chronologicalSortingNormal(){
        // Test that events for a person are sorted chronologically
        Person person = new Person("personID", "username", "firstName", "lastName", 'm', "fatherID", "motherID", "spouseID");
        Event event1 = new Event("eventID1", "username", "personID", 10.0f, 20.0f, "city", "country", "eventType1", 2021);
        Event event2 = new Event("eventID2", "username", "personID", 30.0f, 40.0f, "country", "city", "birth", 2020);
        Event event3 = new Event("eventID3", "username", "personID", 50.0f, 60.0f,"city","eventType2",  "country",  2020);
        Event event4 = new Event("eventID4", "username", "personID",  70.0f, 80.0f, "country", "city", "death", 2022);

        Map<String, Event> events = new HashMap<>();
        events.put(event1.getEventID(), event1);
        events.put(event2.getEventID(), event2);
        events.put(event3.getEventID(), event3);
        events.put(event4.getEventID(), event4);
        DataCache.getInstance().setEvents(events);

        List<Event> expected = new ArrayList<Event>();
        expected.add(event2);
        expected.add(event3);
        expected.add(event1);
        expected.add(event4);

        List<Event> actual = DataCache.getInstance().getEventsForPersonChronologically(person);
        assertEquals(expected, actual);
    }

    @Test
    public void chronologicalSortingAbnormal(){
        // Test that null is returned when there are no events for the person
        Person person = new Person("personID", "username", "firstName", "lastName", 'm', null, null, null);
        DataCache.getInstance().setEvents(new HashMap<>());
        List<Event> expected = new ArrayList<>();
        List<Event> actual = DataCache.getInstance().getEventsForPersonChronologically(person);
        assertEquals(expected, actual);
    }
}
