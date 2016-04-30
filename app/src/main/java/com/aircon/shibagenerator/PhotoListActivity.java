package com.aircon.shibagenerator;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;

public class PhotoListActivity extends AppCompatActivity {

    private GridView mGridView;
    private File mediaStorageDir;
    private ProgressBar mProgressBar;
    private AirGridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);
        findView();
        prepareVariable();


        bindEvent();


        //Start download
        new AsyncPhotoTask().execute();
        mProgressBar.setVisibility(View.VISIBLE);


    }

    private void findView() {
        mGridView = (GridView) findViewById(R.id.photo_gridview);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void prepareVariable() {
        mediaStorageDir = new File(getIntent().getStringExtra("mediaStorageDir"));

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new AirGridViewAdapter(this, R.layout.grid_item, mGridData);
        mGridView.setAdapter(mGridAdapter);

    }


    private void bindEvent(){
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("imageFilePath", mGridData.get(position).getImageFile().getPath());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }


    //Downloading data asynchronously
    public class AsyncPhotoTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            int results = 0;
            GridItem item;
            for (File thePhoto : mediaStorageDir.listFiles()) {
                item = new GridItem();
                item.setTitle(thePhoto.getName().substring(0, thePhoto.getName().indexOf(".")));
                item.setImageFile(thePhoto);
                mGridData.add(item);
            }

            return results;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mGridAdapter.setGridData(mGridData);
            mProgressBar.setVisibility(View.GONE);
        }
    }



}
