package com.example.familymapclient.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.familymapclient.R;
import com.example.familymapclient.data.DataCache;
import com.example.familymapclient.network.ServerProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Event;
import model.Person;
import request.GetAllEventsResult;
import request.GetAllPersonsResult;
import request.LoginRequest;
import request.LoginResult;
import request.RegisterRequest;
import request.RegisterResult;

public class LoginFragment extends Fragment {

    private Listener listener;

    public interface Listener {
        void login();
    }

    private Button signIn;
    private Button register;
    private EditText host;
    private EditText port;
    private EditText username;
    private EditText password;
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private RadioGroup gender;
    private RadioButton male;

    private final String AUTH_TOKEN = "auth_token";
    private final String ERROR_MESSAGE = "error_message";
    private final String PERSON_NAME = "person_name";

    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initializeUI(view);

        setHasOptionsMenu(false);

        return view;
    }

    void initializeUI(View view){
        // find all the ui elements
        signIn = view.findViewById(R.id.signIn);
        register = view.findViewById(R.id.register);
        host = view.findViewById(R.id.serverHostField);
        port = view.findViewById(R.id.serverPortField);
        username = view.findViewById(R.id.usernameField);
        password = view.findViewById(R.id.passwordField);
        firstName = view.findViewById(R.id.firstNameField);
        lastName = view.findViewById(R.id.lastNameField);
        email = view.findViewById(R.id.emailField);
        gender = view.findViewById(R.id.genderField);
        male = view.findViewById(R.id.maleRadio);

        TextWatcher watcher = new TextWatcher(){
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
            public void afterTextChanged(Editable e) {
                updateButtonStatus();
            }
        };

        signIn.addTextChangedListener(watcher);
        register.addTextChangedListener(watcher);
        host.addTextChangedListener(watcher);
        port.addTextChangedListener(watcher);
        username.addTextChangedListener(watcher);
        password.addTextChangedListener(watcher);
        firstName.addTextChangedListener(watcher);
        lastName.addTextChangedListener(watcher);
        email.addTextChangedListener(watcher);
        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId){
                updateButtonStatus();
            }
        });

        Handler loginHandler = new Handler(){
            @Override
            public void handleMessage(Message message) {
                Bundle bundle = message.getData();
                String authToken = bundle.getString(AUTH_TOKEN);
                String errorMessage = bundle.getString(ERROR_MESSAGE);
                if (authToken == null) {
                    Toast.makeText(getActivity(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                } else {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.submit(new GetDataTask(new Handler() {
                        @Override
                        public void handleMessage(Message message) {
                            String personName = message.getData().getString(PERSON_NAME);
                            Toast.makeText(getActivity(), "Logged in as " + personName, Toast.LENGTH_LONG).show();
                            listener.login(); // change to map fragment
                        }
                    }, authToken));
                }
            }
        };

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(new LoginTask(loginHandler));
            }
        });

        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(new RegisterTask(loginHandler));
            }
        });
    }

    void updateButtonStatus(){
        signIn.setEnabled(
                !host.getText().toString().equals("")
                        && !port.getText().toString().equals("")
                        && !username.getText().toString().equals("")
                        && !password.getText().toString().equals("")
        );

        register.setEnabled(
                signIn.isEnabled()
                        && !firstName.getText().toString().equals("")
                        && !lastName.getText().toString().equals("")
                        && !email.getText().toString().equals("")
                        && gender.getCheckedRadioButtonId() != -1
        );
    }

    private class LoginTask implements Runnable {

        Handler handler;
        public LoginTask(Handler handler){ this.handler = handler;}
        @Override
        public void run() {
            LoginRequest request = new LoginRequest(
                    username.getText().toString(),
                    password.getText().toString()
            );
            LoginResult result = new ServerProxy(
                    host.getText().toString(),
                    Integer.parseInt(port.getText().toString())
            ).login(request);
            DataCache.getInstance().setPersonId(result.getPersonID());
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(AUTH_TOKEN, result.getAuthtoken());
            bundle.putString(ERROR_MESSAGE, result.getMessage());
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    private class RegisterTask implements Runnable {

        Handler handler;
        public RegisterTask(Handler handler){ this.handler = handler;}
        @Override
        public void run() {
            char genderChar = 'F';
            if(gender.getCheckedRadioButtonId() == male.getId()) genderChar = 'M';
            RegisterRequest request = new RegisterRequest(
                    username.getText().toString(),
                    password.getText().toString(),
                    email.getText().toString(),
                    firstName.getText().toString(),
                    lastName.getText().toString(),
                    Character.toString(genderChar)
            );
            RegisterResult result = new ServerProxy(
                    host.getText().toString(),
                    Integer.parseInt(port.getText().toString())
            ).register(request);
            DataCache.getInstance().setPersonId(result.getPersonId());

            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(AUTH_TOKEN, result.getAuthtoken());
            bundle.putString(ERROR_MESSAGE, result.getMessage());
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    private class GetDataTask implements Runnable {
        String auth;
        Handler handler;

        public GetDataTask(Handler handler, String auth){
            this.handler = handler;
            this.auth = auth;
        }

        @Override
        public void run() {
            ServerProxy proxy = new ServerProxy(
                    host.getText().toString(),
                    Integer.parseInt(port.getText().toString())
            );

            GetAllPersonsResult peopleResult = proxy.getPeople(auth);
            GetAllEventsResult eventsResult = proxy.getEvents(auth);

            // convert arrays to maps
            Map<String, Person> people = new HashMap<>();
            for(Person p : peopleResult.getData()){
                people.put(p.getPersonID(), p);
            }
            Map<String, Event> events = new HashMap<>();
            for(Event e : eventsResult.getData()){
                events.put(e.getEventID(), e);
            }

            DataCache cache = DataCache.getInstance();
            cache.setPeople(people);
            cache.setEvents(events);

            // display logged in user information (for family map login pass off)
            Person user = cache.getPeople().get(cache.getPersonId());

            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(PERSON_NAME, user.getFirstName() + " " + user.getLastName());
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }
}