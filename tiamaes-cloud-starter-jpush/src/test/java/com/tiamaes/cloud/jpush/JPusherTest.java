package com.tiamaes.cloud.jpush;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;

public class JPusherTest {

	@Test
	public void test(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("key1", "1");
		map.put("key2", "2");
		Gson json = new Gson();
		String content = json.toJson(map);
		System.out.println("content : " + content);
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias("13783698404"))
				.setMessage(Message.content(content)).build();
		System.out.println(payload.toJSON().getAsJsonObject().get("message").getAsJsonObject().get("msg_content").getAsString());
	}
}
