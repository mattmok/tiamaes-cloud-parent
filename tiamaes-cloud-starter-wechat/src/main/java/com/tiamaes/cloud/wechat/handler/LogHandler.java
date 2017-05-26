package com.tiamaes.cloud.wechat.handler;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * @author Chen
 */
@Component
public class LogHandler extends AbstractHandler {
	
	@Resource
	private ObjectMapper jacksonObjectMapper;
    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
            Map<String, Object> context, WxMpService wxMpService,
            WxSessionManager sessionManager) {
    	
        try {
			this.logger.info("RCVD: ", jacksonObjectMapper.writeValueAsString(wxMessage));
		} catch (JsonProcessingException ignore) {
		}
        return null;
    }

}
