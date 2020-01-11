package com.example.emotions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.WriteResult;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class HomeActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    Button btnEdit;
    Button submitButton;
    EditText userInput;
    SwipeMenuListView listView;
    ArrayList<String> usersEmotions;
    ArrayAdapter listAdapter;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;
    DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        submitButton = findViewById(R.id.submitButton);
        userInput = findViewById(R.id.editText);
        listView = findViewById(R.id.listView);

        usersEmotions = new ArrayList<>();

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userID);
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usersEmotions);

        populateList();

        listView.setAdapter(listAdapter);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newEntry = userInput.getText().toString();
                if(userInput.length() != 0) {
                    userInput.setText("");
                    usersEmotions.add(newEntry);
                    listView.setAdapter(listAdapter);
                    populateList();

                    documentReference.update("posts", FieldValue.arrayUnion(newEntry));
                }
            }
        });


        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0x00, 0x66,
                        0xff)));
                // set item width
                openItem.setWidth(170);
                // set item title
                openItem.setIcon(R.drawable.ic_edit);
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(170);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        listView.setMenuCreator(creator);


        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        String itemChosen = listView.getItemAtPosition(position).toString();
                        Intent i = new Intent(HomeActivity.this, DisplayPostActivity.class);
                        i.putExtra("g", itemChosen);
                        startActivityForResult(i, 1);
                        //startActivity(new Intent(getApplicationContext(),DisplayPostActivity.class));
                        documentReference.update("posts", FieldValue.arrayRemove(listView.getItemAtPosition(position).toString()));
                        break;
                    case 1:
                        documentReference.update("posts", FieldValue.arrayRemove(listView.getItemAtPosition(position).toString()));
                        usersEmotions.remove(listView.getItemAtPosition(position).toString());
                        listView.setAdapter(listAdapter);
                        listAdapter.remove(listView.getItemAtPosition(position).toString());
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {

            if (resultCode == Activity.RESULT_OK) {
                if(data!=null) {
                    populateList();
                }

            }

        }

    }

    protected void populateList()
    {
        listAdapter.clear();
        //usersEmotions.clear();


        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        usersEmotions  = (ArrayList<String>) document.get("posts");
                        for(String n : usersEmotions){
                            listAdapter.add(n);
                        }
                        listView.setAdapter(listAdapter);
                    }
                }
            }
        });

    }


}
