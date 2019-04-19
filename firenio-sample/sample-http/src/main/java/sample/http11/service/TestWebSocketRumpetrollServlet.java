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
package sample.http11.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.firenio.codec.http11.HttpFrame;
import com.firenio.codec.http11.WebSocketFrame;
import com.firenio.common.Util;
import com.firenio.component.Frame;
import com.firenio.component.Channel;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

import sample.http11.HttpFrameAcceptor;

// FIXME ________根据当前是否正在redeploy来保存和恢复client
@Service("/web-socket-rumpetroll")
public class TestWebSocketRumpetrollServlet extends HttpFrameAcceptor {

    private Logger              logger     = LoggerFactory.getLogger(getClass());
    private WebSocketMsgAdapter msgAdapter = new WebSocketMsgAdapter("websocket-rumpetroll");

    @Override
    public void accept(Channel ch, Frame frame) throws Exception {
        if (frame instanceof HttpFrame) {
            HttpFrame f = (HttpFrame) frame;
            f.updateWebSocketProtocol(ch);
            msgAdapter.addClient(ch.getDesc(), ch);
            JSONObject o = new JSONObject();
            o.put("type", "welcome");
            o.put("id", ch.getChannelId());
            WebSocketFrame wsf = new WebSocketFrame();
            wsf.setContent(ch.allocate());
            wsf.write(o.toJSONString(), ch.getCharset());
            ch.writeAndFlush(wsf);
            return;
        }
        WebSocketFrame f = (WebSocketFrame) frame;
        // CLOSE
        if (f.isCloseFrame()) {
            if (msgAdapter.removeClient(ch) != null) {
                JSONObject o = new JSONObject();
                o.put("type", "closed");
                o.put("id", ch.getChannelId());
                msgAdapter.sendMsg(o.toJSONString());
                logger.info("客户端主动关闭连接：{}", ch);
            }
            if (ch.isOpen()) {
                ch.writeAndFlush(f);
            }
        } else {
            String     msg  = f.getStringContent();
            JSONObject o    = JSON.parseObject(msg);
            String     name = o.getString("name");
            if (Util.isNullOrBlank(name)) {
                name = Util.randomUUID();
            }
            o.put("name", name);
            o.put("id", ch.getChannelId());
            String type = o.getString("type");
            if ("update".equals(type)) {
                o.put("life", "1");
                o.put("authorized", "false");
                o.put("x", Double.valueOf(o.getString("x")));
                o.put("y", Double.valueOf(o.getString("x")));
                o.put("momentum", Double.valueOf(o.getString("momentum")));
                o.put("angle", Double.valueOf(o.getString("angle")));
            } else if ("message".equals(type)) {
            }
            msgAdapter.sendMsg(o.toJSONString());
        }
    }

    @PreDestroy
    public void destroy() throws Exception {
        Util.stop(msgAdapter);
    }

    /**
     * @return the msgAdapter
     */
    public WebSocketMsgAdapter getMsgAdapter() {
        return msgAdapter;
    }

    @PostConstruct
    public void init() throws Exception {
        Util.start(msgAdapter);
    }

}
