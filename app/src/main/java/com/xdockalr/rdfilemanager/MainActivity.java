package com.xdockalr.rdfilemanager;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        FileManagerAdapter.FileManagerAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<ArrayList<File>>,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final int FILEMANAGER_LOADER_ID = 0;
    private static final String FILEMANAGER_LOADER_PATH = "LOADER_PATH";
    private static final String FILEMANAGER_ACTUAL_PATH = "ACTUAL_PATH";
    private static final String FILEMANAGER_SELECTED_ITEMS = "SELECTED_ITEMS";

    protected static final String BASE_EXTERNAL_PATH = Environment.getExternalStorageDirectory().toString();

    private RecyclerView mRecycleView;
    private FileManagerAdapter mFileManagerAdapter;
    private TextView mErrorText;
    private ProgressBar mProgressBar;

    public static String mActualPath;

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
        mFileManagerAdapter = new FileManagerAdapter(this, this);
        mRecycleView.setAdapter(mFileManagerAdapter);

        runLayoutAnimation(mRecycleView);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        if(savedInstanceState != null) {
            mActualPath = savedInstanceState.getString(FILEMANAGER_ACTUAL_PATH);
            mFileManagerAdapter.setSelectedItemsArray(savedInstanceState.getStringArrayList(FILEMANAGER_SELECTED_ITEMS));
        }
        else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String prefDefaultPath = sharedPreferences.getString(getResources().getString(R.string.pref_default_path_key),null);
            if (prefDefaultPath != null && (new File(prefDefaultPath).isDirectory())) {
                mActualPath = prefDefaultPath;
            }
            else {
                prefDefaultPath = BASE_EXTERNAL_PATH;
                mActualPath = prefDefaultPath;
            }
        }

        if (isWriteExtStoragePermissionGranted()) {
             loadPath(mActualPath);
        }
        else {
            showErrorMessage();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(FILEMANAGER_ACTUAL_PATH, mActualPath);
        savedInstanceState.putStringArrayList(FILEMANAGER_SELECTED_ITEMS, mFileManagerAdapter.getSelectedItemsArray());
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
            return true;
        }
        else if (itemId == R.id.action_settings){
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if ((mActualPath.equals(BASE_EXTERNAL_PATH)) || (mErrorText.getVisibility() == View.VISIBLE)){
            super.onBackPressed();
        }
        else {
            int indexOfLastSlash = mActualPath.lastIndexOf(File.separator);
            String previousFolder = mActualPath.substring(0,indexOfLastSlash);
            loadPath(previousFolder);
        }
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
            runLayoutAnimation(mRecycleView);
            showDataView();

            Collections.sort(data, new SortFileName());
            Collections.sort(data, new SortFolder());

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
        loadPath(actualItemPath);
    }

    @Override
    public void onLongClick(String itemName) {
        //Toast.makeText(this, "LongClick", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions != null && permissions.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                loadPath(mActualPath);
            }
        }
        else if (permissions != null && permissions.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                loadPath(mActualPath);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void loadPath(String path) {
        if (new File(path).isDirectory()) {
            Bundle bundleForLoader = new Bundle();
            bundleForLoader.putString(FILEMANAGER_LOADER_PATH, path);
            mFileManagerAdapter.setFileManagerData(null);
            getSupportLoaderManager().restartLoader(FILEMANAGER_LOADER_ID, bundleForLoader, this);
            mActualPath = path;
        }
        else if (new File(path).isFile()){
            String mimeType = MimeType.getTypeFromName(path);
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File file = new File(String.valueOf(path));
            Uri fileUri = FileProvider.getUriForFile (this, BuildConfig.APPLICATION_ID + ".provider", file);
            newIntent.setDataAndType(fileUri, mimeType);

            try {
                startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, getResources().getString(R.string.file_open_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showDataView() {
        mRecycleView.setVisibility(View.VISIBLE);
        mErrorText.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage() {
        mRecycleView.setVisibility(View.INVISIBLE);
        mErrorText.setVisibility(View.VISIBLE);
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    public  boolean isReadExtStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                //Toast.makeText(this, getResources().getString(R.string.permission_revoked), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else {
            return true;
        }
    }

    public boolean isWriteExtStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                //Toast.makeText(this, getResources().getString(R.string.permission_revoked), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else {
            return true;
        }
    }

    public class SortFileName implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            return file1.getName().compareTo(file2.getName());
        }
    }

    public class SortFolder implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            if (file1.isDirectory() == file2.isDirectory())
                return 0;
            else if (file1.isDirectory() && !file2.isDirectory())
                return -1;
            else
                return 1;
        }
    }


}
