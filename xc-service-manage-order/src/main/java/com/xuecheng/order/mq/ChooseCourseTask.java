package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChooseCourseTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    TaskService taskService;

    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void receiveFinishChoosecourseTask(XcTask xcTask){
        if (xcTask != null && StringUtils.isNotEmpty(xcTask.getId())){
            taskService.finishTask(xcTask.getId());
        }
    }

    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE})
    public void test(){
        System.out.println(1111);
    }

    //定时发送选课任务
    @Scheduled(cron = "0/3 * * * * *")
    public void sendChoosecourseTask(){
        //得到1分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(GregorianCalendar.MINUTE, -1);
        Date time = calendar.getTime();
        List<XcTask> xctaskList = taskService.findXctaskList(time, 100);
        //调用service发布消息，将添加选课任务发送给mq
        for (XcTask xcTask : xctaskList) {
            //如果取到任务
            if (taskService.getTask(xcTask.getId(), xcTask.getVersion()) > 0){
                String ex = xcTask.getMqExchange();
                String routingkey = xcTask.getMqRoutingkey();
                taskService.publish(xcTask,ex,routingkey);
            }
        }
    }







}
