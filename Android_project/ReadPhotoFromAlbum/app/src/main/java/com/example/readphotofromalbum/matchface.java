package com.example.readphotofromalbum;

import android.Manifest;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.face.MatchRequest;
import com.baidu.aip.util.Base64Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class matchface extends Fragment implements View.OnClickListener {
    public static final int SELECT_PHOTO1 = 2;
    public static final int SELECT_PHOTO2 = 3;

    private ImageView imageViewShow1;
    private ImageView imageViewShow2;
    public JSONObject res;
    public String path1, path2,fsres;
    Button btn1, btn2, btn_make;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v=inflater.inflate(R.layout.activity_facematch,container,false);
        btn1 =v.findViewById(R.id.photo1);
        btn2 =v.findViewById(R.id.photo2);
        btn_make =v.findViewById(R.id.make);
        imageViewShow1 =v.findViewById(R.id.iv_show1);
        imageViewShow2 =v.findViewById(R.id.iv_show2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn_make.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo1:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }else{
                    openAlbum1();
                }
                break;
            case R.id.photo2:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }else {
                    openAlbum2();
                }
                break;
            case R.id.make:
                if(path1!=null&&path2!=null){
                    sendRequestWithHttpClient();
                }else{
                    Toast.makeText(getActivity(), "图片获取失败", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    //方法：发送网络请求，获取数据。在里面开启子线程
    private void sendRequestWithHttpClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AipFace client=new AipFace("17728758","gkrWRq0zbK2mEXrfXQ6oMeNB","vv9ljKlnGzsD1PO3FGGi6uGoRVkFtN9P");
                client.setConnectionTimeoutInMillis(2000);
                client.setSocketTimeoutInMillis(6000);
                try {
                    //图片格式转换，百度接口要求传入的图片为BASE64格式
                    byte[] bytes1 = FileUtil.readFileByBytes(path1);
                    byte[] bytes2 = FileUtil.readFileByBytes(path2);
                    String image1 = Base64Util.encode(bytes1);
                    String image2 = Base64Util.encode(bytes2);
                    MatchRequest req1 = new MatchRequest(image1, "BASE64");
                    MatchRequest req2 = new MatchRequest(image2, "BASE64");
                    ArrayList<MatchRequest> requests = new ArrayList<MatchRequest>();
                    requests.add(req1);
                    requests.add(req2);
                    //返回JSON字符串
                    res = client.match(requests);
                    System.out.println(res);
                    Log.e("name",res.toString());
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = res;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = 2;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    private Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int score = 0;
            if (msg.what == 1) {
                JSONObject res = (JSONObject) msg.obj;
                try {
                    JSONObject result = res.getJSONObject("result");
                    score = result.getInt("score");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(score>=75.00){
                    fsres = "这两个人是同一个人";
                }
                else {
                    fsres = "这两个人不是同一个人";
                }
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                String[] mItems = {fsres,"相似度"+score+"%"};
                alertDialog.setTitle("人脸识别报告").setItems(mItems, null).create().show();
            }
        }
    };

    //按钮一事件
    public void openAlbum1() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PHOTO1);
    }

    //按钮二事件
    public void openAlbum2() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PHOTO2);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //打开相册后返回
            case SELECT_PHOTO1:
                if (resultCode == RESULT_OK) {
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT > 19) {
                        path1 = handleImgeOnKitKat(data);
                        Bitmap bmap1 = displayImage(path1);//获取图片Bitmap显示到对应的控件上
                        if (bmap1 != null) {
                            imageViewShow1.setImageBitmap(bmap1);
                        }
                    } else {
                        Toast.makeText(getActivity(), "图片获取失败", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case SELECT_PHOTO2:
                if (resultCode == RESULT_OK) {
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT > 19) {
                        //4.4及以上系统使用这个方法处理图片
                        path2 = handleImgeOnKitKat(data);
                        Bitmap bmap2 = displayImage(path2);
                        if (bmap2 != null) {
                            imageViewShow2.setImageBitmap(bmap2);
                        }
                        else {
                            Toast.makeText(getActivity(), "图片获取失败", Toast.LENGTH_LONG).show();
                        }
                    }
                }break;
        }
    }

    //API19以上的图片处理方法
    private String handleImgeOnKitKat(Intent data) {
        String path=null;
        Uri uri = data.getData();
        Log.d("uri=intent.getData :", "" + uri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(getActivity(), uri)) {
                String docId = DocumentsContract.getDocumentId(uri);        //数据表里指定的行
                Log.d("getDocumentId(uri) :", "" + docId);
                Log.d("uri.getAuthority() :", "" + uri.getAuthority());
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String id = docId.split(":")[1];
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    path = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    path = getImagePath(contentUri, null);
                }

            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                path = getImagePath(uri, null);

            }
        }
        Log.v("path:",path);
        return path;
        //displayImage(path);
    }

    private Bitmap displayImage(String imagePath) {
        Bitmap orc_bitmap = null;
        if (!TextUtils.isEmpty(imagePath)) {
            orc_bitmap = BitmapFactory.decodeFile(imagePath);//获取图片
        }
        return orc_bitmap;
    }
    //获取图片的真实路径
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
