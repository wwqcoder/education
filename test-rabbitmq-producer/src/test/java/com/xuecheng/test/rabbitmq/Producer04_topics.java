package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Producer04_topics {
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_TOPICS_INFORM = "exchange_topics_inform";
    private static final String ROUTINGKEY_EMAIL = "inform.#.email.#"; // info.email.sms
    private static final String ROUTINGKEY_SMS = "inform.#.sms.#"; //info.email.sms
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
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true,false ,false ,null );
            channel.queueDeclare(QUEUE_INFORM_SMS, true,false ,false ,null );
            //声明一个交换机
            /*
                String exchange, String type
                exchange : 交换机的名称
                type :  交换机的类型
                    fanout: 对应的rabbitmq的工作模式是 发布订阅 Publish/subscribe
                    direct:  对应的rabbitmq的工作模式是 routing
                    topic:  对应的rabbitmq的工作模式是 通配符
                    header:  对应的rabbitmq的工作模式是  header
             */
            channel.exchangeDeclare(EXCHANGE_TOPICS_INFORM, BuiltinExchangeType.TOPIC);
            //进行交换机与队列绑定
            /*
                String queue, String exchange, String routingKey
                queue 队列名称
                exchange 交换机名称
                routingKey 路由key  作用是交换机根据路由key的值将消息转发到指定的队列中。在发布/订阅设置为" "
             */
            channel.queueBind(QUEUE_INFORM_EMAIL,EXCHANGE_TOPICS_INFORM,ROUTINGKEY_EMAIL);
            channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_TOPICS_INFORM,ROUTINGKEY_SMS);

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
            for (int i = 0; i < 5; i++) {
                //消息内容 发送消息需要指定routingKey
                String message= "email  发送消息 ";
                channel.basicPublish(EXCHANGE_TOPICS_INFORM,"inform.email",null, message.getBytes());
                System.out.println("send to mq "+ message);
            }

            for (int i = 0; i < 5; i++) {
                //消息内容 发送消息需要指定routingKey
                String message= "sms  发送消息 ";
                channel.basicPublish(EXCHANGE_TOPICS_INFORM,"inform.sms",null, message.getBytes());
                System.out.println("send to mq "+ message);
            }
            for (int i = 0; i < 5; i++) {
                //消息内容 发送消息需要指定routingKey
                String message= "sms or email  发送消息 ";
                channel.basicPublish(EXCHANGE_TOPICS_INFORM,"inform.email.sms",null, message.getBytes());
                System.out.println("send to mq "+ message);
            }
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
