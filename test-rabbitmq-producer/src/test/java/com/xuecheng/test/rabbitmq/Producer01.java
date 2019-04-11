package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/*
    rabbitmq的入门程序
 */
public class Producer01 {
    private static final String QUEUE = "helloworld";
    public static void main(String[] args) {
        //通过连接工厂创建新的连接 //和mq建立连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);//端口
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //设置虚拟机 一个mq服务可以设置多个虚拟机，每个虚拟机就相当于一个独立的mq
        connectionFactory.setVirtualHost("/");
        //建立新连接
        Connection connection = null;
        Channel channel = null;
        try {
            connection = connectionFactory.newConnection();
            //创建会话通道,生产者和mq服务所有通信都在channel中完成
            channel = connection.createChannel();
            //声明队列  如果队列在mq中没有则要创建
            // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
            /*
                queue:队列名称
                durable：是否持久化 如果持久化，mq重启后队列还在。
                exclusive：是否独占连接，队列只允许在该连接中访问，如果连接关闭，队列自动删除，如果将此参数设置为true,可用于临时队列的创建
                autoDelete：自动删除，连接不用时。
                arguments: 参数，可以设置队列的扩展参数。比如，可设置存活时间
             */
            channel.queueDeclare(QUEUE, true,false ,false ,null );
            //发送消息
            /*
                参数：
               String exchange, String routingKey, BasicProperties props, byte[] body
               参数明细
               exchange ： 交换机，如果不指定将使用mq的默认交换机 使用 " "
               routingKey ： 路由key ,交换机根据路由key来将消息转发到指定的队列。如果使用默认交换机，routingKey设置为队列的名称
               props ： 消息的属性
               body ： 消息内容

             */
            //消息内容
            String message= "hello world 黑马程序员";
            channel.basicPublish("",QUEUE,null, message.getBytes());
            System.out.println("send to mq "+ message);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //先关闭通道,再关闭连接
            try {
                channel.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
