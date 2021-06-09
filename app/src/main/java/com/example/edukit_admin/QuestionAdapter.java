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

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static com.example.edukit_admin.CategoryActivity.catList;
import static com.example.edukit_admin.CategoryActivity.selected_cat_index;
import static com.example.edukit_admin.QuestionsActivity.quesList;
import static com.example.edukit_admin.SetsActivity.selected_set_index;
import static com.example.edukit_admin.SetsActivity.setsIDs;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder>{
    private List<QuestionModel> ques_list;
    public QuestionAdapter(List<QuestionModel> ques_list){
        this.ques_list = ques_list;
    }
    @NonNull
    @NotNull
    @Override
    public QuestionAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull QuestionAdapter.ViewHolder holder, int position) {
        holder.setData(position,this);
    }

    @Override
    public int getItemCount() {
        return ques_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView deleteB;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.catName);
            deleteB = itemView.findViewById(R.id.catDelB);
        }
        private void setData(final int pos,QuestionAdapter adapter){

            title.setText("QUESTION"+String.valueOf(pos+1));

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(itemView.getContext(),QuestionDetailsActivity.class);
                    intent.putExtra("ACTION","EDIT");
                    intent.putExtra("Q_ID",pos);
                    itemView.getContext().startActivity(intent);
                }
            });

            deleteB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Question")
                            .setMessage("Do you want to delete this question ? ")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteQuestion(pos, itemView.getContext(),adapter );
                                }
                            })
                            .setNegativeButton("Cancel",null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        }
        private void deleteQuestion(final int pos, Context context, QuestionAdapter adapter){
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("Quiz").document(catList.get(selected_cat_index).getId())
                    .collection(setsIDs.get(selected_set_index))
                    .document(quesList.get(pos).getQuesID())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Map<String,Object> quesDoc = new ArrayMap<>();
                            int index = 1;
                            for(int i = 0;i<quesList.size();i++){
                                if(i != pos)
                                {
                                    quesDoc.put("Q" + String.valueOf(index) + "_ID", quesList.get(i).getQuesID());
                                    index++;
                                }
                            }
                            quesDoc.put("COUNT", String.valueOf(index - 1));
                            firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                                    .collection(setsIDs.get(selected_set_index)).document("QUESTIONS_LIST")
                                    .set(quesDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context,"Question Deleted Successfully",Toast.LENGTH_SHORT).show();

                                            quesList.remove(pos);
                                            adapter.notifyDataSetChanged();


                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Toast.makeText(context,e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
