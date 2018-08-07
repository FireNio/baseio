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
package com.generallycloud.baseio.codec.http11;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.MathUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolCodec;

//FIXME 心跳貌似由服务端发起
/**
* <pre>
* 
*       0               1               2               3
*       0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
*      +-+-+-+-+-------+-+-------------+-------------------------------+
*      |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
*      |I|S|S|S|  (4)  |A|     (7)     |             (16/32)           |
*      |N|V|V|V|       |S|             |   (if payload len==126/127)   |
*      | |1|2|3|       |K|             |       (unsigned(2byte))       |
*      +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
*      |     Extended payload length continued, if payload len == 127  |
*      + - - - - - - - - - - - - - - - +-------------------------------+
*      |    payload len (4+2+2)        |Masking-key, if MASK set to 1  |
*      +-------------------------------+-------------------------------+
*      | Masking-key (continued)       |          Payload Data         |
*      +-------------------------------- - - - - - - - - - - - - - - - +
*      :                     Payload Data continued ...                :
*      + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
*      |                     Payload Data continued ...                |
*      +---------------------------------------------------------------+
* 
* 
* </pre>
*
*/
public class WebSocketCodec extends ProtocolCodec {

    public static final String   FUTURE_STACK_KEY   = "FixedThreadStack_WebSocketFuture";
    public static final int      PROTOCOL_HEADER    = 2;
    public static final String   PROTOCOL_ID        = "WebSocket";
    public static final byte     TYPE_BINARY        = 2;
    public static final byte     TYPE_CLOSE         = 8;
    public static final byte     TYPE_PING          = 9;
    public static final byte     TYPE_PONG          = 10;
    public static final byte     TYPE_TEXT          = 1;
    public static final int      MAX_UNSIGNED_SHORT = (1 << 16) - 1;
    public static WebSocketCodec WS_PROTOCOL_CODEC;

    static void init(ChannelContext context, int limit, int futureStackSize) {
        WS_PROTOCOL_CODEC = new WebSocketCodec(limit, futureStackSize);
        WS_PROTOCOL_CODEC.initialize(context);
    }

    private final int limit;
    private final int futureStackSize;

    public WebSocketCodec(int limit, int futureStackSize) {
        this.limit = limit;
        this.futureStackSize = futureStackSize;
    }

    @Override
    public Future ping(NioSocketChannel ch) {
        if (WebSocketCodec.PROTOCOL_ID.equals(ch.getProtocolId())) {
            return new WebSocketFuture().setPing();
        }
        return null;
    }

    @Override
    public Future pong(NioSocketChannel ch, Future ping) {
        if (WebSocketCodec.PROTOCOL_ID.equals(ch.getProtocolId())) {
            return ping.setPong();
        }
        return null;
    }

    @Override
    public Future decode(NioSocketChannel ch, ByteBuf buffer) throws IOException {
        if (futureStackSize > 0) {
//            NioEventLoop eventLoop = channel.getEventLoop();
//            FixedThreadStack<WebSocketFuture> stack = (FixedThreadStack<WebSocketFuture>) eventLoop
//                    .getAttribute(FUTURE_STACK_KEY);
//            if (stack == null) {
//                stack = new FixedThreadStack<>(futureStackSize);
//                eventLoop.setAttribute(FUTURE_STACK_KEY, stack);
//            }
//            WebSocketFuture future = stack.pop();
//            if (future == null) {
//                return new WebSocketFuture(channel, limit);
//            }
//            return future.reset(channel, limit);
        }
        return new WebSocketFuture(ch, limit);
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Future future) throws IOException {
        ByteBufAllocator allocator = ch.alloc();
        WebSocketFuture f = (WebSocketFuture) future;
        byte[] header;
        byte[] data = f.getWriteBuffer();
        int size = f.getWriteSize();
        byte header0 = (byte) (0x8f & (f.getType() | 0xf0));
        if (size < 126) {
            header = new byte[2];
            header[0] = header0;
            header[1] = (byte) size;
        } else if (size <= MAX_UNSIGNED_SHORT) {
            header = new byte[4];
            header[0] = header0;
            header[1] = 126;
            MathUtil.unsignedShort2Byte(header, size, 2);
        } else {
            header = new byte[10];
            header[0] = header0;
            header[1] = 127;
            MathUtil.long2Byte(header, size, 2);
        }
        ByteBuf buf = allocator.allocate(header.length + size);
        buf.put(header);
        if (size > 0) {
            buf.put(data, 0, size);
        }
        return buf.flip();
    }

    public int getFutureStackSize() {
        return futureStackSize;
    }

    @Override
    public String getProtocolId() {
        return PROTOCOL_ID;
    }

    //  public IOWriteFuture encodeWithMask(BaseContext context, IOReadFuture readFuture) throws IOException {
    //      
    //      WebSocketReadFuture future = (WebSocketReadFuture) readFuture;
    //
    //      BufferedOutputStream o = future.getWriteBuffer();
    //
    //      byte [] header;
    //      
    //      int size = o.size();
    //      
    //      byte header0 = (byte) (0x8f & (future.getType() | 0xf0));
    //      
    //      if (size < 126) {
    //          header = new byte[2];
    //          header[0] = header0;
    //          header[1] = (byte)(size | 0x80);
    //      }else if(size < ((1 << 16) -1)){
    //          header = new byte[4];
    //          header[0] = header0;
    //          header[1] = (byte) (126 | 0xff);
    //          header[3] = (byte)(size & 0xff);
    //          header[2] = (byte)((size >> 8) & 0x80);
    //      }else{
    //          header = new byte[6];
    //          header[0] = header0;
    //          header[1] = (byte) (127 | 0x80);
    //          MathUtil.int2Byte(header, size, 2);
    //      }
    //      
    //      ByteBuf buffer = context.getHeapByteBufferPool().allocate(header.length + size + 4);
    //      
    //      buffer.put(header);
    //      
    //      byte [] array = o.array();
    //      
    //      byte [] mask = MathUtil.int2Byte(size);
    //      
    //      for (int i = 0; i < size; i++) {
    //          
    //          array[i] = (byte)(array[i] ^ mask[i % 4]);
    //      }
    //      
    //      buffer.put(mask);
    //      
    //      buffer.put(array,0,size);
    //      
    //      buffer.flip();
    //
    //      return new IOWriteFutureImpl(readFuture, buffer);
    //  }

}
