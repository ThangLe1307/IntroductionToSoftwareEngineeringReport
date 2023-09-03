package com.example.shortvideoapplication.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.shortvideoapplication.fragment.FollowersListFragment;
import com.example.shortvideoapplication.fragment.FollowingListFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new FollowersListFragment();
        }
        else {
            return new FollowingListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
