package com.example.edukit_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.edukit_admin.CategoryActivity.catList;
import static com.example.edukit_admin.CategoryActivity.selected_cat_index;
import static com.example.edukit_admin.SetsActivity.selected_set_index;
import static com.example.edukit_admin.SetsActivity.setsIDs;

public class QuestionsActivity extends AppCompatActivity {
    private RecyclerView quesView;
    private Button addQB;
    public static List<QuestionModel> quesList = new ArrayList<>();
    private QuestionAdapter adapter;
    private FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        Toolbar toolbar = findViewById(R.id.q_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Questions");

        quesView = findViewById(R.id.quest_recycler);
        addQB = findViewById(R.id.addQB);

        addQB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(QuestionsActivity.this,QuestionDetailsActivity.class);
                intent.putExtra("ACTION","ADD");
                startActivity(intent);

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        quesView.setLayoutManager(layoutManager);
        firestore = FirebaseFirestore.getInstance();
        loadQuestions();
    }
    private void loadQuestions(){
        quesList.clear();
        firestore.collection("Quiz").document(catList.get(selected_cat_index).getId())
                .collection(setsIDs.get(selected_set_index))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>(){
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();
                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                            docList.put(doc.getId(),doc);
                        }
                        QueryDocumentSnapshot quesListDoc = docList.get("QUESTIONS_LIST");
                        String count = quesListDoc.getString("COUNT");
                        for(int i=0; i<Integer.valueOf(count);i++){
                            String quesId = quesListDoc.getString("Q"+String.valueOf(i+1)+"_ID");
                            QueryDocumentSnapshot quesDoc = docList.get(quesId);
                            quesList.add(new QuestionModel(
                                    quesId,
                                    quesDoc.getString("QUESTION"),
                                    quesDoc.getString("A"),
                                    quesDoc.getString("B"),
                                    quesDoc.getString("C"),
                                    quesDoc.getString("D"),
                                    Integer.valueOf(quesDoc.getString("ANSWER"))
                            ));
                        }
                        adapter = new QuestionAdapter(quesList);
                        quesView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(QuestionsActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }
}