/*
 * Copyright 2015 The FireNio Project
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
package test.io.protobase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.firenio.Options;
import com.firenio.codec.protobase.ProtobaseCodec;
import com.firenio.codec.protobase.ProtobaseFrame;
import com.firenio.common.Util;
import com.firenio.component.Channel;
import com.firenio.component.ChannelAcceptor;
import com.firenio.component.ChannelAliveListener;
import com.firenio.component.ChannelConnector;
import com.firenio.component.Frame;
import com.firenio.component.IoEventHandle;
import com.firenio.component.LoggerChannelOpenListener;
import com.firenio.concurrent.Waiter;
import com.firenio.log.DebugUtil;

import junit.framework.Assert;

public class TestProtobase {

    static final String hello = "hello server!";
    static final String res   = "yes server already accept your text message:";

    static {
        Options.setEnableEpoll(true);
    }

    ChannelAcceptor context = new ChannelAcceptor(8300);

    @After
    public void clean() {
        Util.unbind(context);
    }

    @Before
    public void main() throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame f) throws Exception {
                if (f.isText()) {
                    String text = f.getStringContent();
                    DebugUtil.debug("receive text:" + text);
                    f.setContent(ch.allocate());
                    f.write(res, ch);
                    f.write(text, ch);
                    ch.writeAndFlush(f);
                } else {
                    byte[] text = f.getArrayContent();
                    f.setContent(ch.allocate());
                    f.write(res, ch);
                    f.write(text);
                    ch.writeAndFlush(f);
                }

            }
        };
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelIdleEventListener(new ChannelAliveListener());
        context.setIoEventHandle(eventHandleAdaptor);
        context.addProtocolCodec(new ProtobaseCodec());
        context.bind();
    }

    @Test
    public void test() throws Exception {
        testText();
        testBinary();
    }

    public void testBinary() throws Exception {

        Waiter<String> w = new Waiter<>();
        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame f) {
                String text = new String(f.getArrayContent(), ch.getCharset());
                System.out.println();
                System.out.println("____________________" + text);
                System.out.println();
                Util.close(ch);
                w.call(text, null);
            }
        };

        ChannelConnector context = new ChannelConnector(8300);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new ProtobaseCodec());
        Channel        ch = context.connect();
        ProtobaseFrame f  = new ProtobaseFrame();
        f.setBinary();
        f.setString(hello, ch);
        ch.writeAndFlush(f);
        w.await(3000);
        Assert.assertEquals(w.getResponse(), res + hello);
    }

    public void testText() throws Exception {

        Waiter<String> w = new Waiter<>();
        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) {
                String text = frame.getStringContent();
                System.out.println();
                System.out.println("____________________" + text);
                System.out.println();
                Util.close(ch);
                w.call(text, null);
            }
        };

        ChannelConnector context = new ChannelConnector(8300);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new ProtobaseCodec());
        Channel        ch = context.connect();
        ProtobaseFrame f  = new ProtobaseFrame();
        f.setString(hello, ch);
        ch.writeAndFlush(f);
        w.await(3000);
        Assert.assertEquals(w.getResponse(), res + hello);
    }

}
