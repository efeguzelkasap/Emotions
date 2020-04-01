package com.example.emotions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.data.Set;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HomeActivity extends AppCompatActivity {

    int currentEmotion;
    Button submitButton;
    Button Happiness;
    Button Sadness;
    Button surprise;
    Button Fear;
    Button Anger;
    EditText userInput;
    SwipeMenuListView listView;
    ArrayList<String> usersEmotions;
    ArrayAdapter listAdapter;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    DocumentReference documentReference;
    AnyChartView anyChartView;
    String[] months = {"Happiness", "Sadness", "surprise", "Fear", "Anger"};
    ArrayList<Integer> earnings;
    HashMap hashMap;
    int values[] = {0,0,0,0,0};





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        submitButton = findViewById(R.id.submitButton);
        userInput = findViewById(R.id.editText);
        listView = findViewById(R.id.listView);
        anyChartView = findViewById(R.id.any_chart_view);
        Happiness = findViewById(R.id.button2);
        Sadness = findViewById(R.id.button3);
        surprise = findViewById(R.id.button4);
        Fear = findViewById(R.id.button5);
        Anger = findViewById(R.id.button6);

        usersEmotions = new ArrayList<>();
        earnings = new ArrayList<>();
        hashMap = new HashMap();


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userID);
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usersEmotions);

        populateList();
        listView.setAdapter(listAdapter);
        setEmotionValue();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newEntry = userInput.getText().toString();

                if (userInput.length() != 0) {
                    userInput.setText("");
                    usersEmotions.add(newEntry);
                    listView.setAdapter(listAdapter);

                    // Add entry with the current emotion at the end seperated by a dollar sign.
                    documentReference.update("posts", FieldValue.arrayUnion(newEntry + "$" + currentEmotion));
                    earnings.clear();

                    populateList();

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
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        String itemChosen = listView.getItemAtPosition(position).toString();
                        Intent i = new Intent(HomeActivity.this, DisplayPostActivity.class);
                        i.putExtra("g", itemChosen);
                        i.putExtra("e",earnings);
                        i.putExtra("p", position);
                        startActivityForResult(i, 1);
                        //startActivity(new Intent(getApplicationContext(),DisplayPostActivity.class));
                        documentReference.update("posts", FieldValue.arrayRemove(listView.getItemAtPosition(position).toString() + "$" + earnings.get(position)));
                        break;
                    case 1:
                        documentReference.update("posts", FieldValue.arrayRemove(listView.getItemAtPosition(position).toString() + "$" + earnings.get(position)));
                        usersEmotions.remove(listView.getItemAtPosition(position).toString());
                        //earnings.remove(position);
                        earnings.clear();
                        listView.setAdapter(listAdapter);
                        listAdapter.remove(listView.getItemAtPosition(position).toString());
                        populateList();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });


        //setupPieChart();

    }

    public void setEmotionValue() {
        Happiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEmotion = 1;
            }
        });

        Sadness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEmotion = 2;
            }
        });

        surprise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEmotion = 3;
            }
        });

        Fear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEmotion = 4;
            }
        });

        Anger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEmotion = 5;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {

            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    populateList();
                }
            }
        }
    }

    protected void populateList() {
        listAdapter.clear();

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        usersEmotions = (ArrayList<String>) document.get("posts");

                        for(String n : usersEmotions){
                            String last = n.substring(n.length() - 1);
                            int number = Integer.parseInt(last);
                            earnings.add(number);
                            listAdapter.add(n.substring(0, n.indexOf("$")));

                        }
                        listView.setAdapter(listAdapter);

                        values[0] = 0;
                        values[1] = 0;
                        values[2] = 0;
                        values[3] = 0;
                        values[4] = 0;

                        for(int i = 0; i < earnings.size(); i++) {
                            if (earnings.get(i) == 1) {
                                values[0] += 1;
                            } else if (earnings.get(i) == 2) {
                                values[1] += 1;
                            } else if (earnings.get(i) == 3) {
                                values[2] += 1;
                            } else if (earnings.get(i) == 4) {
                                values[3] += 1;
                            } else if (earnings.get(i) == 5) {
                                values[4] += 1;
                            }
                        }

                        final Pie pie = AnyChart.pie();
                        List<DataEntry> dataEntriesPie;


                        dataEntriesPie = new ArrayList<>();

                        pie.legend()
                                .position("center-bottom")
                                .itemsLayout(LegendLayout.HORIZONTAL)
                                .align(Align.CENTER)
                                .fontSize(10);

                        dataEntriesPie.add(new ValueDataEntry(months[0], values[0]));
                        dataEntriesPie.add(new ValueDataEntry(months[1], values[1]));
                        dataEntriesPie.add(new ValueDataEntry(months[2], values[2]));
                        dataEntriesPie.add(new ValueDataEntry(months[3], values[3]));
                        dataEntriesPie.add(new ValueDataEntry(months[4], values[4]));

                        pie.data(dataEntriesPie);
                        anyChartView.setChart(pie);

                        final int delayMillis = 500;
                        final Handler handler = new Handler();
                        final Runnable runnable = new Runnable() {
                            public void run() {
                                List<DataEntry> data = new ArrayList<>();
                                data.add(new ValueDataEntry(months[0], values[0]));
                                data.add(new ValueDataEntry(months[1], values[1]));
                                data.add(new ValueDataEntry(months[2], values[2]));
                                data.add(new ValueDataEntry(months[3], values[3]));
                                data.add(new ValueDataEntry(months[4], values[4]));
                                pie.data(data);

                                handler.postDelayed(this, delayMillis);
                            }
                        };
                        handler.postDelayed(runnable, delayMillis);




                    }
                    }
                }
            });
        }
    }






