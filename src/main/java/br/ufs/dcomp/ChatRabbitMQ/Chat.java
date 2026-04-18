package br.ufs.dcomp.ChatRabbitMQ;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Chat {

    private static String currentTarget = null;
    private static String currentPrompt = "<< ";

    public static void main(String[] args) throws Exception {

        // ===== CONFIGURAÇÃO =====
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("34.225.32.172"); // Rivaldo
        //factory.setHost("100.25.45.122"); // Henrick
        factory.setUsername("admin"); // Alterar
        factory.setPassword("password"); // Alterar
        factory.setVirtualHost("/");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        Scanner scanner = new Scanner(System.in);

        // ===== USUÁRIO =====
        System.out.print("User: ");
        String username = scanner.nextLine().trim();

        // cria fila do usuário
        channel.queueDeclare(username, false, false, false, null);

        System.out.println("Bem-vindo, " + username + "!");
        System.out.print(currentPrompt);

        // ===== CONSUMER (THREAD SEPARADA) =====
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {

                String message = new String(body, StandardCharsets.UTF_8);

                // quebra linha para não misturar com o prompt
                System.out.println("\n" + message);
                System.out.print(currentPrompt);
            }
        };

        channel.basicConsume(username, true, consumer);

        // ===== LOOP PRINCIPAL =====
        while (true) {
            String input = scanner.nextLine().trim();

            // troca de destinatário
            if (input.startsWith("@")) {
                currentTarget = input.substring(1);

                if (currentTarget.isEmpty()) {
                    System.out.print(currentPrompt);
                    continue;
                }

                // garante que a fila do destinatário existe
                channel.queueDeclare(currentTarget, false, false, false, null);

                currentPrompt = "@" + currentTarget + "<< ";
                System.out.print(currentPrompt);
                continue;
            }

            // se não definiu destinatário
            if (currentTarget == null) {
                System.out.println("Use @usuario para escolher o destinatário");
                System.out.print(currentPrompt);
                continue;
            }

            // monta mensagem
            String formattedMessage = formatMessage(username, input);

            // envia para fila do destinatário
            channel.basicPublish(
                    "",
                    currentTarget,
                    null,
                    formattedMessage.getBytes(StandardCharsets.UTF_8)
            );

            System.out.print(currentPrompt);
        }
    }

    // ===== FORMATAÇÃO =====
    private static String formatMessage(String from, String message) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

        String timestamp = LocalDateTime.now().format(formatter);

        return "(" + timestamp + ") @" + from + " diz: " + message;
    }
}