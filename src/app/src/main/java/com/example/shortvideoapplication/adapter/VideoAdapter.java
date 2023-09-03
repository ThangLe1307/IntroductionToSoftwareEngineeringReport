package com.example.shortvideoapplication.adapter;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.example.shortvideoapplication.activity.CommentActivity;
import com.example.shortvideoapplication.R;
import com.example.shortvideoapplication.activity.MainActivity;
import com.example.shortvideoapplication.activity.ProfileActivity;
import com.example.shortvideoapplication.helper.StaticVariable;
import com.example.shortvideoapplication.model.Notification;
import com.example.shortvideoapplication.model.Video;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Video> videos;
    private Context context;
    private static FirebaseUser user = null;
    private List<VideoViewHolder> videoViewHolders;
    private  int currentPage;


    public VideoAdapter(Context context, List<Video> videos) {
        this.context = context;
        this.videos = videos;
        videoViewHolders = new ArrayList<>();
        currentPage = 0;
    }

    public static void setUser(FirebaseUser user) {
        VideoAdapter.user = user;
    }

    public void addVideoObject(Video video) {
        this.videos.add(video);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VideoViewHolder videoViewHolder = new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_container, parent, false ));
        videoViewHolders.add(videoViewHolder);
        return videoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.setVideoObjects(videos.get(position));
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void stopVideo(int position) {
        videoViewHolders.get(currentPage).stopVideo();
        videoViewHolders.get(position).startVideo();
        currentPage = position;
    }


    public class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        VideoView videoView;
        ImageView imvAvatar, imvPause, imvMore;
        TextView txvDescription, tvTitle;
        TextView tvComment, tvFavorites;
        ProgressBar pgbWait;
        String authorId;
        String videoId;
        int totalLikes;
        int totalComments;
        DocumentReference docRef;
        FirebaseFirestore db;
        final String LIKE_COLLECTION = "likes";
        String userId;
        boolean isPaused = false;
        boolean isLiked = false;
        final String TAG = "VideoViewHolder";

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            txvDescription = itemView.findViewById(R.id.txvDescription);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvFavorites = itemView.findViewById(R.id.tvFavorites);
            imvAvatar = itemView.findViewById(R.id.imvAvatar);
            imvPause = itemView.findViewById(R.id.imvPause);
            pgbWait = itemView.findViewById(R.id.pgbWait);
            imvMore = itemView.findViewById(R.id.imvMore);

            db = FirebaseFirestore.getInstance();


            imvAvatar.setOnClickListener(this);
            tvTitle.setOnClickListener(this);
            tvComment.setOnClickListener(this);
            imvMore.setOnClickListener(this);
            tvFavorites.setOnClickListener(this);

        }

        @SuppressLint("ClickableViewAccessibility")
        public void setVideoObjects(final Video videoObject) {
            tvTitle.setText("@" + videoObject.getUsername());
            txvDescription.setText(videoObject.getDescription());
            tvComment.setText(String.valueOf(videoObject.getTotalComments()));
            tvFavorites.setText(String.valueOf(videoObject.getTotalLikes()));
            videoView.setVideoPath(videoObject.getVideoUri());

            authorId = videoObject.getAuthorId();
            videoId = videoObject.getVideoId();
            totalComments = videoObject.getTotalComments();
            totalLikes = videoObject.getTotalLikes();
            userId = user == null ? "" : user.getUid();

            docRef = db.collection(LIKE_COLLECTION).document(videoId);
            setVideoViewListener(videoView, imvPause);

            if(!userId.isEmpty()) {
                setLikes(videoId, userId);
            }
            showAvt(imvAvatar, videoObject.getAuthorId());
            db.collection("videos").document(videoId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot document,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(ContentValues.TAG, "listen:error", e);
//                                Toast.makeText(context, "kkkk", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Integer totalLikes = document.get("totalLikes", Integer.class);
                            Log.d("totalLike", totalLikes + "");
                            tvFavorites.setText(String.valueOf(totalLikes));

                        }
                    });


            db.collection("profiles").document(authorId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot document,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(ContentValues.TAG, "listen:error", e);
//                                Toast.makeText(context, "kkkk", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            tvTitle.setText("@" + document.get("username", String.class));


                        }
                    });
        }

        public void stopVideo() {
            videoView.pause();
        }

        public void startVideo() {
            videoView.start();
            imvPause.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == imvAvatar.getId()) {
                moveToProfile(videoView.getContext(), authorId);
            }
            if(view.getId() == tvTitle.getId()) {
                moveToProfile(videoView.getContext(), authorId);
            }
            if(view.getId() == tvComment.getId()) {
                if(user == null) {
                    showNiceDialogBox(view.getContext(), null, null);
                    return;
                }
                Intent intent = new Intent(view.getContext(), CommentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("videoId", videoId);
                bundle.putString("authorId", authorId);
                bundle.putInt("totalComments", totalComments);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
            if (view.getId() == imvMore.getId()) {
                if (authorId.equals(user.getUid())) {
//                    Intent intent = new Intent(view.getContext(), DeleteVideoSettingActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("videoId", videoId);
//                    bundle.putString("authorId", authorId);
//                    intent.putExtras(bundle);
//                    view.getContext().startActivity(intent);
                }
                else {
                    moveToProfile(videoView.getContext(), authorId);
                }
            }
            if (view.getId() == tvFavorites.getId()) {
                handleTymClick(view);

            }
        }

        private void notifyLike(){
            db.collection("users").document(user.getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String username = document.get("username", String.class);
                                    Notification.pushNotification(username, authorId, StaticVariable.LIKE);
                                    Log.d(ContentValues.TAG, "DocumentSnapshot data: " + document.getData());
                                } else {
                                    Log.d(ContentValues.TAG, "No such document");
                                }
                            } else {
                                Log.d(ContentValues.TAG, "get failed with ", task.getException());
                            }
                        }
                    });


        }


        private void moveToProfile(Context context, String authorId) {
            Intent intent=new Intent(context, ProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("id", authorId);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }

        private void showAvt(ImageView imv, String authorId) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference download = storageRef.child("/user_avatars").child(authorId);

            download.getBytes(StaticVariable.MAX_BYTES_AVATAR)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            imv.setImageBitmap(bitmap);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Do nothing
                        }
                    });
        }

        private void setLikes (String videoId, String userId){
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            if (document.contains(userId)) {
                                setFillLiked(true);
                                isLiked = true;
                            }
                            else {
                                setFillLiked(false);
                                isLiked = false;
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

        }

        @SuppressLint("ClickableViewAccessibility")
        private void setVideoViewListener(VideoView videoView, ImageView imvPause) {
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    pgbWait.setVisibility(View.GONE);
                    imvPause.setVisibility(View.GONE);
                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

            videoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(videoView.isPlaying()) {
                        videoView.pause();
                        imvPause.setVisibility(View.VISIBLE);
                        return false;
                    }
                    else {
                        imvPause.setVisibility(View.GONE);
                        videoView.start();
                        return false;
                    }
                }
            });
        }

        private void handleTymClick(View view) {
            if(user == null) {
                showNiceDialogBox(view.getContext(), null, null);
                return;
            }

            setFillLiked(!isLiked);




            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            if (document.contains(userId)) {
//                                if (totalLikes!=0){
//                                    totalLikes -= 1;
//                                }

                                Map<String, Object> updates = new HashMap<>();
                                updates.put(userId, FieldValue.delete());
                                docRef.update(updates);



                            }
                            else {
                                //totalLikes += 1;

                                Map<String, Object> updates = new HashMap<>();
                                updates.put(userId, null);
                                db.collection("likes").document(videoId).update(updates);
                                notifyLike();

                            }

                        } else {
                            //totalLikes += 1;

                            Map<String, Object> newID = new HashMap<>();
                            newID.put(userId, null);
                            docRef.set(newID);

                            notifyLike();
                        }

                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            if(isLiked) {
                totalLikes -= 1;
            }
            else {
                totalLikes += 1;
            }
            isLiked = !isLiked;
            updateTotalLike(totalLikes);
        }

        private void updateTotalLike(int totalLikes) {
            db.collection("videos").document(videoId)
                    .update("totalLikes", totalLikes);
        }

        private void setFillLiked(boolean isLiked) {
            if(isLiked) {
                tvFavorites.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_fill_favorite, 0, 0);
                tvFavorites.setText(String.valueOf(totalLikes));
            }
            else {
                tvFavorites.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite, 0, 0);
                tvFavorites.setText(String.valueOf(totalLikes));
            }
        }

        public void showNiceDialogBox(Context context, @Nullable String title, @Nullable String message) {
            if(title == null) {
                title = context.getString(R.string.request_account_title);
            }
            if(message == null) {
                message = context.getString(R.string.request_account_message);
            }
            try {
                //CAUTION: sometimes TITLE and DESCRIPTION include HTML markers
                AlertDialog.Builder myBuilder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                myBuilder.setIcon(R.drawable.splash_background)
                        .setTitle(title)
                        .setMessage(message)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        })
                        .setPositiveButton("Sign up/Sign in", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichOne) {
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(intent);
                            }}) //setNegativeButton
                        .show();
            }
            catch (Exception e) { Log.e("Error DialogBox", e.getMessage() ); }
        }

    } // class ViewHolder




}// class adapter
