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
package org.aoju.bus.starter.notify;

import lombok.RequiredArgsConstructor;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.notify.Context;
import org.aoju.bus.notify.Provider;
import org.aoju.bus.notify.Registry;
import org.aoju.bus.notify.provider.aliyun.AliyunSmsProvider;
import org.aoju.bus.notify.provider.aliyun.AliyunVmsProvider;
import org.aoju.bus.notify.provider.dingtalk.DingTalkProvider;
import org.aoju.bus.notify.provider.netease.NeteaseAttachProvider;
import org.aoju.bus.notify.provider.netease.NeteaseSendProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知提供服务
 *
 * @author Justubborn
 * @version 5.8.5
 * @since JDK1.8+
 */
@RequiredArgsConstructor
public class NotifyProviderService {

    /**
     * 通知器配置
     */
    private static Map<Registry, Context> NOTIFY_CACHE = new ConcurrentHashMap<>();
    public final NotifyProperties properties;

    /**
     * 注册组件
     *
     * @param registry 组件名称
     * @param context  组件对象
     */
    public static void register(Registry registry, Context context) {
        if (NOTIFY_CACHE.containsKey(registry)) {
            throw new InstrumentException("重复注册同名称的组件：" + registry.name());
        }
        NOTIFY_CACHE.putIfAbsent(registry, context);
    }

    /**
     * 返回type对象
     *
     * @param registry {@link Registry}
     * @return {@link Provider}
     */
    public static Provider get(Registry registry) {
        if (Registry.ALIYUN_SMS.equals(registry)) {
            return new AliyunSmsProvider(NOTIFY_CACHE.get(registry));
        } else if (Registry.ALIYUN_VMS.equals(registry)) {
            return new AliyunVmsProvider(NOTIFY_CACHE.get(registry));
        } else if (Registry.DINGTALK_CORP_MSG.equals(registry)) {
            return new DingTalkProvider(NOTIFY_CACHE.get(registry));
        } else if (Registry.NETEASE_ATTACH_MSG.equals(registry)) {
            return new NeteaseAttachProvider(NOTIFY_CACHE.get(registry));
        } else if (Registry.NETEASE_MSG.equals(registry)) {
            return new NeteaseSendProvider(NOTIFY_CACHE.get(registry));
        }
        return null;
    }

}