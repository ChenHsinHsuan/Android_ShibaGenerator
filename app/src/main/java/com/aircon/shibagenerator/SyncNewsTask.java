package com.aircon.shibagenerator;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * 最新消息同步作業
 * Created by ChenHsinHsuan on 2016/4/5.
 */
public class SyncNewsTask extends AsyncTask {
    Context mContext;
    View view;
    File rootStorageDir;
    boolean isUpdate = false;

    public SyncNewsTask(View v, File iRootStorageDir, Context iContext) {
        mContext = iContext;
        rootStorageDir = iRootStorageDir;
        view = v;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // 背景工作處理"前"需作的事

    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        // 背景工作處理完"後"需作的事

        if (isUpdate) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("最新消息");
            builder.setIcon(R.drawable.ic_message_black_24dp);

            FileInputStream is;
            BufferedReader reader;
            File file = new File(rootStorageDir + File.separator + "news.txt");
            StringBuffer sb = new StringBuffer();
            try {
                if (file.exists()) {
                    is = new FileInputStream(file);
                    reader = new BufferedReader(new InputStreamReader(is, "utf8"));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            sb.append(line + System.lineSeparator());
                        } else {
                            sb.append(line + "\n");
                        }
                    }
                    reader.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            builder.setMessage(sb.toString());
            builder.setPositiveButton("我知道囉", null);
            final AlertDialog alert = builder.create();
            alert.show();
        }
        System.out.println("SyncNewsTask finish....");

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

        DbxRequestConfig config = new DbxRequestConfig("sync_news_task");

        try {

            DbxClientV2 client = new DbxClientV2(config, Config.DROPBOX_APP_ACCESS_TOKEN);

            Metadata theRemoteNews = client.files().getMetadata("/news.txt");
            File theLocalNewsFile = new File(rootStorageDir.getPath(), "news.txt");
            

            if (theLocalNewsFile.exists() && theLocalNewsFile.length() == ((FileMetadata) theRemoteNews).getSize()) {
                System.out.println(theLocalNewsFile.getName() + "未變更，跳過...");
                isUpdate = false;
            } else {
                System.out.println("News have updated...");
                FileOutputStream outputStream = new FileOutputStream(theLocalNewsFile);
                try {
                    FileMetadata download_metadata = client.files().downloadBuilder(theRemoteNews.getPathLower()).download(outputStream);
                    isUpdate = true;
                } finally {
                    outputStream.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(view, R.string.connect_internet_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

        return null;
    }
}