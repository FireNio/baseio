/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.test.io.protobase;

import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ParamedProtobaseFrame;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.protobase.FixedChannel;
import com.generallycloud.baseio.container.protobase.OnFrame;
import com.generallycloud.baseio.container.protobase.SimpleIoEventHandle;
import com.generallycloud.baseio.protocol.Frame;

public class TestChannelDisconnect {

    public static void main(String[] args) throws Exception {

        String serviceName = "TestChannelDisconnectServlet";
        SimpleIoEventHandle eventHandle = new SimpleIoEventHandle();
        ChannelContext context = new ChannelContext(8300);
        ChannelConnector connector = new ChannelConnector(context);
        context.setIoEventHandle(eventHandle);
        context.setProtocolCodec(new ProtobaseCodec());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        FixedChannel channel = new FixedChannel(connector.connect());
        ParamedProtobaseFrame frame = channel.request(serviceName);
        System.out.println(frame.getReadText());
        channel.listen(serviceName, new OnFrame() {
            @Override
            public void onResponse(NioSocketChannel channel, Frame frame) {
                ParamedProtobaseFrame f = (ParamedProtobaseFrame) frame;
                System.out.println(f.getReadText());
            }
        });
        channel.write(serviceName);
        ThreadUtil.sleep(9999);
        CloseUtil.close(connector);
    }

}
