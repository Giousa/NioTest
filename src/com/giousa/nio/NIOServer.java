package com.giousa.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer{
	
	private int blockSize = 4*1024;
	private ByteBuffer sendBuffer = ByteBuffer.allocate(blockSize);
	private ByteBuffer receiveBuffer = ByteBuffer.allocate(blockSize);
	private Selector selector;
	private int flag = 1;

	public NIOServer(int port) throws Exception {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);//设置是否阻塞
		ServerSocket serverSocket = serverSocketChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));//绑定IP和端口
		selector = Selector.open();//打开选择器
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Server start->"+port);
	}
	
	public void listener() throws IOException{
		while(true){
			selector.select();
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectedKeys.iterator();
			while(iterator.hasNext()){
				SelectionKey selectionKey = iterator.next();
				iterator.remove();
				//业务逻辑
				handlerKey(selectionKey);
			}
		}
	}

	private void handlerKey(SelectionKey selectionKey) throws IOException {
		ServerSocketChannel serverSocketChannel = null;
		SocketChannel socketChannel = null;
		String receiveText;
		String sendText;
		int count = 0;
		System.out.println("我被执行了");
		if(selectionKey.isAcceptable()){
			serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
			socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ);
			System.out.println("服务端可连接");
		}else if(selectionKey.isReadable()){
			socketChannel = (SocketChannel) selectionKey.channel();
			count = socketChannel.read(receiveBuffer);
			if(count > 0){
				receiveText = new String(receiveBuffer.array(),0,count);
				System.out.println("服务端接收到客户端信息："+receiveText);
				socketChannel.register(selector, SelectionKey.OP_WRITE);
			}
		}else if(selectionKey.isWritable()){
			sendBuffer.clear();//清空写的缓冲区
			socketChannel = (SocketChannel) selectionKey.channel();
			sendText = "Giousa msg send to client "+flag++;
			sendBuffer.put(sendText.getBytes());
			sendBuffer.flip();
			socketChannel.write(sendBuffer);
			System.out.println("服务端发送数据给客户端： "+sendText);
		}
		
	}

	public static void main(String[] args) throws Exception {
		int port = 7888;
		NIOServer server = new NIOServer(port);
		server.listener();

	}

}
