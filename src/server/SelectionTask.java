package worth.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import worth.data.UserStatus;
import worth.exceptions.AlreadyLoggedException;
import worth.protocol.CommunicationProtocol;
import worth.protocol.ResponseMessage;
import worth.exceptions.UserNotExistsException;
import worth.exceptions.WrongPasswordException;
import worth.server.rmi.RMICallbackServiceImpl;

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
    private final ObjectMapper mapper;
    RMICallbackServiceImpl callbackService;

    public SelectionTask(TCPOperations data, RMICallbackServiceImpl callbackService) {
        this.data = data;
        this.callbackService = callbackService;
        this.mapper = new ObjectMapper();
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
                    } else if (key.isReadable()) {
                        // client ha scritto su channel, sono pronto a leggerlo
                        SocketChannel client = (SocketChannel) key.channel();

                        Attachment attachment = (Attachment) key.attachment();

                        // la prima volta l'attachment sarà null, lo istanzio
                        if (attachment == null) {
                            attachment = new Attachment();
                        }

                        ByteBuffer buffer = attachment.getBuffer();

                        // null la prima volta, poi sarà sempre allocato
                        if (buffer == null) {
                            buffer = ByteBuffer.allocate(ALLOCATION_SIZE);
                        }

                        // read message from channel

                        int byteReaded = client.read(buffer);

                        // è stata chiusa la connessione dal client
                        if (byteReaded == -1) {
                            // se utente loggato, devo fare logout
                            String username = attachment.getUsername();
                            if (username != null) {
                                try {
                                    data.logout(username);

                                    // notifico altri utenti che username è offline
                                    callbackService.notifyUsers(username, UserStatus.OFFLINE);
                                } catch (UserNotExistsException e) {
                                    e.printStackTrace();
                                }
                            }
                            key.cancel();
                            client.close();
                            continue;
                        }

                        buffer.flip();
                        String messageReceived = StandardCharsets.UTF_8.decode(buffer).toString();

                        // splitto messaggio attraverso separatore
                        String[] tokens = messageReceived.split(CommunicationProtocol.SEPARATOR);
                        String command = tokens[0]; // la prima stringa è il comando

                        List<String> arguments = this.decodeMessageArguments(tokens); // decodifico altre stringhe

                        // preparo response code
                        int responseCode = CommunicationProtocol.UNKNOWN;
                        // preparo response body
                        String responseBody = null;

                        switch (command) {
                            case CommunicationProtocol.LOGIN_CMD: {
                                // controllo numero di parametri
                                if (arguments.size() != 2) {
                                    responseCode = CommunicationProtocol.COMMUNICATION_ERROR;
                                    break;
                                }

                                String username = arguments.get(0);
                                String hash = arguments.get(1);
                                try {
                                    data.login(username, hash);

                                    // corpo risposta: lista utenti e loro stato
                                    Map<String, UserStatus> userStatus = data.getUserStatus();
                                    responseBody = this.mapper.writeValueAsString(userStatus);

                                    // notifica gli utenti che ora l'utente 'username' è online
                                    callbackService.notifyUsers(username, UserStatus.ONLINE);

                                    // inserisco negli attachment il nome utente
                                    attachment.setUsername(username);
                                } catch (UserNotExistsException e) {
                                    responseCode = CommunicationProtocol.USER_NOT_EXISTS;
                                } catch (AlreadyLoggedException e) {
                                    responseCode = CommunicationProtocol.LOGIN_ALREADY_LOGGED;
                                }catch (WrongPasswordException e) {
                                    responseCode = CommunicationProtocol.LOGIN_WRONGPWD;
                                }
                                break;
                            }
                            case CommunicationProtocol.LOGOUT_CMD: {
                                // il nome utente viene prelevato direttamente dal server
                                String username = attachment.getUsername();
                                if (username != null) {
                                    try {
                                        data.logout(username);

                                        // notifico altri utenti che username è offline
                                        callbackService.notifyUsers(username, UserStatus.OFFLINE);
                                    } catch (UserNotExistsException e) {
                                        responseCode = CommunicationProtocol.USER_NOT_EXISTS;
                                    }
                                }
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
                            }
                        }

                        // se codice ancora non identificato => successo
                        if (responseCode == CommunicationProtocol.UNKNOWN) {
                            responseCode = CommunicationProtocol.OP_SUCCESS;
                        }

                        ResponseMessage response = new ResponseMessage(
                                responseCode,
                                responseBody
                        );

                        // preparo messaggio da inviare
                        byte[] byteResponse = mapper.writeValueAsBytes(response);
                        buffer = ByteBuffer.wrap(byteResponse);

                        // salvo il buffer negli attachment
                        attachment.setBuffer(buffer);

                        // preparo per scrittura su client
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE, attachment);
                    } else if (key.isWritable()) { // client aspetta scrittura su channel
                        SocketChannel client = (SocketChannel) key.channel();

                        Attachment attachment = (Attachment) key.attachment();

                        ByteBuffer buffer = attachment.getBuffer();

                        try {
                            client.write(buffer);
                        }
                        catch (IOException e) {
                            client.close();
                        }
                        if (!buffer.hasRemaining())
                            buffer.clear();

                        // preparo per prossima lettura da client
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_READ, attachment);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param elements array di N parametri, i cui ultimi N-1 sono argomenti da dover decodificare
     *
     * @return una lista contenente gli N-1 parametri decodificati in Base64
     *
     * */
    private List<String> decodeMessageArguments(String[] elements) {
        List<String> args = new ArrayList<>();
        for (int i = 1; i < elements.length; i++) {
            String decoded = new String(Base64.getDecoder().decode(elements[i]), StandardCharsets.UTF_8);
            args.add(decoded);
        }
        return args;
    }

}
