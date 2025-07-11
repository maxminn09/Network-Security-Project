import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server {

    public static void main(String[] args) {
        int port = 2250;
        System.out.format("Listening for a client on port %d\n", port);

        // Set SSL properties
        System.setProperty("javax.net.ssl.keyStore", "jill.p12"); // Replace with the path to your keystore file
        System.setProperty("javax.net.ssl.keyStorePassword", "asdfgh"); // Replace with your keystore password

        try {
            // SSL setup
            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);
            SSLSocket socket = (SSLSocket) serverSocket.accept();
            socket.setTcpNoDelay(true);
            socket.startHandshake();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            System.out.format(
                    "Connected by %s:%d\n",
                    socket.getInetAddress().toString(),
                    socket.getPort()
            );

            // Receive a message from the client
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte data[] = new byte[1024];
            int bytesRead = in.read(data);
            baos.write(data, 0, bytesRead);
            System.out.format("Client -> Server: %s\n", baos.toString());

            // Send a message to the client
            out.write(" I'll do the best I can. Let's get moving.".getBytes());

            // Closing the streams and the socket
            in.close();
            out.close();
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
