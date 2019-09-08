/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.spring.crypto;

import org.aoju.bus.base.spring.BaseAdvice;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.annotation.DecryptBody;
import org.aoju.bus.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageConverter;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 请求请求处理类（目前仅仅对requestbody有效）
 * 对加了@Decrypt的方法的数据进行解密密操作
 *
 * @author Kimi Liu
 * @version 3.2.2
 * @since JDK 1.8
 */
public class RequestBodyAdvice extends BaseAdvice
        implements org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice {

    @Autowired
    CryptoProperties cryptoProperties;


    /**
     * 首次调用，以确定是否应用此拦截.
     *
     * @param parameter     方法参数
     * @param type          目标类型，不一定与方法相同
     *                      参数类型，例如 {@code HttpEntity<String>}.
     * @param converterType 转换器类型
     * @return true/false 是否应该调用此拦截
     */
    @Override
    public boolean supports(MethodParameter parameter,
                            Type type,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Annotation[] annotations = parameter.getDeclaringClass().getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof DecryptBody) {
                    return true;
                }
            }
        }
        return parameter.getMethod().isAnnotationPresent(DecryptBody.class);
    }

    /**
     * 在读取和转换请求体之前调用.
     *
     * @param inputMessage  HTTP输入消息
     * @param parameter     方法参数
     * @param type          目标类型，不一定与方法相同
     *                      参数类型，例如 {@code HttpEntity<String>}.
     * @param converterType 转换器类型
     * @return 输入请求或新实例，永远不会 {@code null}
     */
    @Override
    public org.springframework.http.HttpInputMessage beforeBodyRead(org.springframework.http.HttpInputMessage inputMessage,
                                                                    MethodParameter parameter,
                                                                    Type type,
                                                                    Class<? extends HttpMessageConverter<?>> converterType) {
        if (!cryptoProperties.isDebug()) {
            try {
                final DecryptBody decrypt = parameter.getMethod().getAnnotation(DecryptBody.class);
                if (ObjectUtils.isNotNull(decrypt)) {
                    final String key = StringUtils.defaultString(decrypt.key(), cryptoProperties.getDecrypt().getKey());
                    return new HttpInputMessage(inputMessage, key, decrypt.type(), Charset.DEFAULT_UTF_8);
                }
            } catch (Exception e) {
                Logger.error("数据解密失败", e);
            }
        }
        return inputMessage;
    }

    /**
     * 在请求体转换为对象之后调用第三个(也是最后一个).
     *
     * @param body          在调用第一个通知之前将其设置为转换器对象
     * @param inputMessage  HTTP输入消息
     * @param parameter     方法参数
     * @param type          目标类型，不一定与方法相同
     *                      参数类型，例如 {@code HttpEntity<String>}.
     * @param converterType 转换器类型
     * @return 相同的主体或新实例
     */
    @Override
    public Object afterBodyRead(Object body, org.springframework.http.HttpInputMessage inputMessage,
                                MethodParameter parameter,
                                Type type,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * 如果主体为空，则调用第二个(也是最后一个).
     *
     * @param body          通常在调用第一个通知之前将其设置为{@code null}
     * @param inputMessage  HTTP输入消息
     * @param parameter     方法参数
     * @param type          目标类型，不一定与方法相同
     *                      参数类型，例如 {@code HttpEntity<String>}.
     * @param converterType 转换器类型
     * @return 要使用的值或{@code null}，该值可能会引发{@code HttpMessageNotReadableException}.
     */
    @Override
    public Object handleEmptyBody(Object body, org.springframework.http.HttpInputMessage inputMessage,
                                  MethodParameter parameter,
                                  Type type,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    class HttpInputMessage implements org.springframework.http.HttpInputMessage {

        private HttpHeaders headers;
        private InputStream body;

        public HttpInputMessage(org.springframework.http.HttpInputMessage inputMessage,
                                String key,
                                String mode,
                                String charset) throws Exception {
            if (StringUtils.isEmpty(key)) {
                throw new NullPointerException("请配置request.crypto.decrypt.key参数");
            }

            this.headers = inputMessage.getHeaders();
            String content = IoUtils.toString(inputMessage.getBody(), charset);

            String decryptBody;
            if (content.startsWith("{")) {
                decryptBody = content;
            } else {
                StringBuilder json = new StringBuilder();
                content = content.replaceAll(" ", "+");

                if (!StringUtils.isEmpty(content)) {
                    String[] contents = content.split("\\|");
                    for (int k = 0; k < contents.length; k++) {
                        String value = contents[k];
                        value = new String(CryptoUtils.decrypt(mode, key, Base64.decode(value)), charset);
                        json.append(value);
                    }
                }
                decryptBody = json.toString();
            }
            this.body = IoUtils.toInputStream(decryptBody, charset);
        }

        @Override
        public InputStream getBody() {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }

}