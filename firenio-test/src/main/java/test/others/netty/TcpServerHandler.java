package test.others.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TcpServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("channelActive>>>>>>>>");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.channel().writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("hi 发生异常了:" + cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive>>>>>>>>");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //        IdleStateEvent e = (IdleStateEvent) evt;
        //        System.out.println(DateUtil.formatYyyy_MM_dd_HH_mm_ss_SSS() + "   " + e.state());
        super.userEventTriggered(ctx, evt);
    }

}
