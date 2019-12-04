package Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Server {

    public static void main(String[] args) {
        try {
            Selector selector = Selector.open();
            int[] ports = {5000, 5001, 5002};

            for (int port : ports) {
                ServerSocketChannel server = ServerSocketChannel.open();
                InetSocketAddress address = new InetSocketAddress("localhost", port);

                server.bind(address);
                server.configureBlocking(false);
                server.register(selector, server.validOps());
            }

            while (true)
            {
                selector.select();
                Set readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();

                while (iterator.hasNext())
                {
                    SelectionKey key = (SelectionKey)iterator.next();

                    if (key.isAcceptable())
                    {
                        SocketChannel client = ((ServerSocketChannel)key.channel()).accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        log("Connection Accepted: " + client.socket().getLocalPort() + "\n");
                    } else if( key.isReadable()) {
                        read(key);
                    }
                    iterator.remove();
                }
            }


        }
        catch (IOException | InterruptedException e)
        {
            e.getMessage();
        }
    }

    private static void read(SelectionKey key) throws IOException, InterruptedException {
        SocketChannel channel = (SocketChannel)key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);
        channel.read(buffer);
        String message = new String(buffer.array()).trim();
        log("Message received: " + message);


        if (message.equals("close"))
        {
            channel.close();
            log("Connection closed");
        } else {
            Thread.sleep(500);
            Scanner input = new Scanner(System.in);
            String msg = input.nextLine();

            write(key, msg);
        }
    }

    private static void write(SelectionKey key, String msg) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        byte[] message = new String(msg).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(message);
        channel.write(buffer);
    }

    private static void log(String str) {
        System.out.println(str);
    }
}
