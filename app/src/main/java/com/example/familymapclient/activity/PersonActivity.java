package com.example.familymapclient.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.familymapclient.R;
import com.example.familymapclient.data.DataCache;
import com.example.familymapclient.data.RelationshipHelper;

import java.util.List;

import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {

    private Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        TextView firstName = findViewById(R.id.personFirstName);
        TextView lastName = findViewById(R.id.personLastName);
        TextView gender = findViewById(R.id.personGender);

        Intent intent = getIntent();
        Person person = DataCache.getInstance().getPeople().get(intent.getExtras().get("personId"));

        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());
        char genderChar = Character.toLowerCase(person.getGender());
        gender.setText(genderChar == 'm' ? "Male" : "Female");
        this.person = person;

        ExpandableListView expandableListView = findViewById(R.id.expandableListView);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showMaleEvents = preferences.getBoolean("male_events", true);
        boolean showFemaleEvents = preferences.getBoolean("female_events", true);
        boolean showMotherSide = preferences.getBoolean("mother_side", true);
        boolean showFatherSide = preferences.getBoolean("father_side", true);


        DataCache cache = DataCache.getInstance();
        List<Event> events = RelationshipHelper.filterEvents(
                cache.getEventsForPersonChronologically(person),
                showMaleEvents,
                showFemaleEvents,
                showMotherSide,
                showFatherSide
        );
        List<Person> people = cache.getFamilyMembers(person);

        expandableListView.setAdapter(new ExpandableListAdapter(events, people));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    // expandable list
    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private static final int EVENT_GROUP_POSITION = 0;
        private static final int PERSON_GROUP_POSITION = 1;

        private final List<Event> events;
        private final List<Person> people;

        ExpandableListAdapter(List<Event> events, List<Person> people) {
            this.events = events;
            this.people = people;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    return events.size();
                case PERSON_GROUP_POSITION:
                    return people.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            // Not used
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            // Not used
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    titleView.setText(R.string.life_events_title);
                    break;
                case PERSON_GROUP_POSITION:
                    titleView.setText(R.string.family_title);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch(groupPosition) {
                case EVENT_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeEventView(itemView, childPosition);
                    break;
                case PERSON_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.person_item, parent, false);
                    initializePersonView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return itemView;
        }

        private void initializeEventView(View eventItemView, final int childPosition) {
            TextView eventName = eventItemView.findViewById(R.id.eventItemName);
            TextView eventOwner = eventItemView.findViewById(R.id.eventItemOwner);

            Event event = events.get(childPosition);
            Person person = DataCache.getInstance().getPeople().get(event.getPersonID());
            eventName.setText(event.getEventType().toUpperCase() + ": " + event.getCity() + ", " + event.getCountry() + " (" + event.getYear() + ")");
            eventOwner.setText(person.getFirstName() + " " + person.getLastName());

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, EventActivity.class);
                    intent.putExtra("eventId", event.getEventID());
                    startActivity(intent);
                }
            });
        }

        private void initializePersonView(View personItemView, final int childPosition) {
            TextView personName = personItemView.findViewById(R.id.personItemName);
            Person familyMember = people.get(childPosition);
            personName.setText(familyMember.getFirstName() + " " + familyMember.getLastName());

            TextView personRelation = personItemView.findViewById(R.id.personItemRelation);
            personRelation.setText(RelationshipHelper.calculateRelationship(familyMember, person));

            // set gender icon
            ImageView icon = personItemView.findViewById(R.id.personItemIcon);
            char gender = Character.toLowerCase(familyMember.getGender());
            icon.setImageResource(gender == 'm' ? R.drawable.baseline_man_24 : R.drawable.baseline_woman_24);

            personItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, PersonActivity.class);
                    intent.putExtra("personId", familyMember.getPersonID());
                    startActivity(intent);                }
            });
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}