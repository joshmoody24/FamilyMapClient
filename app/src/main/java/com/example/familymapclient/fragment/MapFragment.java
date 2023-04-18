package com.example.familymapclient.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.familymapclient.R;
import com.example.familymapclient.activity.PersonActivity;
import com.example.familymapclient.activity.SearchActivity;
import com.example.familymapclient.activity.SettingsActivity;
import com.example.familymapclient.data.DataCache;
import com.example.familymapclient.data.RelationshipHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.Locale;

import model.Event;
import model.Person;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private TextView eventInfo;
    private ImageView eventInfoIcon;

    private Person selectedPerson;

    private boolean showMenu;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(layoutInflater, container, savedInstanceState);
        View view = layoutInflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        eventInfo = view.findViewById(R.id.mapTextView);
        eventInfoIcon = view.findViewById(R.id.mapEventInfoIcon);

        LinearLayout eventInfo = view.findViewById(R.id.mapEventInfo);
        eventInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPerson == null) return;
                Intent personIntent = new Intent(getActivity(), PersonActivity.class);
                personIntent.putExtra("personId", selectedPerson.getPersonID());
                startActivity(personIntent);
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        drawEventMarkers();
        map.setOnMarkerClickListener(this);

        // if the intent gave us an event id, center on that event
        if(getActivity().getIntent().getExtras() != null){
            String eventId = (String) getActivity().getIntent().getExtras().get("eventId");
            Event event = DataCache.getInstance().getEvents().get(eventId);
            LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLng(eventLocation));
            selectEvent(event);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(map != null) drawEventMarkers();
    }

    private void drawEventMarkers(){
        map.clear(); // get rid of old markers, if any

        DataCache cache = DataCache.getInstance();
        HashMap<String, Float> eventTypeColors = new HashMap<String, Float>();
        float[] colors = {0, 180, 90, 270, 45, 225, 135, 315};
        int nextColorIndex = 0;

        for(String eventId : cache.getEvents().keySet()){

            Event event = cache.getEvents().get(eventId);
            Person relatedPerson = cache.getPeople().get(event.getPersonID());

            if(relatedPerson == null) continue; // silence errors (just in case, don't want it crashing on users)

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean showMaleEvents = preferences.getBoolean("male_events", true);
            boolean showFemaleEvents = preferences.getBoolean("female_events", true);
            boolean showMotherSide = preferences.getBoolean("mother_side", true);
            boolean showFatherSide = preferences.getBoolean("father_side", true);


            if(RelationshipHelper.isEventFiltered(event, showMaleEvents, showFemaleEvents, showMotherSide, showFatherSide)) continue;

            LatLng location = new LatLng(event.getLatitude(), event.getLongitude());

            // dynamically assign colors to each event type
            float color;
            if(eventTypeColors.containsKey(event.getEventType().toLowerCase())){
                color = eventTypeColors.get(event.getEventType().toLowerCase());
            }
            else{
                color = colors[nextColorIndex];
                eventTypeColors.put(event.getEventType().toLowerCase(), colors[nextColorIndex]);
                nextColorIndex++;
                if(nextColorIndex >= colors.length) nextColorIndex = 0;
            }

            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(color);
            Marker marker = map.addMarker(new MarkerOptions().position(location).icon(icon));
            marker.setTag(event);
        }
    }

    private void drawLinesForEvent(Event event){
        drawEventMarkers(); // start from clean slate

        DataCache cache = DataCache.getInstance();
        Person person = cache.getPeople().get(event.getPersonID());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean showLifeStoryLines = preferences.getBoolean("life_story_lines", true);
        boolean showFamilyTreeLines = preferences.getBoolean("family_tree_lines", true);
        boolean showSpouseLines = preferences.getBoolean("spouse_lines", true);

        // draw line to spouse's birth event (or earliest event) if spouse exists
        if(showSpouseLines){
            if(person.getSpouseID() != null){
                Person spouse = cache.getPeople().get(person.getSpouseID());
                Event birth = cache.getEventForPerson(spouse, "birth");
                if(birth == null) birth = cache.getEarliestEventForPerson(spouse);

                boolean showMaleEvents = preferences.getBoolean("male_events", true);
                boolean showFemaleEvents = preferences.getBoolean("female_events", true);
                boolean showMotherSide = preferences.getBoolean("mother_side", true);
                boolean showFatherSide = preferences.getBoolean("father_side", true);

                if(!RelationshipHelper.isEventFiltered(birth, showMaleEvents, showFemaleEvents, showMotherSide, showFatherSide)){
                    map.addPolyline(new PolylineOptions()
                            .add(new LatLng(event.getLatitude(), event.getLongitude()))
                            .add(new LatLng(birth.getLatitude(), birth.getLongitude()))
                            .color(Color.BLUE)
                    );
                }
            }
        }

        // draw lines to ancestors following rules in specification
        if(showFamilyTreeLines){
            boolean showMotherSide = preferences.getBoolean("mother_side", true);
            boolean showFatherSide = preferences.getBoolean("father_side", true);
            drawLinesToUserAncestors(person, event, showMotherSide, showFatherSide);
        }

        // draw lines to events in chronological order
        if(showLifeStoryLines){
            Event previousEvent = null;
            for(Event e : cache.getEventsForPersonChronologically(person)){
                if(previousEvent == null){
                    previousEvent = e;
                    continue;
                };
                map.addPolyline(new PolylineOptions()
                        .add(new LatLng(previousEvent.getLatitude(), previousEvent.getLongitude()))
                        .add(new LatLng(e.getLatitude(), e.getLongitude()))
                        .color(Color.RED)
                );
                previousEvent = e;
            }
        }
    }

    private void drawLinesToUserAncestors(Person person, Event event, boolean motherSide, boolean fatherSide){
        float defaultLineWidth = 10f;
        if(motherSide){
            Person mother = DataCache.getInstance().getPeople().get(person.getMotherID());
            if(mother != null) drawAncestorLines(person, mother, event, defaultLineWidth);
        }
        if(fatherSide){
            Person father = DataCache.getInstance().getPeople().get(person.getFatherID());
            if(father != null) drawAncestorLines(person, father, event, defaultLineWidth);
        }
    }

    private void drawAncestorLines(Person child, Person parent, Event event, float lineWidth){
        if(child == null || parent == null) return;

        DataCache cache = DataCache.getInstance();
        float lineShrinkFactor = 0.6f;

        Event birth = cache.getEventForPerson(parent, "birth");
        if(birth == null) birth = cache.getEarliestEventForPerson(parent);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean showMaleEvents = preferences.getBoolean("male_events", true);
        boolean showFemaleEvents = preferences.getBoolean("female_events", true);
        boolean showMotherSide = preferences.getBoolean("mother_side", true);
        boolean showFatherSide = preferences.getBoolean("father_side", true);

        if(!RelationshipHelper.isEventFiltered(birth, showMaleEvents, showFemaleEvents, showMotherSide, showFatherSide)) {
            map.addPolyline(new PolylineOptions()
                    .add(new LatLng(event.getLatitude(), event.getLongitude()))
                    .add(new LatLng(birth.getLatitude(), birth.getLongitude()))
                    .color(Color.YELLOW)
                    .width(lineWidth)
            );
        }

        Person grandmother = cache.getPeople().get(parent.getMotherID());
        Person grandfather = cache.getPeople().get(parent.getFatherID());
        drawAncestorLines(parent, grandmother, birth, lineWidth * lineShrinkFactor);
        drawAncestorLines(parent, grandfather, birth, lineWidth * lineShrinkFactor);
    }

    @Override
    public void onMapLoaded() {
        // You probably don't need this callback. It occurs after onMapReady and I have seen
        // cases where you get an error when adding markers or otherwise interacting with the map in
        // onMapReady(...) because the map isn't really all the way ready. If you see that, just
        // move all code where you interact with the map (everything after
        // map.setOnMapLoadedCallback(...) above) to here.
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Event event = (Event)marker.getTag();
        LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(eventLocation));
        selectEvent(event);
        return true;
    }

    private void selectEvent(Event event){
        Person person = DataCache.getInstance().getPeople().get(event.getPersonID());
        selectedPerson = person;
        eventInfo.setText(getEventDescription(event));
        setGenderIcon(person);
        drawLinesForEvent(event);
    }

    private String getEventDescription(Event event){
        Person person = DataCache.getInstance().getPeople().get(event.getPersonID());
        String description = person.getFirstName() + " " + person.getLastName();
        description += "\n" + event.getEventType().toUpperCase(Locale.ROOT) + ": " + event.getCity() + ", " + event.getCountry();
        description += " (" + event.getYear() + ")";
        return description;
    }

    private void setGenderIcon(Person person){
        if(Character.toLowerCase(person.getGender()) == 'm'){
            eventInfoIcon.setImageResource(R.drawable.baseline_man_24);
        }
        else{
            eventInfoIcon.setImageResource(R.drawable.baseline_woman_24);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        // if the intent includes any extras, we don't want to draw the menu
        // because it is inside the EventActivity, not the MainActivity
        if(getActivity().getIntent().getExtras() == null){
            inflater.inflate(R.menu.main_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu){
        switch(menu.getItemId()){
            case R.id.searchMenuItem:
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.settingsMenuItem:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(menu);
        }
    }
}