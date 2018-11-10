package com.xtl.xcssoauth.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xtl.xcssoauth.XtlRecoverTicket;
import com.xtl.xcssoauth.pojo.XtlTicket;
import com.xtl.xcssoauth.util.XtlDESUtils;

/**
 * Servlet implementation class SSOAuth
 */
public class XtlSSOAuth extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
	/** 账户信息 */
	private static Map<String, String> accounts;
	
	/** 单点登录标记 */
	private static Map<String, XtlTicket> tickets;
	
	/** cookie名称 */
	private String cookieName;
	
	/** 是否安全协议 */
	private boolean secure;
	
	/** 密钥 */
	private String secretKey;
	
	/** ticket有效时间 */
	private int ticketTimeout;
	
	/** 回收ticket线程�? */
	private ScheduledExecutorService schedulePool;
	
	/** 维护状态flag*/
	private boolean isMaintain = false;
	
	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		accounts = new HashMap<String, String>();
		accounts.put("zhangsan", "zhangsan");
		accounts.put("lisi", "lisi");
		accounts.put("wangwu", "wangwu");
		
		tickets = new ConcurrentHashMap<String, XtlTicket>();
		
		cookieName = config.getInitParameter("cookieName");
		secure = Boolean.parseBoolean(config.getInitParameter("secure"));
		secretKey = config.getInitParameter("secretKey");
		ticketTimeout = Integer.parseInt(config.getInitParameter("ticketTimeout"));
		
		schedulePool = Executors.newScheduledThreadPool(1);
		schedulePool.scheduleAtFixedRate(new XtlRecoverTicket(tickets), ticketTimeout * 60, 1, TimeUnit.MINUTES);
	
		try {
			ReadProperties("maintain.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		String special = request.getParameter("special");
		if(special!=null&&!special.equals("")){
			request.getSession().setAttribute("special", special);
		}
		System.out.println(request.getSession().getAttribute("special"));
		PrintWriter out = response.getWriter();
		String action = request.getParameter("action");
		
		if("preLogin".equals(action)) {
			preLogin(request, response);
		} else if("login".equals(action)) {
			doLogin(request, response);
		} else if("logout".equals(action)) {
			doLogout(request, response);
		} else if("authTicket".equals(action)) {
			authTicket(request, response);
		} else {
			System.err.println("指令错误");
			out.print("Action can not be empty?");
		}
		out.close();
	}

	@Override
	public void destroy() {
		if(schedulePool != null)    schedulePool.shutdown();
	}

	private void preLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Object special = request.getSession().getAttribute("special");
		Cookie ticket = null;
		Cookie[] cookies = request.getCookies();
		if(cookies != null)
			for(Cookie cookie : cookies) {
				if(cookie.getName().equals(cookieName)) {
					ticket = cookie;
					break;
				}
			}
		if(ticket == null) {
			if(isMaintain&&special!=null){
				if(special.equals("is")){
					request.getRequestDispatcher("login.jsp").forward(request, response);
				}else{
					request.getRequestDispatcher("maintain.jsp").forward(request, response);
				}
				
			}else{
				request.getRequestDispatcher("login.jsp").forward(request, response);
			}
			
		} else {
			
				String encodedTicket = ticket.getValue();
				String decodedTicket = XtlDESUtils.decrypt(encodedTicket, secretKey);
				if(isMaintain){
					
					if(tickets.containsKey(decodedTicket)&&special!=null&&special.equals("is")) {
						String setCookieURL = request.getParameter("setCookieURL");
						String gotoURL = request.getParameter("gotoURL");
						if(setCookieURL != null)
			                response.sendRedirect(setCookieURL + "?ticket=" + encodedTicket + "&expiry=" + ticket.getMaxAge() + "&gotoURL=" + gotoURL);
					} else {
						if(special==null||!special.equals("is")){
							request.getRequestDispatcher("maintain.jsp").forward(request, response);
						}else{
							request.getRequestDispatcher("login.jsp").forward(request, response);
						}
						
					}
				}else{
					if(tickets.containsKey(decodedTicket)) {
						String setCookieURL = request.getParameter("setCookieURL");
						String gotoURL = request.getParameter("gotoURL");
						if(setCookieURL != null)
			                response.sendRedirect(setCookieURL + "?ticket=" + encodedTicket + "&expiry=" + ticket.getMaxAge() + "&gotoURL=" + gotoURL);
					} else {
						request.getRequestDispatcher("login.jsp").forward(request, response);
					}
				}
				
			
			
		}
	}

	private void authTicket(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if(isMaintain){
			Object special = request.getSession().getAttribute("special");
		//	System.out.println(request.getSession().getId());
			StringBuilder result = new StringBuilder("{");
			PrintWriter out = response.getWriter();
			String encodedTicket = request.getParameter("cookieName");
			if(encodedTicket == null) {
				result.append("\"error\":true,\"errorInfo\":\"Ticket can not be empty!\"");
			} else {
				String decodedTicket = XtlDESUtils.decrypt(encodedTicket, secretKey);
				if(tickets.containsKey(decodedTicket)&&special!=null && special.equals("is")){
					result.append("\"error\":false,\"username\":").append("\"" + tickets.get(decodedTicket).getUsername() + "\"");
					result.append("}");
					out.print(result);
				}
				else{
					if(special==null|| !special.equals("is")){
						result.append("\"error\":true,\"errorInfo\":\"special is bad!\"");
						result.append("}");
						out.print(result);
					}else{
						result.append("\"error\":true,\"errorInfo\":\"Ticket is not found!\"");
						result.append("}");
						out.print(result);
					}
				}
					
			}
			
			
		}else{
			StringBuilder result = new StringBuilder("{");
			PrintWriter out = response.getWriter();
			String encodedTicket = request.getParameter("cookieName");
			if(encodedTicket == null) {
				result.append("\"error\":true,\"errorInfo\":\"Ticket can not be empty!\"");
			} else {
				String decodedTicket = XtlDESUtils.decrypt(encodedTicket, secretKey);
				if(tickets.containsKey(decodedTicket))
					result.append("\"error\":false,\"username\":").append("\"" + tickets.get(decodedTicket).getUsername() + "\"");
				else
					result.append("\"error\":true,\"errorInfo\":\"Ticket is not found!\"");
			}
			result.append("}");
			out.print(result);
		}
		
	}

	private void doLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		StringBuilder result = new StringBuilder("{");
		PrintWriter out = response.getWriter();
		String encodedTicket = request.getParameter("cookieName");
		if(encodedTicket == null) {
			result.append("\"error\":true,\"errorInfo\":\"Ticket can not be empty!\"");
		} else {
			String decodedTicket = XtlDESUtils.decrypt(encodedTicket, secretKey);
			tickets.remove(decodedTicket);
			result.append("\"error\":false");
		}
		result.append("}");
		out.print(result);
	}

	private void doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String pass = accounts.get(username);
		if(pass == null || !pass.equals(password)) {
			request.getRequestDispatcher("login.jsp?errorInfo=username or password is wrong!").forward(request, response);
		} else {
			String ticketKey = UUID.randomUUID().toString().replace("-", "");
			String encodedticketKey = XtlDESUtils.encrypt(ticketKey, secretKey);
			
			Timestamp createTime = new Timestamp(System.currentTimeMillis());
			Calendar cal = Calendar.getInstance();
			cal.setTime(createTime);
			cal.add(Calendar.MINUTE, ticketTimeout);
			Timestamp recoverTime = new Timestamp(cal.getTimeInMillis());
			XtlTicket ticket = new XtlTicket(username, createTime, recoverTime);
			
			tickets.put(ticketKey, ticket);

			String[] checks = request.getParameterValues("autoAuth");
			int expiry = -1;
			if(checks != null && "1".equals(checks[0]))
				expiry = 7 * 24 * 3600;
			Cookie cookie = new Cookie(cookieName, encodedticketKey);
			cookie.setSecure(secure);// 为true时用于https
			cookie.setMaxAge(expiry);
			cookie.setPath("/");
			response.addCookie(cookie);

			String setCookieURL = request.getParameter("setCookieURL");
			String gotoURL = request.getParameter("gotoURL");
			
			PrintWriter out = response.getWriter();
			out.print("<script type='text/javascript'>");
			out.print("document.write(\"<form id='url' method='post' action='" + setCookieURL + "'>\");");
			out.print("document.write(\"<input type='hidden' name='gotoURL' value='" + gotoURL + "' />\");");
			out.print("document.write(\"<input type='hidden' name='ticket' value='" + encodedticketKey + "' />\");");
			out.print("document.write(\"<input type='hidden' name='expiry' value='" + expiry + "' />\");");
			out.print("document.write(\"<input type='hidden' name='special' value='" + request.getSession().getAttribute("special") + "' />\");");
			out.print("document.write('</form>');");
			out.print("document.getElementById('url').submit();");
			out.print("</script>");
		}
	}
	
	public void ReadProperties(String fileName) throws IOException{
		Properties p = new Properties();
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		p.load(in);
		String flag = p.getProperty("MaintainFlag");
		if(flag.equals("true")){
			isMaintain = true;
		}
		if(flag.equals("false")){
			isMaintain = false;
		}
		if(in!=null){
			in.close();
		}
	}

}
