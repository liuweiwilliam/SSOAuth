package com.xtl.xcssoauth.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class XtlMD5 {
	public static String md5(String str){
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
	        // ����md5����
	        md.update(str.getBytes());
	        // digest()���ȷ������md5 hashֵ������ֵΪ8Ϊ�ַ�������Ϊmd5 hashֵ��16λ��hexֵ��ʵ���Ͼ���8λ���ַ�
	        // BigInteger������8λ���ַ���ת����16λhexֵ�����ַ�������ʾ���õ��ַ�����ʽ��hashֵ
	        return new BigInteger(1, md.digest()).toString(16);
	    } catch (Exception e) {
	        System.out.println("MD5���ܳ��ִ���");
	        return "";
	    }
	}
}
