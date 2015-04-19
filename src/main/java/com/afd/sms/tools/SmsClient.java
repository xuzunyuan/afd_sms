package com.afd.sms.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.message.BasicNameValuePair;  

public class SmsClient{
	 
	 private String url="http://web.cr6868.com/asmx/smsservice.aspx";
	 private String user="bjbaoyuan";
	 private String pwd="46511CB817F5F2622E846B966D3B";
     private String sign="afd";
	 
     public   int sendSMS(String[] mobile,String msg,int type){
    		if(null == msg || 0 == msg.length()){
    			return -1;
    		}
    		if(null == mobile || 0 == mobile.length){
    			return -1;
    		}   
    	  List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();  
  		  nvps.add(new BasicNameValuePair("name", user));  
  		  
  		  nvps.add(new BasicNameValuePair("pwd", pwd)); 
  		  
  		  StringBuffer mobile_str=new StringBuffer();
  		  int k=0;
  		   for(int i=0;i<mobile.length;i++){
  			 if(k<0){mobile_str.append(",");}else{k++;}
  			 mobile_str.append(mobile[i]);
  		   }
  		   System.out.print(mobile_str.toString());
  		  nvps.add(new BasicNameValuePair("mobile", mobile_str.toString())); 
  		  
  		  nvps.add(new BasicNameValuePair("content", msg));  
  		  
  		  if(type==1){
  		  nvps.add(new BasicNameValuePair("sign", sign)); 
  		  }
  		  
  		  nvps.add(new BasicNameValuePair("type", "pt")); 
  		  
  		  return this.sendMsg(nvps);
     }
    
	public int sendScheduledSMS(String[] mobile, String envelopSms,String sendTime) {
		if(null == envelopSms || 0 == envelopSms.length()){
			return -1;
		}    		
	  List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();  
		  nvps.add(new BasicNameValuePair("name", user));  
		  
		  nvps.add(new BasicNameValuePair("pwd", pwd)); 
		  StringBuffer mobile_str=new StringBuffer();
		  int k=0;
 		   for(int i=0;i<mobile.length;i++){
 			 if(k<0){mobile_str.append(",");}else{k++;}
 			 mobile_str.append(mobile[i]);
 		   }
 		   System.out.print(mobile_str.toString());
		  nvps.add(new BasicNameValuePair("mobile", mobile_str.toString())); 
		  
		  nvps.add(new BasicNameValuePair("content", envelopSms));  
		  
		  nvps.add(new BasicNameValuePair("stime", sendTime));  
		  // nvps.add(new BasicNameValuePair("sign", sign)); 
		  
		  nvps.add(new BasicNameValuePair("type", "pt")); 
		  
		  return this.sendMsg(nvps);
	}
	
	private int sendMsg(List<BasicNameValuePair> nvps){
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build(); 
		HttpPost httpPost = new HttpPost(url);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps)); 
			HttpResponse response = closeableHttpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (200 != response.getStatusLine().getStatusCode()) {
				return -2;
			}
			if (null == entity) {
				return -3;
			}
			String data = EntityUtils.toString(entity);
			System.out.print(data);
			String[] rets = data.split(",");
			if(rets[0].endsWith("0")){
				return 0;
			}else{
				return (Integer.valueOf(rets[0]));
			}
			
		} catch (ClientProtocolException e) {
			return -4;
		} catch (IOException e) {
			return -5;
		} finally {  
			try {
				closeableHttpClient.close();  
			} catch (IOException e) {  
				return -6;
			}
		}
	}
}
