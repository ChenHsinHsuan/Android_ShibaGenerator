package com.aircon.shibagenerator;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aircon.shibagenerator.com.aircon.imgur.imgurmodel.ImageResponse;
import com.aircon.shibagenerator.com.aircon.imgur.imgurmodel.Upload;
import com.aircon.shibagenerator.com.aircon.imgur.services.UploadService;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.fabric.sdk.android.Fabric;
import me.panavtec.drawableview.DrawableView;
import me.panavtec.drawableview.DrawableViewConfig;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import yuku.ambilwarna.AmbilWarnaDialog;


public class MainActivity extends AppCompatActivity {

    private ImageView shibaImage;
    private EditText inputText;
    private TextView demoText;
    private SeekBar seekBar;
    private Toolbar toolbar;
    private AdRequest adRequest;
    private RelativeLayout imageLayout;
    private ProgressDialog dialog;
    private DrawableView paintView;
    private boolean shareClick = false;
    private boolean imgurClick = false;
    private boolean saveToAlbumClick = false;

    File[] myAlbumFiles;
    File rootStorageDir;
    File mediaStorageDir;
    int photoIndex = 1;
    boolean isVertical = true;

    private SyncPhotoTask syncPhotoTask;
    private SyncNewsTask syncNewsTask;


    public static final int RESULT_SELECT_IMAGE = 3000;
    private static final int RESULT_LOAD_IMAGE = 1009;
    /*用来标识请求照相功能的activity*/
    private static final int CAMERA_WITH_DATA = 3023;
    /*用来标识请求gallery的activity*/
    private static final int PHOTO_PICKED_WITH_DATA = 3021;

    /*用來標示 從gallery 選完照片crop後的activity*/
    private static final int PHOTO_CROP_WITH_DATA = 5566;

    /*拍照的照片存储位置*/
    private static final File PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    private File mCurrentPhotoFile;//照相机拍照得到的图片

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    InterstitialAd mInterstitialAd;
    private DrawableViewConfig paintConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("");
        Fabric.with(this, new Crashlytics());
        findView();
        prepareVariable();
        bindEvent();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                askAccessExternalStoragePermission();
            } else {

                syncNewsTask = new SyncNewsTask(imageLayout, rootStorageDir, MainActivity.this);
                syncNewsTask.execute();


                syncPhotoTask = new SyncPhotoTask(imageLayout, mediaStorageDir);
                syncPhotoTask.execute();
            }
        } else {

            syncNewsTask = new SyncNewsTask(imageLayout, rootStorageDir, MainActivity.this);
            syncNewsTask.execute();


            syncPhotoTask = new SyncPhotoTask(imageLayout, mediaStorageDir);
            syncPhotoTask.execute();
        }


    }


    private void findView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        shibaImage = (ImageView) findViewById(R.id.imageView);
        inputText = (EditText) findViewById(R.id.editText);
        demoText = (TextView) findViewById(R.id.textView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        imageLayout = (RelativeLayout) findViewById(R.id.imageLayout);
//        paintView = (DrawableView)findViewById(R.id.paintView);
    }

    private void prepareVariable() {

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(Config.ADUNIT_ID);
        requestNewInterstitial();

        rootStorageDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + getApplicationContext().getPackageName());
        mediaStorageDir = new File(rootStorageDir + File.separator + "Files");

        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }

        myAlbumFiles = mediaStorageDir.listFiles();


//        paintConfig = new DrawableViewConfig();
//        paintConfig.setStrokeColor(ContextCompat.getColor(this, android.R.color.black));
//        paintConfig.setShowCanvasBounds(true);
//        paintConfig.setStrokeWidth(20.0f);
//        paintConfig.setMinZoom(1.0f);
//        paintConfig.setMaxZoom(3.0f);
//        paintConfig.setCanvasHeight(imageLayout.getHeight());
//        paintConfig.setCanvasWidth(imageLayout.getWidth());
//        paintView.setConfig(paintConfig);
    }


    private void bindEvent() {

        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                demoText.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (demoText.getText().length() == 0) {
                    demoText.setText("你想說什麼。。");
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 100) {
                    demoText.setTextSize(100);
                } else if (progress < 20) {
                    demoText.setTextSize(20);
                } else {
                    demoText.setTextSize(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                if (shareClick) {
                    shareToAnotherAPPs();
                } else if (imgurClick) {
                    uploadToImgur();
                } else if (saveToAlbumClick) {
                    genPhotoFile();

                    saveToAlbumClick = false;
                    Snackbar.make(shibaImage, R.string.the_photo_is_save_in_your_album, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                requestNewInterstitial();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //最新消息
        if (id == R.id.action_news) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

            return true;
        }


        //換圖片
        if (id == R.id.action_change) {
            Intent i = new Intent();
            i.setClass(this, PhotoListActivity.class);
            i.putExtra("mediaStorageDir", mediaStorageDir.getPath());
            startActivityForResult(i, RESULT_SELECT_IMAGE);
            return true;
        }

        //換顏色
        if (id == R.id.action_color) {
            AmbilWarnaDialog theAmbilWarnaDialog = new AmbilWarnaDialog(this, demoText.getCurrentTextColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    // color is the color selected by the user.
                    demoText.setTextColor(color);
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // cancel was selected by the user
                }
            });
            theAmbilWarnaDialog.show();
        }


        //與伺服器同步圖片
        if (id == R.id.action_sync) {
            Snackbar.make(shibaImage, R.string.sync_in_background, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            syncPhotoTask = new SyncPhotoTask(imageLayout, mediaStorageDir);
            syncPhotoTask.execute();
        }


        //存至相片集
        if (id == R.id.action_save_album) {
            if (inputText.getText().toString().trim().length() == 0) {
                Snackbar.make(shibaImage, R.string.i_dont_know_what_you_want_to_say, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                saveToAlbumClick = true;
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    genPhotoFile();
                    saveToAlbumClick = false;
                    Snackbar.make(shibaImage, R.string.the_photo_is_save_in_your_album, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        }


        //上傳imgur拿短網址
        if (id == R.id.action_upload_imgur) {
            if (inputText.getText().toString().trim().length() == 0) {
                Snackbar.make(inputText, R.string.i_dont_know_what_you_want_to_say, Snackbar.LENGTH_LONG).setAction("Action", null).show();

            } else {

                imgurClick = true;

                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    uploadToImgur();
                }
            }
        }


        //分享到其他APP
        if (id == R.id.action_share) {
            if (inputText.getText().toString().trim().length() == 0) {
                Snackbar.make(shibaImage, R.string.i_dont_know_what_you_want_to_say, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                shareClick = true;
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    shareToAnotherAPPs();
                }
            }
        }


        //切煥文字排版
        if (id == R.id.action_align) {
            isVertical = !isVertical;

            ViewGroup.LayoutParams lp = demoText.getLayoutParams();
            if (isVertical) {
                item.setIcon(R.drawable.ic_vertical_align_bottom_white_24dp);

                lp.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                demoText.setLayoutParams(lp);
                demoText.setGravity(Gravity.CENTER);
                demoText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                inputText.setSingleLine(true);
                inputText.setHint("點我輸入文字");
            } else {
                item.setIcon(R.drawable.ic_trending_flat_white_24dp);

                lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                demoText.setLayoutParams(lp);
                demoText.setGravity(Gravity.LEFT | Gravity.TOP);
                demoText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                inputText.setSingleLine(false);
                inputText.setHint("點我輸入文字\n點選(↩)︎可以排版");
            }
        }


        if (id == R.id.action_choice_photo) {
            doPickPhotoAction();
        }

        return super.onOptionsItemSelected(item);
    }


    public String getDateTime() {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        Date date = new Date();
        String strDate = sdFormat.format(date);
        return strDate;
    }


    //admob handle
    private void requestNewInterstitial() {
        adRequest = new AdRequest.Builder().addTestDevice("D743BC1D31994BE696058A955990EE44").build();
        mInterstitialAd.loadAd(adRequest);
    }


    private File genPhotoFile() {
        String appDirectoryName = "ShibaGenerator";
        File imageRoot = new File(getApplicationContext().getDir(Environment.DIRECTORY_DCIM, MODE_PRIVATE), appDirectoryName);
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            imageRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), appDirectoryName);
        }

        if (!imageRoot.exists()) {
            imageRoot.mkdirs();
        }

        File theOutPutFile = new File(imageRoot, "shiba_" + getDateTime() + ".jpg");

        imageLayout.setDrawingCacheEnabled(true);
        Bitmap shibaImageBM = imageLayout.getDrawingCache();

        try {
            OutputStream outStream = new FileOutputStream(theOutPutFile);
            shibaImageBM.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

            outStream.flush();
            outStream.close();
            imageLayout.destroyDrawingCache();

            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{theOutPutFile.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Snackbar.make(shibaImage, e.getMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(shibaImage, e.getMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

        return theOutPutFile;
    }


    /**
     * 複製到剪貼簿
     *
     * @param str
     */
    private void copyToClipboard(String str) {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(str);
            Log.e("version", "1 version");
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text label", str);
            clipboard.setPrimaryClip(clip);
            Log.e("version", "2 version");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Config.EXTERNAL_STORAGE_REQUESTCODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    syncNewsTask = new SyncNewsTask(imageLayout, rootStorageDir, MainActivity.this);
                    syncNewsTask.execute();

                    syncPhotoTask = new SyncPhotoTask(imageLayout, mediaStorageDir);
                    syncPhotoTask.execute();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.you_dont_agree_this_app_access_your_storage)
                            .setMessage(R.string.app_cant_be_used)
                            .setPositiveButton(R.string.ok_i_know, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        askAccessExternalStoragePermission();
                                    }
                                }
                            }).show();
                }
                return;
            }
        }
    }


    /**
     * 取得存取授權
     */
    private void askAccessExternalStoragePermission() {
        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
        ActivityCompat.requestPermissions(MainActivity.this, perms, Config.EXTERNAL_STORAGE_REQUESTCODE);
    }


    private void shareToAnotherAPPs() {
        //分享至其他APP
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/*");
        Uri uri = Uri.fromFile(genPhotoFile());
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.what_words_do_you_want_to_share)));
        shareClick = false;
        Snackbar.make(shibaImage, R.string.the_photo_is_save_in_your_album, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private void uploadToImgur() {
        dialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.upload_url), getResources().getString(R.string.please_wait), true);
        //上傳imgur
        Upload upload = new Upload();
        upload.image = genPhotoFile();
                    /*
                      Start upload
                     */
        new UploadService(getApplicationContext()).Execute(upload, new UiCallback());

        imgurClick = false;
    }


    private class UiCallback implements Callback<ImageResponse> {
        @Override
        public void success(ImageResponse imageResponse, Response response) {
            dialog.dismiss();
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(imageResponse.data.link)
                    .setMessage(getResources().getString(R.string.already_copy))
                    .setPositiveButton(getResources().getString(R.string.ok_i_know), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            copyToClipboard(imageResponse.data.link);
        }

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null) {
                dialog.dismiss();
                Snackbar.make(shibaImage, getResources().getString(R.string.connect_internet_error), Snackbar.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 自定義圖檔
     *
     * @date 2016-04-29
     */


    private void doPickPhotoAction() {
        Context context = MainActivity.this;
        // Wrap our context to inflate list items using correct theme
        final Context dialogContext = new ContextThemeWrapper(context,
                android.R.style.Theme_Light);
        String cancel = "返回";
        String[] choices;
        choices = new String[2];
        choices[0] = getString(R.string.take_photo);  //拍照
        choices[1] = getString(R.string.pick_photo);  //从相册中选择
        final ListAdapter adapter = new ArrayAdapter<String>(dialogContext, android.R.layout.simple_list_item_1, choices);

        final AlertDialog.Builder builder = new AlertDialog.Builder(
                dialogContext);
        builder.setTitle(R.string.attachToContact);
        builder.setSingleChoiceItems(adapter, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0: {
                                String status = Environment.getExternalStorageState();
                                if (status.equals(Environment.MEDIA_MOUNTED)) {//判断是否有SD卡
                                    doTakePhoto();// 用户点击了从照相机获取
                                } else {
                                    Toast.makeText(getApplicationContext(), "没有SD卡", Toast.LENGTH_LONG).show();
                                }
                                break;

                            }
                            case 1:
                                doPickPhotoFromGallery();// 从相册中去获取
                                break;
                        }
                    }
                });
        builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        builder.create().show();
    }


    // 请求Gallery程序
    protected void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Photo Picker Not Found...", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * 拍照获取图片
     */
    protected void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();// 创建照片的存储目录
            mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
            intent.putExtra("aspectX", 3);
            intent.putExtra("aspectY", 4);
            intent.putExtra("scale", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentPhotoFile));
            startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Photo Picker Not Found...",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 用当前时间给取得的图片命名
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMddHHmmss");
        return dateFormat.format(date) + ".jpg";
    }


    // 因为调用了Camera和Gally所以要判断他们各自的返回情况,他们启动时是这样的startActivityForResult
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        System.out.println("requestCode:" + requestCode);
        switch (requestCode) {

            case RESULT_LOAD_IMAGE: {
                final Bitmap photo = data.getParcelableExtra("data");
                shibaImage.setImageBitmap(photo);
                break;
            }

            // 调用Gallery返回的
            case PHOTO_PICKED_WITH_DATA: {
                Uri uri = data.getData();
                doCropPhoto(uri);
                shibaImage.setImageURI(uri);
                break;
            }

            // 照相机程序返回的,再次调用图片剪辑程序去修剪图片
            case CAMERA_WITH_DATA: {
                Uri photoUri = Uri.fromFile(mCurrentPhotoFile);
                doCropPhoto(photoUri);
                break;
            }

            case PHOTO_CROP_WITH_DATA: {
                Uri uri = data.getData();
                if (uri != null) {
                    //For Android 5.x
                    Bitmap myImg = BitmapFactory.decodeFile(uri.getPath());
                    shibaImage.setImageBitmap(myImg);
                } else {
                    //For Android 6.x
                    final Bitmap photo = data.getParcelableExtra("data");
                    System.out.println("photo:" + photo.getByteCount());
                    shibaImage.setImageBitmap(photo);
                }


                break;
            }


            case RESULT_SELECT_IMAGE: {
                String imageFilePath = data.getStringExtra("imageFilePath");
                shibaImage.setImageBitmap(BitmapFactory.decodeFile(imageFilePath));
                break;
            }
        }
    }

    protected void doCropPhoto(Uri photoUri) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(photoUri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 3);
            intent.putExtra("aspectY", 4);
            intent.putExtra("outputX", 600);
            intent.putExtra("outputY", 800);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, PHOTO_CROP_WITH_DATA);
        } catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();

        } catch (Exception e) {
            Toast.makeText(this, "Photo pick not found...", Toast.LENGTH_LONG).show();
        }
    }


}
