package worth.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import worth.CommunicationProtocol;
import worth.ResponseMessage;
import worth.exceptions.UserNotExistsException;
import worth.exceptions.WrongPasswordException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by alessiomatricardi on 03/01/21
 */
public class SelectionTask implements Runnable {
    private static final int ALLOCATION_SIZE = 1024*1024; // size (in byte) per allocazione di un ByteBuffer
    private final TCPOperations data;

    public SelectionTask(TCPOperations data) {
        this.data = data;
    }

    public void run() {
        ServerSocketChannel serverChannel;
        Selector selector = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            serverChannel = ServerSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress(
                    CommunicationProtocol.SERVER_IP_ADDRESS,
                    CommunicationProtocol.SERVER_PORT
            );
            ServerSocket server = serverChannel.socket();
            server.bind(address);
            serverChannel.configureBlocking(false); // server socket non bloccante
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT); // registro server per accettare connessioni

            while (true) {
                selector.select();

                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) { // server pronto ad accettare connessione
                        ServerSocketChannel server1 = (ServerSocketChannel) key.channel();
                        try {
                            SocketChannel client = server1.accept(); // non si bloccherà

                            client.configureBlocking(false); // client socket non bloccante

                            // preparo per lettura da client
                            SelectionKey key2 = client.register(selector, SelectionKey.OP_READ);
                        } catch (IOException e) { // todo rivedi
                            e.printStackTrace();
                            try {
                                server1.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            return;
                        }
                    } else if (key.isReadable()) { // client ha scritto su channel, sono pronto a leggerlo
                        SocketChannel client = (SocketChannel) key.channel();

                        ByteBuffer buffer = (ByteBuffer) key.attachment();

                        if (buffer == null) {
                            buffer = ByteBuffer.allocate(ALLOCATION_SIZE);
                        }

                        // read message from channel

                        int byteReaded = client.read(buffer);
                        if (byteReaded == -1) {
                            key.cancel();
                            client.close();
                            continue;
                        }
                        buffer.flip();
                        String messageReceived = StandardCharsets.UTF_8.decode(buffer).toString();

                        String[] tokens = messageReceived.split(CommunicationProtocol.SEPARATOR);
                        List<String> arguments = this.decodeMessageArguments(tokens);

                        // prepara messaggio di risposta
                        ResponseMessage response = null;

                        switch (tokens[0]) {
                            case CommunicationProtocol.LOGIN_CMD: {
                                if (arguments.size() != 2) {
                                    response = new ResponseMessage(
                                            CommunicationProtocol.LOGIN_COMMUNICATION_ERROR,
                                            null
                                    );
                                    break;
                                }
                                String user = arguments.get(0);
                                String hash = arguments.get(1);
                                try {
                                    data.login(user, hash);
                                    response = new ResponseMessage(
                                            CommunicationProtocol.LOGIN_SUCCESS,
                                            null
                                    );
                                } catch (UserNotExistsException e) {
                                    response = new ResponseMessage(
                                            CommunicationProtocol.LOGIN_USERNOTEXISTS,
                                            null
                                    );
                                } catch (WrongPasswordException e) {
                                    response = new ResponseMessage(
                                            CommunicationProtocol.LOGIN_WRONGPWD,
                                            null
                                    );
                                }
                                break;
                            }
                            /* todo
                            case CommunicationProtocol.LOGOUT_CMD: {
                                // todo1
                                break;
                            }
                            case CommunicationProtocol.CREATEPROJECT_CMD: {
                                // todo1
                                break;
                            }
                            case CommunicationProtocol.ADD_CARD_CMD: {
                                // todo1
                                break;
                            }
                            case CommunicationProtocol.MOVE_CARD_CMD: {
                                // todo1
                                break;
                            }
                            case CommunicationProtocol.ADD_MEMBER_CMD: {
                                // todo1
                                break;
                            }
                            case CommunicationProtocol.SHOW_CARDS_CMD: {
                                // todo1
                                break;
                            }
                            case CommunicationProtocol.CANCELPROJECT_CMD: {
                                // todo1
                                break;
                            }*/
                        }

                        // preparo messaggio da inviare e lo salvo sul buffer
                        byte[] byteResponse = mapper.writeValueAsBytes(response);
                        buffer = ByteBuffer.wrap(byteResponse);

                        // preparo per scrittura su client
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE, buffer);
                    } else if (key.isWritable()) { // client aspetta scrittura su channel
                        SocketChannel client = (SocketChannel) key.channel();

                        ByteBuffer buffer = (ByteBuffer) key.attachment();

                        try {
                            client.write(buffer);
                        }
                        catch (IOException e) {
                            client.close();
                        }
                        if (!buffer.hasRemaining())
                            buffer.clear();

                        // preparo per prossima lettura da client
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_READ, buffer);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // todo interface
    private List<String> decodeMessageArguments(String[] elements) {
        List<String> args = new ArrayList<>();
        for (int i = 1; i < elements.length; i++) {
            String decoded = new String(Base64.getDecoder().decode(elements[i]), StandardCharsets.UTF_8);
            args.add(decoded);
        }
        return args;
    }

}
