package com.tiamaes.cloud.wechat.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * @author Chen
 */
@RestController
@RequestMapping("/wechat/portal")
public class WechatController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private WxMpService wxService;

	@Autowired
	private WxMpMessageRouter router;

	@GetMapping(produces = "text/plain;charset=utf-8")
	public String authGet(@RequestParam(name = "signature", required = false) String signature,
			@RequestParam(name = "timestamp", required = false) String timestamp,
			@RequestParam(name = "nonce", required = false) String nonce,
			@RequestParam(name = "echostr", required = false) String echostr) {
		if (logger.isDebugEnabled()) {
			logger.info("AUTH INFO：[{}, {}, {}, {}]", signature, timestamp, nonce, echostr);
		}

		if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
			throw new IllegalArgumentException("请求参数非法，请核实!");
		}

		if (this.wxService.checkSignature(timestamp, nonce, signature)) {
			return echostr;
		}

		return "非法请求";
	}

	@PostMapping(produces = "application/xml; charset=UTF-8")
	public String post(@RequestBody String requestBody, @RequestParam("signature") String signature,
			@RequestParam("timestamp") String timestamp, @RequestParam("nonce") String nonce,
			@RequestParam(name = "encrypt_type", required = false) String encType,
			@RequestParam(name = "msg_signature", required = false) String msgSignature) {
		if (logger.isDebugEnabled()) {
			logger.info(
					"RCVD：[signature=[{}], encType=[{}], msgSignature=[{}], timestamp=[{}], nonce=[{}], requestBody=[{}]",
					signature, encType, msgSignature, timestamp, nonce, requestBody);
		}
		if (!this.wxService.checkSignature(timestamp, nonce, signature)) {
			throw new IllegalArgumentException("非法请求，可能属于伪造的请求!");
		}

		String out = null;
		if (encType == null) {
			WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
			WxMpXmlOutMessage outMessage = this.route(inMessage);
			if (outMessage == null) {
				return "";
			}
			out = outMessage.toXml();
		} else if ("aes".equals(encType)) {
			WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxService.getWxMpConfigStorage(),
					timestamp, nonce, msgSignature);
			if (logger.isDebugEnabled()) {
				logger.debug("AES AFTER：{} ", inMessage.toString());
			}
			WxMpXmlOutMessage outMessage = this.route(inMessage);
			if (outMessage == null) {
				return "";
			}

			out = outMessage.toEncryptedXml(wxService.getWxMpConfigStorage());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("SEND：{}", out);
		}
		return out;
	}

	private WxMpXmlOutMessage route(WxMpXmlMessage message) {
		try {
			return this.router.route(message);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

}
