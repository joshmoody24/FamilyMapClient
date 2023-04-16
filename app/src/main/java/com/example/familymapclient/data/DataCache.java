package com.example.familymapclient.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.*;

public class DataCache {

    private static DataCache instance;
    public static DataCache getInstance(){
        if(instance == null) instance = new DataCache();
        return instance;
    }

    String authToken;
    String personId;
    Map<String, Person> people;
    Map<String, Event> events;
    Map<String, List<Event>> personEvents;
    Set<String> paternalAncestors;
    Set<String> maternalAncestors;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public Map<String, Person> getPeople() {
        return people;
    }

    public void setPeople(Map<String, Person> people) {
        this.people = people;
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }

    public Set<Event> getEventsForPerson(Person person) {
        HashSet<Event> personEvents = new HashSet();
        for(Event e : events.values()){
            if(e.getPersonID().equals(person.getPersonID())){
                personEvents.add(e);
            }
        }
        return personEvents;
    }

    // get first event for person with the matching event type
    public Event getEventForPerson(Person person, String eventType) {
        Set<Event> personEvents = getEventsForPerson(person);
        for(Event e : personEvents){
            if(e.getEventType().equals(eventType)) return e;
        }
        return null;
    }

    public Event getEarliestEventForPerson(Person person){
        return Collections.min(getEventsForPerson(person), new EventDateComparator());
    }

    // get first event for person with the matching event type
    public List<Event> getEventsForPersonChronologically(Person person) {
        Set<Event> personEvents = getEventsForPerson(person);
        List<Event> sortedEvents = new ArrayList<>();
        sortedEvents.addAll(personEvents);
        sortedEvents.sort(new EventDateComparator());
        return sortedEvents;
    }

    public Set<Person> getPaternalAncestors(Person person) {
        return getSelfAndAncestors(people.get(person.getFatherID()));
    }

    public Set<Person> getMaternalAncestors(Person person) {
        return getSelfAndAncestors(people.get(person.getMotherID()));
    }

    // get parents and children
    public List<Person> getFamilyMembers(Person person){
        List<Person> family = new ArrayList<>();
        if(person == null) return family;
        if(person.getMotherID() != null) family.add(getPeople().get(person.getMotherID()));
        if(person.getFatherID() != null) family.add(getPeople().get(person.getFatherID()));
        if(person.getSpouseID() != null) family.add(getPeople().get(person.getSpouseID()));
        for(Person p : getPeople().values()){
            if(p.getFatherID() != null){
                if(p.getFatherID().equals(person.getPersonID())){
                    family.add(p);
                }
            }
            if(p.getMotherID() != null){
                if(p.getMotherID().equals(person.getPersonID())){
                    family.add(p);
                }
            }
        }
        return family;
    }

    private Set<Person> getSelfAndAncestors(Person person){
        Set<Person> result = new HashSet<>();
        if(person.getMotherID() != null){
            result.addAll(getSelfAndAncestors(people.get(person.getMotherID())));
        }
        if(person.getFatherID() != null){
            result.addAll(getSelfAndAncestors(people.get(person.getFatherID())));
        }
        result.add(person);
        return result;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    class EventDateComparator implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2){

            // birth events come first
            if (e1.getEventType().toLowerCase().equals("birth")) {
                return -1;
            } else if (e2.getEventType().toLowerCase().equals("birth")) {
                return 1;
            }

            // birth events come last
            if(e1.getEventType().toLowerCase().equals("death")){
                return 1;
            } else if (e2.getEventType().toLowerCase().equals("death")){
                return -1;
            }

            // sort by year otherwise
            int year1 = e1.getYear();
            int year2 = e2.getYear();
            int yearCompare = Integer.compare(year1, year2);
            if (yearCompare != 0) {
                return yearCompare;
            }

            // if years equal, sort by event type
            String eventType1 = e1.getEventType().toLowerCase();
            String eventType2 = e2.getEventType().toLowerCase();
            return eventType1.compareTo(eventType2);
        }
    }
}
