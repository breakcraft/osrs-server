package nl.bartpelle.veteres.net.codec.pregame;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import nl.bartpelle.veteres.net.message.PreloginResponseMessage;

/**
 * Created by Bart on 7/7/2015.
 */
public class PregameLoginEncoder extends MessageToByteEncoder<PreloginResponseMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, PreloginResponseMessage msg, ByteBuf out) throws Exception {
		out.writeLong(420_69_1080_1337L);
	}

}
