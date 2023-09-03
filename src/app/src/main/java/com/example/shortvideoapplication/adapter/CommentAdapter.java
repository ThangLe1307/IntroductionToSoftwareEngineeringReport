package com.example.shortvideoapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.shortvideoapplication.R;
import com.example.shortvideoapplication.helper.StaticVariable;
import com.example.shortvideoapplication.model.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class CommentAdapter extends ArrayAdapter<Comment> {
    private ArrayList<Comment> comments;
    private Context context;

    public CommentAdapter(@NonNull Context context, int resource, ArrayList<Comment> comments) {
        super(context, resource, comments);
        this.comments = comments;
        this.context = context;
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.layout_row_comment, null);

        }

        ImageView imvAvatarInComment = (ImageView) row.findViewById(R.id.imvAvatarInComment);
        TextView txvUsernameInComment = (TextView) row.findViewById(R.id.txvUsernameInComment);
        TextView txvComment = (TextView) row.findViewById(R.id.txvComment);
        TextView txvTotalLikeComment = (TextView) row.findViewById(R.id.txvTotalLikeComment);

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(comments.get(position).getAuthorId());
        final String TAG = "CommentAdapter";
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        txvUsernameInComment.setText("@" + document.get("username", String.class));
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        loadAvatar(comments.get(position).getAuthorId(), imvAvatarInComment);

        txvComment.setText(comments.get(position).getContent());
        txvTotalLikeComment.setText(comments.get(position).getTotalLikes() + "");

        return (row);
    }

    private void loadAvatar(String authorId, ImageView imv) {
        StorageReference download = FirebaseStorage.getInstance().getReference().child("/user_avatars").child(authorId);
//                        StorageReference download = storageRef.child(userId.toString());

        download.getBytes(StaticVariable.MAX_BYTES_AVATAR)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                        imv.setImageBitmap(bitmap);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Do nothing
                    }
                });
//                    }
//                    else { }
//                }
//                else { }
//            });
    }
}
