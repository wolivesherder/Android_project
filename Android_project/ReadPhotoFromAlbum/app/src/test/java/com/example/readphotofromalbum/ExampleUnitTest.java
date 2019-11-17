package com.example.readphotofromalbum;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.face.MatchRequest;
import com.baidu.aip.util.Base64Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void test(){
        AipFace client=new AipFace("17728758","gkrWRq0zbK2mEXrfXQ6oMeNB","vv9ljKlnGzsD1PO3FGGi6uGoRVkFtN9P");
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(6000);
            //图片格式转换，百度接口要求传入的图片为BASE64格式
            byte[] bytes1 = new byte[1024*1000];
            byte[] bytes2 = new byte[1024*1000];
            try {
                bytes1 = FileUtil.readFileByBytes("C:\\Users\\33036\\Desktop\\timg (1).jpg");
                bytes2 = FileUtil.readFileByBytes("C:\\Users\\33036\\Desktop\\timg (1).jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }

            String image1 = Base64Util.encode(bytes1);
            String image2 = Base64Util.encode(bytes2);
            MatchRequest req1 = new MatchRequest(image1, "BASE64");
            MatchRequest req2 = new MatchRequest(image2, "BASE64");
            ArrayList<MatchRequest> requests = new ArrayList<MatchRequest>();
            requests.add(req1);
            requests.add(req2);
            //返回JSON字符串
            JSONObject jsonObject = client.match(requests);
        try {
            System.out.println(jsonObject.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}