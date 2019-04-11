package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;

/*
    入门程序消费者
 */
public class Consumer01 {
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

        try {
            connection = connectionFactory.newConnection();
            //创建会话通道,生产者和mq服务所有通信都在channel中完成
             Channel channel = connection.createChannel();
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
            //实现消费方法
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
                //当接收到消息后，此方法将被调用

                /**
                 *
                 * @param consumerTag 消费者标签  用来标识消费者，在监听队列时设置 channel.basicConsume
                 * @param envelope   信封
                 * @param properties  消息属性
                 * @param body    消息的内容
                 * @throws IOException
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //消息ID，mq 在channel中用来标识消息的ID，可用于确认消息已经接收。
                    long deliveryTag = envelope.getDeliveryTag();
                    String message = new String(body, "utf-8");
                    System.out.println("receive : "+message);
                }
            };

            /*
                参数：
                  String queue, boolean autoAck, Consumer callback
                  queue: 队列名称
                  autoAck ： 自动回复，当消费者接收到消息后要告诉mq消息已经接收，如果true，表示会自动回复。
                  callback ： 消费方法，当消息者接收到消息要执行的方法
             */
            channel.basicConsume(QUEUE,true,defaultConsumer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }
}
