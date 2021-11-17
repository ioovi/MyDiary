package com.example.experiment2;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.icu.text.LocaleDisplayNames;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddActivity extends AppCompatActivity{

    public static final int TAKE_PHOTO = 1;
    private Uri imageUri;
    public static String imagePath =null; //定义一个全局变量，把图片路径变为string保存到数据库中
    public static final int CHOOSE_PHOTO = 2;
    private EditText title;
    private EditText body;
    private CreateSQL dbHelper;
    public ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        dbHelper =new CreateSQL(this,"DiaryBook.db",null,2);
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        //dbHelper.onUpgrade(db,2,2);

        Button back = findViewById(R.id.btn_back);
        Button commit = findViewById(R.id.commit);
        ImageButton btn_takePhoto = findViewById(R.id.btn_takeP);
        title=findViewById(R.id.title_text);
        body=findViewById(R.id.body_text);
        ImageButton btn3 = findViewById(R.id.btn_openP);
        imageView= findViewById(R.id.picture);
        Button btn_del = findViewById(R.id.btn_del);

        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = getIntent().getStringExtra("username");
                SQLiteDatabase db=dbHelper.getWritableDatabase();
                ContentValues values=new ContentValues();

                long numberOfRows = DatabaseUtils.queryNumEntries(db, "diary")+1;
                values.put("id",numberOfRows);
                values.put("author",name);
                values.put("title",title.getText().toString());
                values.put("body",body.getText().toString());
                values.put("photoURL",imagePath);
                java.util.Date data=new java.util.Date();
                long time =data.getTime();
                values.put("time",time);
                db.insert("diary",null,values);
                Toast.makeText(AddActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePath=null;
                imageView.setImageDrawable(null);
            }
        });

        btn_takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                 利用系统自带的相机应用:拍照
//                 此处这句intent的值设置关系到后面的onActivityResult中会进入那个分支，即关系到data是否为null
//                 如果此处指定，则后来的data为null
//                 只有指定路径才能获取原图

                //创建file对象，存储拍照后的照片 （getExternalCacheDir()存放到当前应用缓存数据位置）
                File outputImage = new File(getExternalCacheDir(),"IMG_" + System.currentTimeMillis() + ".jpg");
                Log.d("file--------",outputImage.toString());
                try {
                    if (outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }

                imageUri = FileProvider.getUriForFile(AddActivity.this,"com.example.experiment2.fileProvider",outputImage);
                //启动相机
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
            }

        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(AddActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    openAlbum();
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //拍照完以后返回到这儿
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {//如果拍照成功
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));//把这张照片转换为Bitmap对象
                        saveToSystemGallery(bitmap); //将照片保存到相册
                        imageView.setImageBitmap(bitmap);//设置到ImageView显示出来
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(Build.VERSION.SDK_INT >=19) {
                    //4.4及以上系统使用这个方法处理图片
                    handleImageOnKitKat(data);
                }else {
                    handleImageBeforeKitKat(data);
                }
                break;
            default:
                break;
        }
    }

    //把拍照照片存到相册
    public void saveToSystemGallery(Bitmap bmp) {


        File file = new File(getExternalCacheDir(),"output_image" + System.currentTimeMillis() + ".jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        imagePath = uri.getPath();
        intent.setData(uri);
        Log.d("zzzzzzzzzzz1",imagePath);
        Log.d("zzzzzzzzzzz2",uri.toString());
        sendBroadcast(intent);// 发送广播，通知图库更新
    }


    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);//打开相册
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                }else {
                    Toast.makeText(this,"you denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        Uri uri = data.getData();
        //Log.d("TAG", "handleImageOnKitKat: uri is " + uri);

        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath); // 根据图片路径显示图片
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }


}