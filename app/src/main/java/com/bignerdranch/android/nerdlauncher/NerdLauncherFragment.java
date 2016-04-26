package com.bignerdranch.android.nerdlauncher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by simon on 4/21/16.
 */
public class NerdLauncherFragment extends Fragment  {

    private RecyclerView mRecyclerView;
    private static final String TAG = "NerdLauncherFragment";
    private ActivityAdapter mActivityAdapter;
    private List<ResolveInfo> activities = null;
    private List<ResolveInfo> filteredActivities = new ArrayList<>();


    public static NerdLauncherFragment newInstance(){
        return new NerdLauncherFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_nerd_launcher_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return view;
    }


    private void setupAdapter(){

        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final PackageManager pm = getActivity().getPackageManager();
        activities = pm.queryIntentActivities(startupIntent, 0);

        Collections.sort(activities, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo lhs, ResolveInfo rhs) {
                return String.CASE_INSENSITIVE_ORDER.compare(lhs.loadLabel(pm).toString(), rhs.loadLabel(pm).toString());
            }
        });


        Log.i(TAG, "Found "+activities.size()+" activities.");
        mActivityAdapter = new ActivityAdapter(activities);
        mRecyclerView.setAdapter(mActivityAdapter);
    }

    public void filterDataInAdapter(String filterString){
        filteredActivities.clear();
        for(int i = 0; i < activities.size(); i++){
            PackageManager pm = getActivity().getPackageManager();
            ResolveInfo resolveInfo = activities.get(i);
            String activityName = resolveInfo.loadLabel(pm).toString();
            if(activityName.toLowerCase().contains(filterString)){
                filteredActivities.add(resolveInfo);
            }
        }
        mActivityAdapter.updateDataSet(filteredActivities);

    }

    private class ActivityHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ResolveInfo mResolveInfo;
        private TextView mNameTextView;
        private ImageView mImageView;

        public ActivityHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.list_item_row_text_view);
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_row_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindActivity(ResolveInfo resolveInfo){
            mResolveInfo = resolveInfo;
            PackageManager pm = getActivity().getPackageManager();
            String appName = mResolveInfo.loadLabel(pm).toString();
            Drawable drawale = mResolveInfo.loadIcon(pm).getCurrent();
            mNameTextView.setText(appName);
            mImageView.setImageDrawable(drawale);
        }

        @Override
        public void onClick(View v) {
            ActivityInfo activityInfo = mResolveInfo.activityInfo;

            Intent intent = new Intent(Intent.ACTION_MAIN).setClassName(activityInfo.applicationInfo.packageName, activityInfo.name)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);

        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder>{

        private List<ResolveInfo> mActivities;

        private ActivityAdapter(List<ResolveInfo> activities) {
            mActivities = activities;
        }

        @Override
        public ActivityHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_row, viewGroup, false);

            return new ActivityHolder(view);
        }

        @Override
        public void onBindViewHolder(ActivityHolder activityHolder, int i) {
            ResolveInfo resolveInfo = mActivities.get(i);
            activityHolder.bindActivity(resolveInfo);
        }

        @Override
        public int getItemCount() {
            return mActivities.size();
        }

        private void updateDataSet(List<ResolveInfo> activities){
            mActivities = activities;
            notifyDataSetChanged();
        }
    }
}
