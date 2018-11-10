package com.xtl.xcssoauth.idmgr;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServlet;

public class XtlIDMgrServer extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init () {
		// TODO Auto-generated method stub
		new Thread(new XtlIDMgrThread()).start();
	}
}
