package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import android.text.format.DateFormat;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static int CODE = 1;
    private FirebaseListAdapter<Message> adapter;

    RelativeLayout main;
    FloatingActionButton fab;

    int messageCounter;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.sign_out){
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(main, (String) getText(R.string.bye), Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CODE){
            if(resultCode == RESULT_OK){
                Snackbar.make(main, (String) getText(R.string.succes), Snackbar.LENGTH_SHORT).show();
                displayChatMessage();
            } else {
                Snackbar.make(main, (String) getText(R.string.fail), Snackbar.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = (RelativeLayout)findViewById(R.id.activity_main);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input);
                String _name = getNick();
                FirebaseDatabase.getInstance().getReference().push().setValue(new Message(input.getText().toString(),
                        _name));

                input.getText().clear();
            }
        });

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),
                    CODE);
        } else {
            String _name = getNick();
            Snackbar.make(main, (String) getText(R.string.hello) + getNick(),
                    Snackbar.LENGTH_SHORT).show();
            displayChatMessage();
        }

    }

    private String getNick() {
        String _name;
        try {
            _name = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String[] strings = _name.split("@");
            _name = " " + strings[0];
        } catch (NullPointerException NPE){
            Log.e("AAA", "NPE this not email");
            _name = "John Smith";
        }
        return _name;
    }

    private void displayChatMessage() {
        final ListView messageList = (ListView)findViewById(R.id.message_list);

        Query query = FirebaseDatabase.getInstance().getReference();
        FirebaseListOptions<Message> options =
                new FirebaseListOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .setLayout(R.layout.list_item)
                        .build();

        adapter = new FirebaseListAdapter<Message>(options){
            @Override
            protected void populateView(@NonNull View v, @NonNull Message model, int position) {
                TextView text = (TextView)v.findViewById(R.id.text);
                TextView user = (TextView)v.findViewById(R.id.user);
                TextView time = (TextView)v.findViewById(R.id.time);

                text.setText(model.getText());
                user.setText(model.getUser());
                String _time = (String) DateFormat.format("HH:mm dd.MM.yyyy", model.getTime());
                long _newTime = new Date().getTime();
                long delta = _newTime - model.getTime();
                if(delta < 60000){
                    _time = (String) getText(R.string.just);
                } else if (delta < 15 * 60000){
                    _time = (String) getText(R.string.few_minutes_ago);
                }

                time.setText(_time);
                if(messageCounter != adapter.getCount()) {
                    if(messageCounter != 0)
                        messageList.smoothScrollToPosition(adapter.getCount() - 1);
                    messageCounter = adapter.getCount();
                }
            }
        };

        adapter.startListening();
        messageList.setAdapter(adapter);
    }
}