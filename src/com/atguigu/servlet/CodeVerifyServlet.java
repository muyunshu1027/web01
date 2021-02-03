package com.atguigu.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.VerifyCodeConfig;

import redis.clients.jedis.Jedis;

/**
 * Servlet implementation class CodeVerifyServlet
 */
public class CodeVerifyServlet extends HttpServlet {


    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public CodeVerifyServlet() {
        super();
        // TODO Auto-generated constructor stub
    }


    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Jedis jedis = new Jedis("192.168.22.128", 6379);
        String phoneNo = request.getParameter("phone_no");
        // 获取客户端的验证码
        String code = request.getParameter("verify_code");
        if (phoneNo == null || code == null){
            return;
        }
        // 获取redis中的验证码
        String codeKey= VerifyCodeConfig.PHONE_PREFIX+phoneNo+VerifyCodeConfig.CODE_SUFFIX;
        String redisCode = jedis.get(codeKey);
        // 比较
        if (redisCode.equals(code)){
            response.getWriter().write("true");
        }

    }
}
