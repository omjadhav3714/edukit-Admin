package com.example.edukit_admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import static com.example.edukit_admin.CategoryActivity.catList;
import static com.example.edukit_admin.CategoryActivity.selected_cat_index;
import static com.example.edukit_admin.SetsActivity.selected_set_index;

public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.ViewHolder>{

    private List<String> setIds;

    public SetsAdapter(List<String> setIds){
        this.setIds = setIds;
    }
    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public SetsAdapter.ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull SetsAdapter.ViewHolder holder, int position) {
        String setID = setIds.get(position);
        holder.setData(position,setID,this);
    }

    @Override
    public int getItemCount() {
        return setIds.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView setName;
        private ImageView deleteSetb;

        public ViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);

            setName = itemView.findViewById(R.id.catName);
            deleteSetb = itemView.findViewById(R.id.catDelB);
        }
        private void setData(final int pos,final String setID,SetsAdapter adapter){

            setName.setText("SET"+String.valueOf(pos + 1));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_set_index = pos;
                    Intent intent = new Intent(itemView.getContext(),QuestionsActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });
            deleteSetb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Set")
                            .setMessage("Do you want to delete this set ? ")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteSet(pos,setID,itemView.getContext(),adapter);
                                }
                            })
                            .setNegativeButton("Cancel",null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });

        }

        private void deleteSet(int pos, String setID, Context context,SetsAdapter adapter){
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("Quiz").document(catList.get(selected_cat_index).getId())
                  .collection(setID).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            WriteBatch batch = firestore.batch();

                            for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                                batch.delete(doc.getReference());
                            }
                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Map<String, Object> catDoc = new ArrayMap<>();
                                    int index = 1;
                                    for (int i = 0;i<setIds.size(); i++){
                                        if(i!= pos){
                                            catDoc.put("SET"+String.valueOf(index)+"_ID",setIds.get(i));
                                            index++;
                                        }
                                    }
                                    catDoc.put("SETS",index-1);
                                    firestore.collection("Quiz").document(catList.get(selected_cat_index).getId())
                                            .update(catDoc)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(context,"Set Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                    SetsActivity.setsIDs.remove(pos);

                                                    catList.get(selected_cat_index).setNoOfSets(String.valueOf(SetsActivity.setsIDs.size()));
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {
                                    Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener(){
                        @Override
                        public void onFailure(@Nonnull Exception e){
                            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }
}
