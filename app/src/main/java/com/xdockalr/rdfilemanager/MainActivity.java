package com.xdockalr.rdfilemanager;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements FileManagerAdapter.FileManagerdapterOnClickHandler, LoaderManager.LoaderCallbacks<String[]>{

    private RecyclerView mRecycleView;
    private FileManagerAdapter mFileManagerAdapter;
    private TextView mErrorText;
    private ProgressBar mProgressBar;

    private static final int FILEMANAGER_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecycleView = findViewById(R.id.recyclerview_main);
        mErrorText = findViewById(R.id.tv_error_message_display);
        mProgressBar = findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setHasFixedSize(true);
        mFileManagerAdapter = new FileManagerAdapter(this);
        mRecycleView.setAdapter(mFileManagerAdapter);

        int loaderId = FILEMANAGER_LOADER_ID;
        LoaderManager.LoaderCallbacks<String[]> callback = MainActivity.this;
        Bundle bundleForLoader = null;
        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callback);
    }

    private void invalidateData() {
        mFileManagerAdapter.setWeatherData(null);
    }

    private void showWeatherDataView() {
        mRecycleView.setVisibility(View.VISIBLE);
        mErrorText.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage() {
        mRecycleView.setVisibility(View.INVISIBLE);
        mErrorText.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<String[]> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String[]>(this) {

            String[] mFileManagerData;

            @Override
            protected void onStartLoading() {
                if (mFileManagerData != null) {
                    deliverResult(mFileManagerData);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public String[] loadInBackground() {
                try {
                    mFileManagerData = new String[3];
                    mFileManagerData[0] = "first";
                    mFileManagerData[1] = "second";
                    mFileManagerData[2] = "third";
                    return mFileManagerData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(String[] data) {
                mFileManagerData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        mProgressBar.setVisibility(View.INVISIBLE);
        if (data != null) {
            showWeatherDataView();
            mFileManagerAdapter.setWeatherData(data);
        }
        else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {

    }

    @Override
    public void onClick(String itemName) {

    }
}
