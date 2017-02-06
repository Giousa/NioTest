package com.giousa.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOClient {

	private static int blockSize = 4*1024;
	private static ByteBuffer sendBuffer = ByteBuffer.allocate(blockSize);
	private static ByteBuffer receiveBuffer = ByteBuffer.allocate(blockSize);
	private static int flag = 1;
	private final static InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 7888);
	
	public static void main(String[] args) throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		Selector selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
		socketChannel.connect(serverAddress);
		
		Set<SelectionKey> selectionKeys;
		Iterator<SelectionKey> iterator;
		SelectionKey selectionKey;
		SocketChannel client;
		String receiveText;
		String sendText;	
		System.out.println("client 开始执行了");
		while(true){
			selectionKeys = selector.selectedKeys();
			iterator = selectionKeys.iterator();
			while(iterator.hasNext()){
				System.out.println("client hasnext");
				selectionKey = iterator.next();
				if(selectionKey.isConnectable()){
					System.out.println("client connect");
					client = (SocketChannel) selectionKey.channel();
					if(client.isConnectionPending()){
						client.finishConnect();
						System.out.println("客户端完成连接操作");
						sendBuffer.clear();
						sendBuffer.put("hello,Server!".getBytes());
						sendBuffer.flip();
						client.write(sendBuffer);
						
					}
					
					client.register(selector, SelectionKey.OP_READ);
				}
				
				if(selectionKey.isReadable()){
					client = (SocketChannel) selectionKey.channel();
					receiveBuffer.clear();
					int count = client.read(receiveBuffer);
					if(count > 0){
						receiveText = new String(receiveBuffer.array(), 0, count);
						System.out.println("客户端接收到服务端数据："+receiveText);
						client.register(selector, SelectionKey.OP_WRITE);
					}
				}
				
				if(selectionKey.isWritable()){
					sendBuffer.clear();
					client = (SocketChannel) selectionKey.channel();
					sendText = "Msg send to Server->"+flag++;
					sendBuffer.put(sendText.getBytes());
					sendBuffer.flip();
					client.write(sendBuffer);
					System.out.println("客户端发送数据给服务端："+sendText);
					client.register(selector, SelectionKey.OP_READ);
				}
			}
			selectionKeys.clear();
		}
		
	}

}
