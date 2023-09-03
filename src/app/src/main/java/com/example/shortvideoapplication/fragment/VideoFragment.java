package com.example.shortvideoapplication.fragment;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.shortvideoapplication.R;
import com.example.shortvideoapplication.adapter.VideoAdapter;
import com.example.shortvideoapplication.model.Video;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.example.shortvideoapplication.activity.SearchActivity;

import java.util.ArrayList;

public class VideoFragment extends Fragment implements View.OnClickListener {
    private Context context = null;
    private ImageButton btnSearch;
    private TextView tvVideo; // DE TEST. Sau nay sua thanh clip de xem
    private ViewPager2 viewPager2;
    ArrayList<Video> videos;
    VideoAdapter videoAdapter;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;

    StorageReference storageRef;
    Uri videoUri;

    public static VideoFragment newInstance(String strArg) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putString("name", strArg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            context = getActivity(); // use this reference to invoke main callbacks
        }
        catch (IllegalStateException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
// inflate res/layout_blue.xml to make GUI holding a TextView and a ListView
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_video, null);
        tvVideo = (TextView) layout.findViewById(R.id.tvVideo);

        btnSearch=(ImageButton) layout.findViewById(R.id.btnSearch);


        btnSearch.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();



/////////////////////////////////////////////////////////////////////////
        viewPager2 = layout.findViewById(R.id.viewPager);
        viewPager2.setOffscreenPageLimit(2);
        videos = new ArrayList<>();
        videoAdapter = new VideoAdapter(context, videos);
        VideoAdapter.setUser(user);
        viewPager2.setAdapter(videoAdapter);
        int currentPage = 0;
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                videoAdapter.stopVideo(position);
                Log.e("Selected_Page", String.valueOf(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        loadVideos();
        return layout;
    }

    @Override public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnSearch.getId())
        {
            Intent intent = new Intent(context, SearchActivity.class);
            startActivity(intent);
        }

    }//on click


    private void loadVideos() {
        db.collection("videos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Video video = dc.getDocument().toObject(Video.class);
                                    videos.add(0, video);
                                    videoAdapter.notifyItemInserted(0);
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                                    break;
                            }
                        }

                    }
                });
    }

}
