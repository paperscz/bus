/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.starter.oauth;

import lombok.RequiredArgsConstructor;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Provider;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.provider.*;

/**
 * 授权服务提供
 *
 * @author Kimi Liu
 * @version 5.8.5
 * @since JDK 1.8+
 */
@RequiredArgsConstructor
public class AuthProviderService {

    public final AuthProperties properties;
    public final ExtendCache extendCache;

    /**
     * 返回type对象
     *
     * @param type {@link Registry}
     * @return {@link Provider}
     */
    public Provider get(Registry type) {
        Context context = properties.getType().get(type);
        if (Registry.ALIPAY.equals(type)) {
            return new AlipayProvider(context, extendCache);
        } else if (Registry.BAIDU.equals(type)) {
            return new BaiduProvider(context, extendCache);
        } else if (Registry.CODING.equals(type)) {
            return new CodingProvider(context, extendCache);
        } else if (Registry.CSDN.equals(type)) {
            return new CsdnProvider(context, extendCache);
        } else if (Registry.DINGTALK.equals(type)) {
            return new DingTalkProvider(context, extendCache);
        } else if (Registry.DOUYIN.equals(type)) {
            return new DouyinProvider(context, extendCache);
        } else if (Registry.ELEME.equals(type)) {
            return new ElemeProvider(context, extendCache);
        } else if (Registry.FACEBOOK.equals(type)) {
            return new FacebookProvider(context, extendCache);
        } else if (Registry.FEISHU.equals(type)) {
            return new FeishuProvider(context, extendCache);
        } else if (Registry.GITEE.equals(type)) {
            return new GiteeProvider(context, extendCache);
        } else if (Registry.GITHUB.equals(type)) {
            return new GithubProvider(context, extendCache);
        } else if (Registry.GITLAB.equals(type)) {
            return new GitlabProvider(context, extendCache);
        } else if (Registry.GOOGLE.equals(type)) {
            return new GoogleProvider(context, extendCache);
        } else if (Registry.HUAWEI.equals(type)) {
            return new HuaweiProvider(context, extendCache);
        } else if (Registry.JD.equals(type)) {
            return new JdProvider(context, extendCache);
        } else if (Registry.KUJIALE.equals(type)) {
            return new KujialeProvider(context, extendCache);
        } else if (Registry.LINKEDIN.equals(type)) {
            return new LinkedinProvider(context, extendCache);
        } else if (Registry.MEITUAN.equals(type)) {
            return new MeituanProvider(context, extendCache);
        } else if (Registry.MICROSOFT.equals(type)) {
            return new MicrosoftProvider(context, extendCache);
        } else if (Registry.MI.equals(type)) {
            return new MiProvider(context, extendCache);
        } else if (Registry.OSCHINA.equals(type)) {
            return new OschinaProvider(context, extendCache);
        } else if (Registry.PINTEREST.equals(type)) {
            return new PinterestProvider(context, extendCache);
        } else if (Registry.QQ.equals(type)) {
            return new QqProvider(context, extendCache);
        } else if (Registry.RENREN.equals(type)) {
            return new RenrenProvider(context, extendCache);
        } else if (Registry.STACKOVERFLOW.equals(type)) {
            return new StackOverflowProvider(context, extendCache);
        } else if (Registry.TAOBAO.equals(type)) {
            return new TaobaoProvider(context, extendCache);
        } else if (Registry.TEAMBITION.equals(type)) {
            return new TeambitionProvider(context, extendCache);
        } else if (Registry.TENCENT.equals(type)) {
            return new TencentProvider(context, extendCache);
        } else if (Registry.TOUTIAO.equals(type)) {
            return new ToutiaoProvider(context, extendCache);
        } else if (Registry.TWITTER.equals(type)) {
            return new TwitterProvider(context, extendCache);
        } else if (Registry.WECHAT_EE.equals(type)) {
            return new WeChatEEProvider(context, extendCache);
        } else if (Registry.WECHAT_MP.equals(type)) {
            return new WeChatMpProvider(context, extendCache);
        } else if (Registry.WECHAT_OP.equals(type)) {
            return new WeChatOPProvider(context, extendCache);
        } else if (Registry.WEIBO.equals(type)) {
            return new WeiboProvider(context, extendCache);
        }
        throw new InstrumentException(Builder.Status.UNSUPPORTED.getCode());
    }

}
