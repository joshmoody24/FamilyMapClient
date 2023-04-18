package com.example.familymapclient.data;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import model.Event;
import model.Person;

public class SearchHelper {

    public static boolean eventMatchesQuery(Event e, String query){
        if(query == null || query.equals("")) return false;
        query = query.toLowerCase();
        return e.getEventType().toLowerCase().contains(query)
                || e.getCountry().toLowerCase().contains(query)
                || e.getCity().toLowerCase().contains(query)
                || e.getYear().toString().contains(query);
    }

    public static boolean personMatchesQuery(Person p, String query){
        if(query == null || query.equals("")) return false;
        query = query.toLowerCase();
        String fullName = p.getFirstName() + " " + p.getLastName();
        return fullName.toLowerCase().contains(query);
    }

}
