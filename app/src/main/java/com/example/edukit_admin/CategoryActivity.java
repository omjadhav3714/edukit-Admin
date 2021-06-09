package com.example.edukit_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class CategoryActivity extends AppCompatActivity {
    private RecyclerView cat_recycle_view;
    private Button addCatB;
    public static List<CategoryModel> catList = new ArrayList<>();
    public static int selected_cat_index = 0;
    private FirebaseFirestore firestore;
    private Dialog addCatDialog;
    private EditText dialog_cat_name;
    private Button dialogAddB;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        cat_recycle_view = findViewById(R.id.cat_recycler);
        addCatB = findViewById(R.id.addCatB);

        addCatDialog = new Dialog(CategoryActivity.this);
        addCatDialog.setContentView(R.layout.add_category_dialog);
        addCatDialog.setCancelable(false);
        addCatDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog_cat_name = addCatDialog.findViewById(R.id.ac_cat_name);
        dialogAddB = addCatDialog.findViewById(R.id.ac_add_btn);

        firestore = FirebaseFirestore.getInstance();

        addCatB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_cat_name.getText().clear();
                addCatDialog.show();
            }
        });

        dialogAddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialog_cat_name.getText().toString().isEmpty())
                {
                    dialog_cat_name.setError("Enter Category Name");
                    return;
                }

                addNewCategory(dialog_cat_name.getText().toString());
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cat_recycle_view.setLayoutManager(layoutManager);

        loadData();
    }
    private void loadData()
    {
        catList.clear();

        firestore.collection("Quiz").document("Categories")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful())
                {
                    DocumentSnapshot doc = task.getResult();

                    if(doc.exists())
                    {
                        long count = (long)doc.get("COUNT");

                        for(int i=1; i <= count; i++)
                        {
                            String catName = doc.getString("CAT" + String.valueOf(i) + "_NAME");
                            String catId = doc.getString("CAT" + String.valueOf(i) + "_ID");
                            catList.add(new CategoryModel(catId,catName,"0","1"));
                        }
                        adapter = new CategoryAdapter(catList);
                        cat_recycle_view.setAdapter(adapter);
                    }
                    else
                    {
                        Toast.makeText(CategoryActivity.this,"No Category Document Exists!",Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
                else
                {

                    Toast.makeText(CategoryActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void addNewCategory(String title){
        addCatDialog.dismiss();

        Map<String,Object> catData = new ArrayMap<>();
        catData.put("NAME",title);
        catData.put("SETS",0);
        catData.put("COUNTER","1");

        String doc_id = firestore.collection("Quiz").document().getId();

        firestore.collection("Quiz").document(doc_id).set(catData)
                .addOnSuccessListener(new OnSuccessListener<Void>(){
                   @Override
                    public void onSuccess(Void aVoid){
                       Map<String ,Object> catDoc = new ArrayMap<>();
                       catDoc.put("CAT"+String.valueOf(catList.size() + 1)+ "_NAME",title);
                       catDoc.put("CAT"+String.valueOf(catList.size() + 1)+ "_ID",doc_id);
                       catDoc.put("COUNT",catList.size()+1);

                       firestore.collection("Quiz").document("Categories").update(catDoc)
                               .addOnSuccessListener(new OnSuccessListener<Void>(){
                                   @Override
                                   public void onSuccess(Void aVoid) {
                                       Toast.makeText(CategoryActivity.this,"Category Added",Toast.LENGTH_SHORT).show();
                                       catList.add(new CategoryModel(doc_id,title,"0","1"));
                                       adapter.notifyItemInserted(catList.size());
                                   }
                               }).addOnFailureListener(new OnFailureListener(){
                          @Override
                           public void onFailure(@NonNull Exception e){
                              Toast.makeText(CategoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                          }
                       });
                   }
                }).addOnFailureListener(new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull Exception e){
                        Toast.makeText(CategoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
        });

    }
}