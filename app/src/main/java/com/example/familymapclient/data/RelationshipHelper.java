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
            return "Extended family member";
    }

    // filter events by preferences
    // true = event is not drawn
    public static boolean isEventFiltered(Event event, Context context){
        DataCache cache = DataCache.getInstance();
        Person person = cache.getPeople().get(event.getPersonID());
        Person user = cache.getPeople().get(cache.getPersonId());

        // filter out events based on settings
        // to use a custom preference file location, use this instead:
        // SharedPreferences preferences = this.getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showMaleEvents = preferences.getBoolean("male_events", true);
        boolean showFemaleEvents = preferences.getBoolean("female_events", true);
        boolean showMotherSide = preferences.getBoolean("mother_side", true);
        boolean showFatherSide = preferences.getBoolean("father_side", true);

        if(!showMaleEvents && Character.toLowerCase(person.getGender()) == 'm') return true;
        if(!showFemaleEvents && Character.toLowerCase(person.getGender()) == 'f') return true;
        if(!showMotherSide && cache.getMaternalAncestors(user).contains(person)) return true;
        if(!showFatherSide && cache.getPaternalAncestors(user).contains(person)) return true;

        return false;
    }

    public static boolean isPersonFiltered(Person person, Context context){
        DataCache cache = DataCache.getInstance();
        Person user = cache.getPeople().get(cache.getPersonId());

        // filter out events based on settings
        // to use a custom preference file location, use this instead:
        // SharedPreferences preferences = this.getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showMaleEvents = preferences.getBoolean("male_events", true);
        boolean showFemaleEvents = preferences.getBoolean("female_events", true);
        boolean showMotherSide = preferences.getBoolean("mother_side", true);
        boolean showFatherSide = preferences.getBoolean("father_side", true);

        if(!showMaleEvents && Character.toLowerCase(person.getGender()) == 'm') return true;
        if(!showFemaleEvents && Character.toLowerCase(person.getGender()) == 'f') return true;
        if(!showMotherSide && cache.getMaternalAncestors(user).contains(person)) return true;
        if(!showFatherSide && cache.getPaternalAncestors(user).contains(person)) return true;

        return false;
    }

    public static List<Person> filterPeople(Collection<Person> people, Context context){
        List<Person> filteredPeople = new ArrayList<>();
        for(Person p : people){
            if(!isPersonFiltered(p, context)){
                filteredPeople.add(p);
            }
        }
        return filteredPeople;
    }

    public static List<Event> filterEvents(Collection<Event> events, Context context){
        List<Event> filteredEvents = new ArrayList<>();
        for(Event e : events){
            if(!isEventFiltered(e, context)){
                filteredEvents.add(e);
            }
        }
        return filteredEvents;
    }
}
