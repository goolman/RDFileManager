package com.xdockalr.rdfilemanager;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class FileManagerAdapter extends RecyclerView.Adapter<FileManagerAdapter.FileManagerAdapterViewHolder> {

    private ArrayList<File> mFileManagerListData = new ArrayList<>();
    private final FileManagerAdapterOnClickHandler mClickHandler;
    private boolean mMultiSelect = false;
    private ArrayList<File> mSelectedItems = new ArrayList<>();

    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mMultiSelect = true;
            menu.add(R.string.delete_button);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            for (File intItem : mSelectedItems) {
                mFileManagerListData.remove(intItem);
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMultiSelect = false;
            mSelectedItems.clear();
            notifyDataSetChanged();
        }
    };

    public FileManagerAdapter(FileManagerAdapterOnClickHandler clickHandler) {

        mClickHandler = clickHandler;
    }

    public interface FileManagerAdapterOnClickHandler {

        void onClick(String itemName);
        void onLongClick(String itemName);
    }

    @Override
    public FileManagerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        int layoutIdForListItem;
        if (parent.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            layoutIdForListItem = R.layout.filemanager_list_item;
        }
        else {
            layoutIdForListItem = R.layout.filemanager_grid_item;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new FileManagerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileManagerAdapterViewHolder holder, int position) {
        String itemName = mFileManagerListData.get(position).getName();
        holder.updateItem(itemName);
    }

    @Override
    public int getItemCount() {
        if (null == mFileManagerListData) return 0;
            return mFileManagerListData.size();
    }

    public void setFileManagerData(ArrayList<File> fileManagerListdata) {
        mFileManagerListData = fileManagerListdata;
        notifyDataSetChanged();
    }

    public class FileManagerAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener {

        public final TextView mFileManagerTextView;

        public FileManagerAdapterViewHolder(View itemView) {
            super(itemView);
            mFileManagerTextView = itemView.findViewById(R.id.tv_filemanager_data);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String actualItemPath = null;
            if (mMultiSelect) {
                onLongClick(v);
            }
            else {
                int position = getAdapterPosition();
                actualItemPath = mFileManagerListData.get(position).toString();
                mClickHandler.onClick(actualItemPath);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            String actualItemPath = mFileManagerListData.get(position).toString();
            mClickHandler.onLongClick(actualItemPath);
            if (!mMultiSelect) {
                ((AppCompatActivity) v.getContext()).startSupportActionMode(actionModeCallbacks);
            }
            selectItem(mFileManagerListData.get(position));
            return true;
        }

        public void updateItem(String itemName) {
            mFileManagerTextView.setText(itemName);
            if (!mMultiSelect) {
                    mFileManagerTextView.setBackgroundColor(Color.WHITE);
                } else {
                    mFileManagerTextView.setBackgroundColor(Color.LTGRAY);
                }
            }

        void selectItem(File item) {
            if (mMultiSelect) {
                if (mSelectedItems.contains(item)) {
                    mSelectedItems.remove(item);
                    mFileManagerTextView.setBackgroundColor(Color.WHITE);
                } else {
                    mSelectedItems.add(item);
                    mFileManagerTextView.setBackgroundColor(Color.LTGRAY);
                }
            }
        }

    }
}