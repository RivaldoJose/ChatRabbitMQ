package br.ufs.dcomp.ChatRabbitMQ;

//import com.rabbitmq.client.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

//import java.io.IOException;

public class Criador {

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.225.32.172"); // Rivaldo
    //factory.setHost("100.25.45.122"); // Henrick
    factory.setUsername("admin"); // Alterar
    factory.setPassword("password"); // Alterar
    factory.setVirtualHost("/");  
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

                      //(queue-name, durable, exclusive, auto-delete, params); 
    channel.queueDeclare("joao", false,   false,     false,       null);
    channel.queueDeclare("marcio", false,   false,     false,       null);

    //channel.exchangeDeclare("E1", "fanout");
    
    channel.queueBind("joao", "", "joao");
    channel.queueBind("marcio", "", "marcio");
    
    channel.close();
    connection.close();
    
  }
}