package com.example.familymapclient.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import model.Event;
import model.Person;

public class RelationshipHelper {
    public static String calculateRelationship(Person person, Person other){
            if(person.getFatherID() != null && person.getFatherID().equals(other.getPersonID())) return "Child";
            if(person.getMotherID() != null && person.getMotherID().equals(other.getPersonID())) return "Child";
            if(person.getSpouseID() != null && person.getSpouseID().equals(other.getPersonID())) return "Spouse";
            if(other.getFatherID() != null && other.getFatherID().equals(person.getPersonID())) return "Father";
            if(other.getMotherID() != null && other.getMotherID().equals(person.getPersonID())) return "Mother";
            return "Extended family member"; // technically all people are extended family members
    }

    // filter events by preferences
    // true = event is not drawn
    public static boolean isEventFiltered(Event event, boolean showMaleEvents, boolean showFemaleEvents, boolean showMotherSide, boolean showFatherSide){
        DataCache cache = DataCache.getInstance();
        Person person = cache.getPeople().get(event.getPersonID());
        Person user = cache.getPeople().get(cache.getPersonId());

        if(!showMaleEvents && Character.toLowerCase(person.getGender()) == 'm') return true;
        if(!showFemaleEvents && Character.toLowerCase(person.getGender()) == 'f') return true;
        if(!showMotherSide && cache.getMaternalAncestors(user).contains(person)) return true;
        if(!showFatherSide && cache.getPaternalAncestors(user).contains(person)) return true;

        return false;
    }

    public static List<Event> filterEvents(Collection<Event> events, boolean showMaleEvents, boolean showFemaleEvents, boolean showMotherSide, boolean showFatherSide){
        List<Event> filteredEvents = new ArrayList<>();
        for(Event e : events){
            if(!isEventFiltered(e, showMaleEvents, showFemaleEvents, showMotherSide, showFatherSide)){
                filteredEvents.add(e);
            }
        }
        return filteredEvents;
    }
}
