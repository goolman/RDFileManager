package com.xdockalr.rdfilemanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class FileManagerAdapter extends RecyclerView.Adapter<FileManagerAdapter.FileManagerAdapterViewHolder> {

    private String mFileManagerListData[];
    private final FileManagerdapterOnClickHandler mClickHandler;

    public FileManagerAdapter(FileManagerdapterOnClickHandler clickHandler) {

        mClickHandler = clickHandler;
    }

    public interface FileManagerdapterOnClickHandler {

        void onClick(String itemName);
    }

    @Override
    public FileManagerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.filemanager_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new FileManagerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileManagerAdapterViewHolder holder, int position) {
        String itemName = mFileManagerListData[position];
        holder.mFileManagerTextView.setText(itemName);
    }

    @Override
    public int getItemCount() {
        if (null == mFileManagerListData) return 0;
            return mFileManagerListData.length;
    }

    public void setWeatherData(String[] fileManagerListdata) {
        mFileManagerListData = fileManagerListdata;
        notifyDataSetChanged();
    }

    public class FileManagerAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public final TextView mFileManagerTextView;

        public FileManagerAdapterViewHolder(View itemView) {
            super(itemView);
            mFileManagerTextView = itemView.findViewById(R.id.tv_filemanager_data);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            // TODO Akce po kliknutí na item v recycleview.
            String itemPosition = "Item: " + mFileManagerListData[position];
            Toast.makeText(v.getContext(), itemPosition, Toast.LENGTH_SHORT).show();

            mClickHandler.onClick(itemPosition);
        }
    }
}