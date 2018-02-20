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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements FileManagerAdapter.FileManagerAdapterOnClickHandler, LoaderManager.LoaderCallbacks<ArrayList<File>>{

    private RecyclerView mRecycleView;
    private FileManagerAdapter mFileManagerAdapter;
    private TextView mErrorText;
    private ProgressBar mProgressBar;
    private String mActualPath;

    private static final int FILEMANAGER_LOADER_ID = 0;
    private static final String FILEMANAGER_LOADER_PATH = "LOADER_PATH";

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
        mActualPath = Environment.getExternalStorageDirectory().toString();
        loadPath(mActualPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_refresh) {
            loadPath(mActualPath);
        }
        else if (itemId == R.id.action_settings){

        }
        return super.onOptionsItemSelected(item);
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
        LoaderManager.LoaderCallbacks<ArrayList<File>> callback = MainActivity.this;
        Bundle bundleForLoader = new Bundle();
        bundleForLoader.putString(FILEMANAGER_LOADER_PATH, path);
        mFileManagerAdapter.setFileManagerData(null);
        getSupportLoaderManager().restartLoader(loaderId,bundleForLoader, this);
        mActualPath = path;
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
    public Loader<ArrayList<File>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<File>>(this) {
            ArrayList<File> mFileManagerData = null;
            String actualPath = args.getString(FILEMANAGER_LOADER_PATH);

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
            public ArrayList<File> loadInBackground() {
                File mDirectory;
                File[] mFiles = null;
                ArrayList<File> inFiles = new ArrayList<>();
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
                        for (File file : mFiles) {
                            inFiles.add(file);
                        }
                        return inFiles;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return inFiles;
            }

            public void deliverResult(ArrayList<File> data) {
                mFileManagerData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<File>> loader, ArrayList<File> data) {
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
    public void onLoaderReset(Loader<ArrayList<File>> loader) {

    }

    @Override
    public void onClick(String actualItemPath) {
        Toast.makeText(this, actualItemPath, Toast.LENGTH_SHORT).show();
        loadPath(actualItemPath);
    }

    @Override
    public void onLongClick(String itemName) {
        Toast.makeText(this, "LongClick", Toast.LENGTH_SHORT).show();
    }

}
