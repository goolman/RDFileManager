package com.xdockalr.rdfilemanager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileManagerAdapter extends RecyclerView.Adapter<FileManagerAdapter.FileManagerAdapterViewHolder> {

    private Context mContext;
    private ArrayList<File> mFileManagerListData = new ArrayList<>();
    private final FileManagerAdapterOnClickHandler mClickHandler;
    private boolean mMultiSelect = false;
    private ArrayList<String> mSelectedItemsArray = new ArrayList<>();
    private ActionMode mActionMode;

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
        public boolean onActionItemClicked(final ActionMode actionMode, MenuItem item) {

            String itemId = item.toString();
            if (itemId.equals(item.toString())) {
                if(((MainActivity) mContext).isWriteExtStoragePermissionGranted()) {
                    new AlertDialog.Builder(mContext)
                    .setTitle(R.string.delete_button)
                    .setMessage(R.string.delete_alert_text)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            for (String itemName : mSelectedItemsArray) {
                                File actualFile = new File(itemName);
                                mFileManagerListData.remove(actualFile);
                                boolean x = deleteMain(mContext,actualFile);
                            }
                            actionMode.finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

                }
                else {
                    ActivityCompat.requestPermissions(((MainActivity) mContext), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                }
                return true;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMultiSelect = false;
            mSelectedItemsArray.clear();
            notifyDataSetChanged();
        }
    };

    FileManagerAdapter(FileManagerAdapterOnClickHandler clickHandler, Context context) {

        mClickHandler = clickHandler;
        mContext = context;
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

    void setFileManagerData(ArrayList<File> fileManagerListdata) {
        mFileManagerListData = fileManagerListdata;
        notifyDataSetChanged();
    }

    ArrayList<String> getSelectedItemsArray() {
        return mSelectedItemsArray;
    }

    void setSelectedItemsArray(ArrayList<String> selectedItems) {
        mSelectedItemsArray = selectedItems;
            if (selectedItems.size() > 0) {
                mMultiSelect = true;
                mActionMode = ((AppCompatActivity) mContext).startSupportActionMode(actionModeCallbacks);
        }
    }

    public class FileManagerAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener {

        final TextView mFileManagerTextView;
        final ImageView mFileManagerImageView;

        FileManagerAdapterViewHolder(View itemView) {
            super(itemView);
            mFileManagerTextView = itemView.findViewById(R.id.tv_filemanager_data);
            mFileManagerImageView = itemView.findViewById(R.id.iv_icon);
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
                mActionMode = ((AppCompatActivity) mContext).startSupportActionMode(actionModeCallbacks);
            }
            else if ((mSelectedItemsArray.size() == 1) && (mSelectedItemsArray.get(0).equals(actualItemPath))){
                mActionMode.finish();
            }
            selectItem(mFileManagerListData.get(position));
            return true;
        }

        void updateItem(String itemName) {
            mFileManagerTextView.setText(itemName);
            if (new File(MainActivity.mActualPath + File.separator + itemName).isDirectory()) {
                mFileManagerImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_folder));
            }
            else if (!(new File(MainActivity.mActualPath + File.separator +itemName).isDirectory())) {
                mFileManagerImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_file));
            }
            if (mMultiSelect && (mSelectedItemsArray.contains(MainActivity.mActualPath + File.separator + itemName ))) {
                mFileManagerTextView.setBackgroundColor(Color.LTGRAY);
            } else {
                mFileManagerTextView.setBackgroundColor(Color.WHITE);
            }
        }

        void selectItem(File item) {
            if (mMultiSelect) {
                if (mSelectedItemsArray.contains(item.toString())) {
                    mSelectedItemsArray.remove(item.toString());
                    mFileManagerTextView.setBackgroundColor(Color.WHITE);
                } else {
                    mSelectedItemsArray.add(item.toString());
                    mFileManagerTextView.setBackgroundColor(Color.LTGRAY);
                }
            }
        }
    }

    private static boolean deleteItem(final Context context, final File file) {
        final String where = MediaStore.MediaColumns.DATA + "=?";
        final String[] selectionArgs = new String[] {
                file.getAbsolutePath()
        };
        final ContentResolver contentResolver = context.getContentResolver();
        final Uri filesUri = MediaStore.Files.getContentUri("external");

        contentResolver.delete(filesUri, where, selectionArgs);

        if (file.exists() && file.isDirectory()) {
            file.delete();
        }
        return !file.exists();
    }

    private static boolean deleteMain(final Context context, final File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return deleteItem(context, path);
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteMain(context, file);
                } else {
                    for (File delFile : files)
                        deleteItem(context, delFile);
                }
            }
        }
        return deleteItem(context, path);
    }
}