package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //测试redis
    @Test
    public void testRedis(){
        //定义key
        String key = "user_token:e7e37baf-8b15-4410-98b9-37da56d313de";
        //定义value
        HashMap<String, String> value = new HashMap<>();
        value.put("jwt", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Ind3cSIsInNjb3BlIjpbImFwcCJdLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU1MDQxMjg4MywianRpIjoiZTdlMzdiYWYtOGIxNS00NDEwLTk4YjktMzdkYTU2ZDMxM2RlIiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.B9hP6ujVP1hp-Qc_JykAgrChfMzGT-qRSvhsBIFdGGkSr0Rerw1Heb0B1Q1iVWrrX265HbVnreZ-07gasMr35sUo-do5wGhBBH5iM-GELFK33hY4UDp_f8puau10nnaoQlam_TAbBhqQ6LTifozsH0S_4mTNNGURO5zAqLpHOHVuF1f_5MgJhpL5nRLk5Arbb_D4YUMCgTBSCiIxAF6WVQKvlojfyqFMwzUS0LshE9IYJTSEv7x5So58JM1wjfmP3z5vI_KCOuH1Vc6MWN1bkNUNZM1wJmUI7KI9_bW3OuX_rSB9R9DfFmC-DaEImJw9cjI-QFVcgtaf1_KRigV-QQ");
        value.put("refresh_token", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Ind3cSIsInNjb3BlIjpbImFwcCJdLCJhdGkiOiJlN2UzN2JhZi04YjE1LTQ0MTAtOThiOS0zN2RhNTZkMzEzZGUiLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU1MDQxMjg4MywianRpIjoiNjE3NWMyNWEtMmFmZS00MTYzLThhNDAtNTc4Yjk3NjZlZjdhIiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.AnpcISnJ44D6ClX0Hq6SW0wM3Mu1Gz6fXIVPRtfBOKcqRiwF2nUqrDZD_-JJc2hYMlycqwnAyw95ErHueTKRIVLTOEZoM-XMxGoH034p1PYOqsH14FXORi7Tst2JmFfAYutNOB4zXfngjRwFY9P-cw5p-w-fwgySFZOr5FyKF-jEuwCx1DYS6HJhEI4NLLSNIOBO2s4YysF_IoebMj2jA0H2EW8oy-2kUmdXUIyUDG02Y4P61HKYaa4iX6VkRbFoCugVCusoCdQtIwZwsdUkns8LA4GvXDPjFv32jwilvjiW-s_zO5lCMphV7bXc4EA3f9moHiOpJR-oIYREQ0YFyw");
        String jSONString = JSON.toJSONString(value);
        //存储数据
        stringRedisTemplate.boundValueOps(key).set(jSONString, 60, TimeUnit.SECONDS);
        //获取数据
        String s = stringRedisTemplate.opsForValue().get(key);
         System.out.println(s);

    }



}
