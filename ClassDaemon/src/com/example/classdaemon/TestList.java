package com.example.classdaemon;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TestList extends Activity {
	public static List<String> arrlist = new ArrayList<String>();
	public static ArrayList numlist = new ArrayList();
	public static ListView list1;
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what==GlobalData.success) {
				ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getBaseContext(), R.layout.array_item, arrlist);
				list1.setAdapter(adapter1);
			}
			else {
				Log.e("handler error", "...........");
			}
		}
	};
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testlist);
		list1 = (ListView) findViewById(R.id.list1);
		
		ListClickListener l = new ListClickListener();
		list1.setOnItemClickListener(l);
		
		new Thread() {
			public void run() {
				try {
					HttpClient httpClient = new DefaultHttpClient();
					HttpPost request = new HttpPost(GlobalData.baseUrl + "/smartClass/student/course/test/");
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair("coursename", GlobalData.getCourse());
					paramList.add(param);
					request.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
					
					if(null != GlobalData.sessionid) {
						request.setHeader("Cookie", "sessionid=" + GlobalData.sessionid);
					}
					
				HttpClient client = new DefaultHttpClient();
				HttpResponse httpResponse = client.execute(request);
				
				CookieStore mCookieStore = ((DefaultHttpClient)client).getCookieStore();
				List<Cookie> cookies = mCookieStore.getCookies();
				for(int i = 0; i < cookies.size(); i++) {
					if("sessionid".equals(cookies.get(i).getName())) {
						GlobalData.sessionid = cookies.get(i).getValue();
					}
				}
				
				String retSrc = EntityUtils.toString(httpResponse.getEntity());
				
				JSONObject result = new JSONObject(retSrc);
				Message message = new Message();
				if("1".equals(result.getString("result"))) {
					message.what = GlobalData.success;
					Log.e("message.message", result.getString("message"));
					Log.e("testlist", result.getString("testlist"));
					for(int i = 0; i < result.getJSONArray("testlist").length(); i++) {
						arrlist.add(result.getJSONArray("testlist").getJSONArray(i).getString(0));
						numlist.add(result.getJSONArray("testlist").getJSONArray(i).getInt(1));
						Log.e("JSONArray", result.getJSONArray("testlist").getJSONArray(i).getString(0));
						Log.e("JSONArray", result.getJSONArray("testlist").getJSONArray(i).getInt(1)+"");
					}
					handler.sendMessage(message);
				}
				else {
					Log.e("network failed", ".................");
				}
				}
				catch(Exception e){
				}
			}
		}.start();
		
	}
	
	class ListClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.e("item selected", list1.getItemAtPosition(position)+"");
			// 设置AnswerQuestion界面需要的测试名称和题目数量
			GlobalData.setTest(arrlist.get(position).toString());
			GlobalData.setTestNum((int) numlist.get(position));
			Intent bintent = new Intent(com.example.classdaemon.TestList.this, com.example.classdaemon.AnswerQuestion.class);
			startActivityForResult(bintent, 0);			
		}
	}
}
