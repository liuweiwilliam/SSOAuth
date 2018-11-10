<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
	<link href="css/main.css" rel="stylesheet" type="text/css"/>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Language" content="zh-CN"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<script src="http://apps.bdimg.com/libs/jquery/2.1.1/jquery.min.js"></script>
	<!-- 使用钉钉jsapi必须引用此js包 -->
	<script type="text/javascript" src="http://g.alicdn.com/dingding/open-develop/0.8.4/dingtalk.js"></script>
	<script src="http://g.alicdn.com/dingding/dinglogin/0.0.2/ddLogin.js"></script>
	
	<style type="text/css">
		body{
			background-image:url(images/back.jpg);
			background-size:100% 100%;
		}
	</style>
	
	<script type="text/javascript">
		var logined = false;
		var cookies = document.cookie;
		//alert(cookies);
		var start_pos = cookies.indexOf("login");
		//alert("start:" + start_pos);
		var value = "";
		var cookie_end = -1;
		if(start_pos!=-1){
			start_pos+="login".length + 1;
			cookie_end = cookies.indexOf(";", start_pos); 
			
			if(cookie_end==-1){
				cookie_end = cookies.length; 
			}
			//alert("end:" + cookie_end);
			value = unescape(cookies.substring(start_pos, cookie_end));
		}
		
		if(value=="true"){
			//alert("已登录过");
			//window.location.href = "toSuccess.action"
		}else{
			//alert("未登录过");
		}
	
		//dd test
		var hanndleMessage = function (event) {
		    var loginTmpCode = event.data; //拿到loginTmpCode后就可以在这里构造跳转链接进行跳转了
		    var origin = event.origin;
		    //alert("loginTmpCode:" + loginTmpCode);
		    var token = $("input[name='token']").val();
			var redirect_url = "http://www.taolitech.com:8080/XC-IMS/DdSignin.action?struts.token.name=token&token=" + token;
			
		    window.location.href="https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=dingoahndpik0swyu0k8vr&response_type=code&scope=snsapi_login&" + redirect_url + "&loginTmpCode=" + loginTmpCode;
		};
		if (typeof window.addEventListener != 'undefined') {
		    window.addEventListener('message', hanndleMessage, false);
		} else if (typeof window.attachEvent != 'undefined') {
		    window.attachEvent('onmessage', hanndleMessage);
		}
	
		var orig_username;
		var actionMsg;
		function init(){
			/* orig_username = $('#username').val();
			var request = new XMLHttpRequest();
			request.open("GET", "getdebugswitch.action", true);
			request.send();
			request.onreadystatechange = function(){
				if(request.readyState===4 && request.status === 200){
					var rslt = request.responseText;
					if(rslt=="true"){
						$('#div_debug').css("display","");
						$('#debugswitch').val("关闭调试模式");
					}else{
						$('#div_debug').css("display","none");
						$('#debugswitch').val("开启调试模式");
					}
				}
			} */
			
			<%-- actionMsg = "${__MessageStoreInterceptor_ActionMessages_SessionKey }";
			if(actionMsg!=""){
				actionMsg = actionMsg.substring(1, actionMsg.length-1);
				$('#actionMsg').html(actionMsg);
			}
			
			var username = <%=request.getParameter("username")%>;
			if(username!=null){
				$('#username').val(username);
			} --%>
		}
		
		function changeDebugSwitch(){
			var request = new XMLHttpRequest();
			request.open("GET", "debugswitch.action", true);
			request.send();
			request.onreadystatechange = function(){
				if(request.readyState===4 && request.status === 200){
					var rslt = request.responseText;
					if(rslt=="true" || rslt=="false"){
						//alert("设置成功");
					}else{
						alert("设置失败");
					}
				}
			}
			
			if($('#debugswitch').val()=="开启调试模式"){
				$('#debugswitch').val("关闭调试模式");
				$('#div_debug').css("display","");
				
			}else{
				$('#debugswitch').val("开启调试模式");
				$('#div_debug').css("display","none");
			}
			
		}
	
		function setDebugUser(username){
			//此处设置用户信息传递的参数，从而改变用户身份
			if(username==""){
				alert("switch to orig user : " + orig_username);
				$('#debug_username').val(orig_username);
			}else{
				alert("switch to user : " + username);
				$('#debug_username').val(username);
			}
		}
		
		function setDebugDate(){
			var date = $('#date').val().toString();
			//alert(date);
			var request = new XMLHttpRequest();
			request.open("GET", "setdebugdate.action?date=" + date, true);
			request.send();
			request.onreadystatechange = function(){
				if(request.readyState===4 && request.status === 200){
					var rslt = request.responseText;
					if(rslt=="true"){
						alert("设置成功");
					}else{
						alert("设置失败");
					}
				}
			}
		}
		
		function setTimer(){
			setInterval("refresh()",1000);
		}
		
		function refresh(){
			var request = new XMLHttpRequest();
			request.open("GET", "refresh.action", true);
			request.send();
			request.onreadystatechange = function(){
				if(request.readyState===4 && request.status === 200){
					var rslt = request.responseText;
					$('#refresh_text').val(rslt);
				}
			}
		}
		
		function getUserList(){
			var request = new XMLHttpRequest();
			request.open("GET", "getuserlist.action", true);
			request.send();
			request.onreadystatechange = function(){
				if(request.readyState===4 && request.status === 200){
					var rslt = request.responseText;
					$('#userlist_div').html(rslt);
				}
			}
		}
		
		function addUser(){
			var request = new XMLHttpRequest();
			var username = $('#username').val();
			var userid = $('#userid').val();
			
			request.open("GET", "adduser.action?userid=" + userid + "&username=" + username, true);
			request.send();
			request.onreadystatechange = function(){
				if(request.readyState===4 && request.status === 200){
					alert("添加成功");
				}
			}
		}
		
		window.onbeforeunload=onclose;
		function onclose()
		{
			/* var request = new XMLHttpRequest();		
			request.open("GET", "clearsession.action", true);
			request.send();
			request.onreadystatechange = function(){
			} */
		}	
	
		function signInUseAccount() {
			$("#nor_login").show();
			$("#qrcode").hide();
			
			if(document.getElementById("qrcode").innerHTML != null) {
				document.getElementById("qrcode").innerHTML = "";
			}
		
			/* var redirect_url = "http://60.168.242.122:11005/BaseProject/testRedirect.jsp";
			var url = "https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=dingoahndpik0swyu0k8vr&response_type=code&scope=snsapi_login&state=STATE&redirect_uri="+redirect_url;
			window.location.href = (url); */
		}
		function getDDQRCode(){
			$("#nor_login").hide();
			
			if(document.getElementById("qrcode").innerHTML != "") return;

			var token = $("input[name='token']").val();
			var redirect_url = "http://www.taolitech.com:8080/XC-IMS/DdSignin.action";
			//var url = encodeURIComponent("https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=dingoahndpik0swyu0k8vr&response_type=code&scope=snsapi_login&redirect_uri="+redirect_url) + ;
			var url = encodeURIComponent("https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=dingoahndpik0swyu0k8vr&response_type=code&scope=snsapi_login&redirect_uri="+redirect_url);
			var obj = DDLogin({
			     id:"qrcode",
			     goto: url,
			     style: "",
			     href: "",
			     width : "300px",
			     height: "300px"
			 });
			$("#qrcode").show();
		}
		
		function clearActionMsg(){
			
			$('#loginMsg').empty();
		}
	</script>
  </head>
  <body onLoad="init()">
  	<!-- 调试系统区域 -->
  	<!-- <input id="debugswitch" type="button" value="开启调试模式" onclick="changeDebugSwitch()"> -->
	<div id="div_debug" style="display:none;width:100%;">
		
		<!-- 请在option选项中填写对应用户身份信息 -->
		<div id="user_sel">
			<span>切换用户身份</span>
			<input type="text" id="dwbug_username" name=username value="username" style="display:none;">
			<select id="debug_user_sel" name="username" onchange="setDebugUser(this.options[this.options.selectedIndex].value)">
				<option value="">当前用户</option>
				<option value="user1">用户1</option>
				<option value="user2">用户2</option>
			</select>
		</div>
		<hr>
		
		<div id="date_sel">
			<span>调整当前时间</span>
			<input id="debug_date" type="text" name=date style="display:none;">
			<input id="date" type="date" name="debugdate">
			<input type="button" value="设置" onclick="setDebugDate()">
		</div>
		<hr>		
	</div>
	<%-- <s:debug></s:debug> --%>
	<!-- 调试系统区域结束-->
	
	<center>
        <div class="wrap">
            <div class="boxed" style="background:rgba(0,0,0,0)">
                <div class="content" id="nor_login">
                	<h2>欢迎使用西城教学管理系统</h2>
                	<br />
                	<br />
                    <h4>登录</h4>
                    <form class="form" accept-charset="UTF-8" action="${pageContext.request.contextPath }/SSOAuth">
						<input type="hidden" name="action" value="login" />
						<input type="hidden" name="gotoURL" value="${param.gotoURL }" />
						<input type="hidden" name="setCookieURL" value="${param.setCookieURL }" />
						
						<div class="form-item form-type-textfield form-item-mergevars-EMAIL">
							<input placeholder="手机号或昵称" type="text" name="username" id="username" value="${username }" size="25" maxlength="128" class="form-text required" required="required" onfocus="clearActionMsg()"/>
						</div>
						<div class="form-item form-type-textfield form-item-mergevars-EMAIL">
							<input placeholder="密码" type="password" name="password" id="pwd" size="25" maxlength="128" class="form-text required" required="required" onfocus="clearActionMsg()"/>
						</div>
						<span id="loginMsg">${actionMessages[0] }</span>
						<div class="form-actions form-wrapper">
							<input type="submit" id="signin" value="登录" class="form-submit"/>
						</div>
						<input type="text" name="operationMode" value="AUTOMATIC" style="display:none;">
					</form>
				</div>
				<div id="qrcode"></div>
                <form class="form" accept-charset="UTF-8">
	                <div class="form-actions form-wrapper">
	                	<input type="button" id="QRCode" value="扫码登录" class="form-submit" onclick="getDDQRCode()"/>
	                    <span style="width:100px;"></span>
	                    <input type="button" id="DDLog" value="账号登录" class="form-submit" onclick="signInUseAccount();"/>
	                </div>
                </form>
            </div>
        </div>
      </center>
  </body>
</html>
