package com.example.readphotofromalbum;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.util.Base64Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class detect extends Fragment {
    private Button detect;
    private Uri imageUri;
    private ImageView picture;
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private JSONObject res=null;
    private String path;
    private List<Map<String, Object>> list = null;
    private String face_age=null,face_gender=null,face_race=null,face_beauty=null,face_expression=null;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v=inflater.inflate(R.layout.activity_main,container,false);
        Button takePhoto = v.findViewById(R.id.take_photo);
        Button chooseFromAlbum = v.findViewById(R.id.choose_from_album);
        detect=v.findViewById(R.id.jiance);
        picture =v.findViewById(R.id.picture);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File outputImage = new File(getActivity().getExternalCacheDir(),"output_image.jpg");
                try {
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT>=24){
                    imageUri = FileProvider.getUriForFile(getActivity(), "com.example.readphotofromalbum.fileprovider",outputImage);
                }else{
                    imageUri = Uri.fromFile(outputImage);
                }
                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });

        chooseFromAlbum.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.
                        PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }
            }
        });
        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                res=null;
                if(path!=null){
                    list = new ArrayList<Map<String, Object>>();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] bytes = new byte[0];
                            try {
                                bytes = FileUtil.readFileByBytes(path);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String image = Base64Util.encode(bytes);
                            HashMap<String,String> options=new HashMap<>();
                            options.put("face_field","age,gender,race,beauty,expression,type");
                            AipFace client=new AipFace("17728758","gkrWRq0zbK2mEXrfXQ6oMeNB","vv9ljKlnGzsD1PO3FGGi6uGoRVkFtN9P");
                            client.setConnectionTimeoutInMillis(2000);
                            client.setSocketTimeoutInMillis(6000);

                            res=client.detect(image,"BASE64",options);
                            try{
                                Message message = Message.obtain();
                                message.what = 1;
                                message.obj = res;
                                handler.sendMessage(message);
                            }catch (Exception e){
                                e.printStackTrace();
                                Message message = Message.obtain();
                                message.what = 2;
                                handler.sendMessage(message);
                            }
                        }
                    }).start();
                }else{
                    Toast.makeText(getActivity(), "没有选择图片", Toast.LENGTH_SHORT).show();
                }

            }
        });
        return v;
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    Toast.makeText(getActivity(),"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode==RESULT_OK){
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK){
                    if(Build.VERSION.SDK_INT>=19){
                        handleImageOnKitKat(data);
                    }else{
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(getActivity(),uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                path = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                path = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            path = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            path = uri.getPath();
        }
        displayImage(path);
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri,String selection){
        String path = null;
        Cursor cursor = getActivity().getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath){
        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        }else{
            Toast.makeText(getActivity(),"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }

    private Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int num=0;
            if (msg.what == 1) {
                JSONObject res = (JSONObject) msg.obj;
                try {
                    JSONObject result = res.getJSONObject("result");
                    num = result.getInt("face_num");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (num>= 1) {
                    try {
                        JSONObject js = new JSONObject(res.optString("result"));
                        JSONArray face_list = js.optJSONArray("face_list");
                        face_age = face_list.optJSONObject(0).optString("age");
                        face_gender = face_list.optJSONObject(0).optString("gender");
                        if (face_gender.equals("female")) {
                            face_gender = "女";
                        } else {
                            face_gender = "男";
                        }
                        JSONObject face_race1 = face_list.optJSONObject(0).optJSONObject("race");
                        face_race = face_race1.optString("type");
                        if (face_race.equals("yellow")) {
                            face_race = "黄种人";
                        } else if (face_race.equals("white")) {
                            face_race = "白种人";
                        } else if (face_race.equals("black")) {
                            face_race = "黑种人";
                        } else if (face_race.equals("arabs")) {
                            face_race = "阿拉伯人";
                        }
                        int express  = 0;
                        try{
                        express = Integer.parseInt(face_list.optJSONObject(0).optString("expression"));}
                        catch (Exception e){
                            express = 0;
                        }
                        if (express == 0) {
                            face_expression = "无";
                        } else if (express == 1) {
                            face_expression = "微笑";
                        } else {
                            face_expression = "大笑";
                        }
                        face_beauty = face_list.optJSONObject(0).optString("beauty");
                        double beauty = Math.ceil(Double.parseDouble(face_beauty) + 25);
                        if (beauty >= 100) {
                            beauty = 99.0;
                        } else if (beauty < 70) {
                            beauty += 10;
                        } else if (beauty > 80 && beauty < 90) {
                            beauty += 5;
                        } else if (beauty >= 90 && beauty < 95) {
                            beauty += 2;
                        }
                        face_beauty = String.valueOf(beauty);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    String[] mItems = {"性别：" + face_gender, "年龄：" + face_age, "肤色：" + face_race, "颜值：" + face_beauty, "笑容：" + face_expression};
                    alertDialog.setTitle("人脸识别报告").setItems(mItems, null).create().show();
                }
            }
        }
    };

}
