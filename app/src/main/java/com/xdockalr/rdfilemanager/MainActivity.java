package com.xdockalr.rdfilemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements FileManagerAdapter.FileManagerdapterOnClickHandler, LoaderManager.LoaderCallbacks<File[]>{

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

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRecycleView.setLayoutManager(layoutManager);
        }
        else {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            mRecycleView.setLayoutManager(gridLayoutManager);
        }

        mRecycleView.setHasFixedSize(true);
        mFileManagerAdapter = new FileManagerAdapter(this);
        mRecycleView.setAdapter(mFileManagerAdapter);

        String path = Environment.getExternalStorageDirectory().toString();
        int loaderId = FILEMANAGER_LOADER_ID;
        LoaderManager.LoaderCallbacks<File[]> callback = MainActivity.this;
        Bundle bundleForLoader = new Bundle();
        bundleForLoader.putString("path", path);
        mFileManagerAdapter.setFileManagerData(null);
        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkStoragePermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1 );
        }
    }

    private void loadPath(String path) {
        int loaderId = FILEMANAGER_LOADER_ID;
        LoaderManager.LoaderCallbacks<File[]> callback = MainActivity.this;
        Bundle bundleForLoader = new Bundle();
        bundleForLoader.putString("path", path);
        mFileManagerAdapter.setFileManagerData(null);
        getSupportLoaderManager().restartLoader(loaderId,bundleForLoader, this);
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
    public Loader<File[]> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<File[]>(this) {

            File[] mFileManagerData = null;
            String actualPath = args.getString("path");

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
            public File[] loadInBackground() {

                File mDirectory;
                File[] mFiles = null;
                try {
                    if (new File(actualPath).isDirectory()) {
                        mDirectory = new File(String.valueOf(actualPath));
                        mFiles = mDirectory.listFiles(new FilenameFilter() {
                                                          @Override
                                                          public boolean accept(File dir, String name) {
                                                              return name.matches("^[^\\.].*");
                                                          }
                                                      }
                        );

                        if (mFiles != null) {
                            Arrays.sort(mFiles);
                        }
                    }

                    return mFiles;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(File[] data) {
                mFileManagerData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<File[]> loader, File[] data) {
        mProgressBar.setVisibility(View.INVISIBLE);
        if (data != null) {
            showWeatherDataView();
            mFileManagerAdapter.setFileManagerData(data);
        }
        else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<File[]> loader) {

    }

    @Override
    public void onClick(String actualItemPath) {
        Toast.makeText(this, actualItemPath, Toast.LENGTH_SHORT).show();
        loadPath(actualItemPath);
    }
}
