package com.aircon.shibagenerator;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 照片同步作業
 * Created by ChenHsinHsuan on 2016/4/5.
 */
public class SyncPhotoTask extends AsyncTask {

    View view;
    File mediaStorageDir;

    //    int newPhotoCount = 0;
//    int updatePhotoCount = 0;
    boolean errorFlag = false;

    public SyncPhotoTask(View v, File iMediaStorageDir) {
        mediaStorageDir = iMediaStorageDir;
        view = v;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // 背景工作處理"前"需作的事

        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }


//        for (File theLocalPhoto : mediaStorageDir.listFiles()) {
//            System.out.println("刪除圖檔....");
//            theLocalPhoto.delete();
//        }
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        // 背景工作處理完"後"需作的事

        Snackbar.make(view, "圖檔同步完畢...", Snackbar.LENGTH_LONG).setAction("Action", null).show();

    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        // 背景工作處理"中"更新的事
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        // 背景工作被"取消"時作的事，此時不作 onPostExecute(String result)
    }

    @Override
    protected Object doInBackground(Object[] params) {
        // 再背景中處理的耗時工作
        try {
            DbxRequestConfig config = new DbxRequestConfig("sync_photo_task");
            //下載
            DbxClientV2 client = new DbxClientV2(config, Config.DROPBOX_APP_ACCESS_TOKEN);

            // Get files and folder metadata from Dropbox pics directory
            ListFolderResult result = client.files().listFolder("/pics");

            File theFile;
            FileOutputStream outputStream;
            for (Metadata metadata : result.getEntries()) {
                System.out.println(metadata.getPathLower());
                theFile = new File(mediaStorageDir.getPath(), metadata.getName());


                //如果檔案已經存在就跳過
                if (theFile.exists() && (theFile.length() == ((FileMetadata) metadata).getSize())) {
                    System.out.println(theFile.getName() + "未變更，跳過...");
                    continue;
                }

                //下載
                System.out.println(metadata.getName() + "下載中...");
                outputStream = new FileOutputStream(theFile);
                try {
                    FileMetadata download_metadata = client.files().downloadBuilder(metadata.getPathLower()).download(outputStream);
                } finally {
                    outputStream.close();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            errorFlag = true;
        }
        return null;
    }
}