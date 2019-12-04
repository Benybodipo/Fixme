package Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Client implements  Runnable{
    int port = 5000;
    public static void main(String[] args) {
        String message = "Hola como estas";
        Client client = new Client();
        Thread thread = new Thread(client);
        thread.start();

    }

    public void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        if (channel.isConnectionPending())
            channel.finishConnect();

        log("Connected");
        channel.configureBlocking(false);
        channel.register(key.selector(), SelectionKey.OP_READ);
    }

    public void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);

        if (channel.read(buffer) != -1)
        {
            String result = new String(buffer.array()).trim();
            log(result);
        }

    }

    public void write(SelectionKey key, String message) throws IOException {
        SocketChannel channel = (SocketChannel)key.channel();
        byte[] msg = new String(message).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(msg);

        channel.write(buffer);
        log("Sending: " + message);
    }

    @Override
    public void run() {

        try {
            Selector selector = Selector.open();
            InetSocketAddress address = new InetSocketAddress("localhost", port);
            SocketChannel channel = SocketChannel.open();

            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect( address);

            log("Connected to server on port " + port);
            String message = "Hello there from client!";

            while (!Thread.interrupted())
            {
                selector.select();
                Iterator iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext())
                {
                    SelectionKey key = (SelectionKey) iterator.next();

                    if (key.isConnectable())
                    {
                        connect(key);
                        Thread.sleep(2000);
                        write(key, message);
                    }
                    else if (key.isReadable())
                    {
                        read(key);

                        if (message.length() > 0)
                        {
                            Thread.sleep(2000);
                            write(key, message);
                        }
                        else
                            channel.close();
                    }
                    iterator.remove();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void log(String str) {
        System.out.println(str);
    }
}
