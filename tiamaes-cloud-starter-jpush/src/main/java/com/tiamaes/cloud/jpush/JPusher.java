package com.tiamaes.cloud.jpush;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;

public class JPusher {
	private static final Logger logger = LoggerFactory.getLogger(JPusher.class);

	private final JPushClient jPushClient;

	public JPusher(JPushClient jPushClient) {
		this.jPushClient = jPushClient;
	}

	/**
	 * 群发通知
	 */
	public PushResult send(String message) {
		PushPayload payload = PushPayload.alertAll(message);
		return sendPush(payload);
	}

	/**
	 * 给指定的客户端发送通知
	 * 
	 * @param notice
	 *            要发送的通知内容
	 * @param mobile
	 *            要接收的用户
	 * @throws Exception
	 */
	public PushResult sendNotice(String notice, String mobile){
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(mobile))
				.setNotification(Notification.alert(notice)).build();
		return sendPush(payload);
	}

	/**
	 * 给指定的客户端发送通知
	 * 
	 * @param notice
	 *            要发送的通知内容
	 * @param mobiles
	 *            要接收的用户
	 * @throws Exception
	 */
	public PushResult sendNotice(String notice, Collection<String> mobiles){
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(mobiles))
				.setNotification(Notification.alert(notice)).build();
		return sendPush(payload);
	}

	/**
	 * 给指定客户端发送消息
	 * 
	 * @param message
	 *            要发送的消息内容
	 * @param mobile
	 *            要接收的用户
	 * @throws Exception
	 */
	public PushResult sendMessage(String message, String... mobile){
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(mobile))
				.setMessage(Message.content(message)).build();
		return sendPush(payload);
	}

	/**
	 * 给多个客户端发送消息
	 * 
	 * @param message
	 *            要发送的消息内容
	 * @param mobiles
	 *            要接收的用户
	 * @throws Exception
	 */
	public PushResult sendMessage(String message, Collection<String> mobiles) {
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(mobiles))
				.setMessage(Message.content(message)).build();
		return sendPush(payload);
	}
	
	/**
	 * 给指定客户端发送消息
	 * 
	 * @param message
	 *            要发送的消息内容
	 * @param mobile
	 *            要接收的用户
	 * @throws Exception
	 */
	public PushResult sendMessage(Object message, String... mobile){
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(mobile))
				.setMessage(Message.content(new Gson().toJson(message))).build();
		return sendPush(payload);
	}

	/**
	 * 给多个客户端发送消息
	 * 
	 * @param message
	 *            要发送的消息内容
	 * @param mobiles
	 *            要接收的用户
	 * @throws Exception
	 */
	public PushResult sendMessage(Object message, Collection<String> mobiles) {
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(mobiles))
				.setMessage(Message.content(new Gson().toJson(message))).build();
		return sendPush(payload);
	}
	
	public PushResult sendPush(PushPayload payload) {
		PushResult result = null;
		try {
			result = jPushClient.sendPush(payload);
		} catch (APIConnectionException e) {
			e.printStackTrace();
			logger.error("JPush Server Connection error. Should retry later again.");
		} catch (APIRequestException e) {
			e.printStackTrace();
			logger.error("Error response from JPush server. Should review and fix it. errorCode : ", e.getErrorCode());
		}
		if(logger.isDebugEnabled() && result != null && result.isResultOK()){
			result.getOriginalContent();
			logger.debug("SEND: " + getPayloadContent(payload));
		}
		return result;
	}
	
	
	private static String getPayloadContent(PushPayload payload){
		return payload.toJSON().getAsJsonObject().get("message").getAsJsonObject().get("msg_content").getAsString();
	}
	
}