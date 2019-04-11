package com.xuecheng.manage_media_process;

import com.xuecheng.framework.utils.Mp4VideoUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Administrator
 * @version 1.0
 * @create 2018-07-12 9:11
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestProcessBuilder {

    //使用processBuilder来调用第三方应用程序
    @Test
    public void testProcessBuilder() throws IOException {

        //创建processBuilder对象
        ProcessBuilder processBuilder = new ProcessBuilder();
        //设置第三方应用程序的命令
        processBuilder.command("ipconfig");
        //将标准输入流和错误流合并
        processBuilder.redirectErrorStream(true);
        //启动一个进程
        Process process = processBuilder.start();

        //通过标准输入流来拿到正常和错误的信息
        InputStream inputStream = process.getInputStream();

        //转成字符流
        InputStreamReader reader = new InputStreamReader(inputStream,"gbk");
        //缓冲
        char[] chars = new char[1024];
        int len = -1;
        while((len = reader.read(chars)) != -1){
            String str = new String(chars, 0, len);
            System.out.println(str);
        }
        inputStream.close();
        reader.close();
    }

    //测试使用工具类将avi转成mp4
    @Test
    public void testProcessMp4(){
        String ffmpeg_path = "E:\\BaiduNetdiskDownload\\setup\\ffmpeg-20180227-fa0c9d6-win64-static\\bin\\ffmpeg.exe";
        String video_path = "H:\\WebstormProjects\\develop\\video\\lucene.avi";
        String mp4_name = "1.mp4";
        String mp4folder_path = "H:\\WebstormProjects\\develop\\video\\";
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        //生成mp4
        String result = mp4VideoUtil.generateMp4();
        System.out.println(result);

    }

}
