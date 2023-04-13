package com.example.familymapclient.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.familymapclient.R;
import com.example.familymapclient.data.DataCache;

import model.Person;

public class PersonActivity extends AppCompatActivity {

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