package com.atguigu.servlet;

import java.io.IOException;
import java.util.Random;

import javax.naming.LimitExceededException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.VerifyCodeConfig;
import com.sun.java_cup.internal.runtime.virtual_parse_stack;

import redis.clients.jedis.Jedis;

/**
 * Servlet implementation class VerifiCodeServlet
 */
public class CodeSenderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public CodeSenderServlet() {
        super();
        // TODO Auto-generated constructor stub
    }


    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Jedis jedis=new Jedis("192.168.22.128",6379);

        //获取手机号
        String phoneNo = request.getParameter("phone_no");
        if(phoneNo==null){
            return;
        }


        String countKey=VerifyCodeConfig.PHONE_PREFIX+phoneNo+VerifyCodeConfig.COUNT_SUFFIX;
        String count = jedis.get(countKey);
        //第一次发送
        if(count==null){
            jedis.setex(countKey,VerifyCodeConfig.SECONDS_PER_DAY,"1");
        }else{
            //不是第一次发送
            int countint = Integer.parseInt(count);
            if(countint<3){
                jedis.incr(countKey);//每次加1
            }else{
                System.out.println("超过3次");
                response.getWriter().write("limit");
                return;
            }
        }


        //生成6位的验证码
        String code = genCode(6);
        System.out.println(code);//向手机发送验证码
        //把手机号存到redis中 2分钟有效
       /* jedis.set("code",code);
        jedis.expire("code",120);*/
        //phoneNo:1212123:code
        String codeKey= VerifyCodeConfig.PHONE_PREFIX+phoneNo+VerifyCodeConfig.CODE_SUFFIX;
        jedis.setex(codeKey,120,code);
        response.getWriter().write("true");



    }


    private String genCode(int len) {
        String code = "";
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            int rand = random.nextInt(10);
            code += rand;
        }
        return code;
    }

}
