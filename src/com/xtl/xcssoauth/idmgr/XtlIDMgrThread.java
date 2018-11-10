package com.xtl.xcssoauth.idmgr;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class XtlIDMgrThread implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Lock lock=new ReentrantLock();
		lock.lock();

		try {
			ServerSocket server = new ServerSocket(18887);
			while (true) {    
				System.out.println("ID mgr listerner start");
				Socket socket = server.accept();
				new XtlIDMgrHandlerThread(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		lock.unlock();
	}
	
}
