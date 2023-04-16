package com.example.familymapclient.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.familymapclient.R;
import com.example.familymapclient.data.DataCache;
import com.example.familymapclient.data.RelationshipHelper;

import java.util.ArrayList;
import java.util.List;

import model.Event;
import model.Person;

public class SearchActivity extends AppCompatActivity {

    private static final int EVENT_ITEM_VIEW_TYPE = 0;
    private static final int PERSON_ITEM_VIEW_TYPE = 1;

    private EditText searchText;

    private class SearchItemAdapter extends RecyclerView.Adapter<SearchItemViewHolder> {
        private final List<Event> events;
        private final List<Person> people;

        SearchItemAdapter(List<Event> events, List<Person> people) {
            this.events = events;
            this.people = people;
        }

        @Override
        public int getItemViewType(int position) {
            return position < events.size() ? EVENT_ITEM_VIEW_TYPE : PERSON_ITEM_VIEW_TYPE;
        }

        @NonNull
        @Override
        public SearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if(viewType == EVENT_ITEM_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.event_item, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.person_item, parent, false);
            }

            return new SearchItemViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchItemViewHolder holder, int position) {
            if(position < events.size()) {
                holder.bind(events.get(position));
            } else {
                holder.bind(people.get(position - events.size()));
            }
        }

        @Override
        public int getItemCount() {
            return events.size() + people.size();
        }
    }

    private class SearchItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView name;
        private final TextView detail;
        private final ImageView icon;

        private final int viewType;
        private Event event;
        private Person person;

        SearchItemViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);


            if(viewType == EVENT_ITEM_VIEW_TYPE) {
                name = itemView.findViewById(R.id.eventItemName);
                detail = itemView.findViewById(R.id.eventItemOwner);
                icon = null; // only used to set people's gender icon
            } else {
                name = itemView.findViewById(R.id.personItemName);
                detail = null; // only display name for people
                icon = itemView.findViewById(R.id.personItemIcon);
            }
        }

        private void bind(Event event) {
            this.event = event;
            name.setText(event.getEventType().toUpperCase() + ": " + event.getCity() + ", " + event.getCountry() + " (" + event.getYear() + ")");
            Person eventPerson = DataCache.getInstance().getPeople().get(event.getPersonID());
            detail.setText(eventPerson.getFirstName() + " " + eventPerson.getLastName());
        }

        private void bind(Person person) {
            this.person = person;
            name.setText(person.getFirstName() + " " + person.getLastName());
            char gender = Character.toLowerCase(person.getGender());
            icon.setImageResource(gender == 'm' ? R.drawable.baseline_man_24 : R.drawable.baseline_woman_24);
        }

        @Override
        public void onClick(View view) {
            if(viewType == EVENT_ITEM_VIEW_TYPE) {
                Intent eventIntent = new Intent(SearchActivity.this, EventActivity.class);
                eventIntent.putExtra("eventId", event.getEventID());
                startActivity(eventIntent);
            } else {
                Intent personIntent = new Intent(SearchActivity.this, PersonActivity.class);
                personIntent.putExtra("personId", person.getPersonID());
                startActivity(personIntent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchText = findViewById(R.id.searchViewText);

        TextWatcher watcher = new TextWatcher(){
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
            public void afterTextChanged(Editable e) {
                applySearchText();
            }
        };

        searchText.addTextChangedListener(watcher);

        applySearchText();
    }

    private void applySearchText(){
        RecyclerView recyclerView = findViewById(R.id.searchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        DataCache cache = DataCache.getInstance();

        String query = searchText.getText().toString().toLowerCase();

        List<Event> events = new ArrayList<Event>();
        for(Event e : cache.getEvents().values()){
            if(
                    e.getEventType().toLowerCase().contains(query)
                    || e.getCountry().toLowerCase().contains(query)
                    || e.getCity().toLowerCase().contains(query)
                    || e.getYear().toString().contains(query)
            ){
                if(!RelationshipHelper.isEventFiltered(e, getApplicationContext())){
                    events.add(e);
                }
            }
        }

        List<Person> people = new ArrayList<Person>();
        for(Person p : cache.getPeople().values()){
            String fullName = p.getFirstName() + " " + p.getLastName();
            if(fullName.toLowerCase().contains(query)){
                if(!RelationshipHelper.isPersonFiltered(p, getApplicationContext())){
                    people.add(p);
                };
            }
        }

        // as per the project spec, blank search results at first
        // not the most efficient method but easiest to understand
        if(query.equals("")){
            people = new ArrayList<>();
            events = new ArrayList<>();
        }

        SearchItemAdapter adapter = new SearchItemAdapter(events, people);
        recyclerView.setAdapter(adapter);
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
}