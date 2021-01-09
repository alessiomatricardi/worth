package worth.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import worth.data.Card;
import worth.data.CardStatus;
import worth.data.UserStatus;
import worth.exceptions.*;
import worth.protocol.CommunicationProtocol;
import worth.protocol.ResponseMessage;
import worth.server.rmi.RMICallbackServiceImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by alessiomatricardi on 03/01/21
 */
public class SelectionTask implements Runnable {
    private static final int ALLOCATION_SIZE = 1024; // size (in byte) per allocazione di un ByteBuffer
    private final TCPOperations data;       // dati dell'applicazione
    private final ObjectMapper mapper;      // mapper utilizzato per serializzazione/deserializzazione Jackson
    RMICallbackServiceImpl callbackService; // servizio di callback

    public SelectionTask(TCPOperations data, RMICallbackServiceImpl callbackService) {
        this.data = data;
        this.callbackService = callbackService;

        this.mapper = new ObjectMapper();
        // abilita indentazione
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // formattazione data
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        this.mapper.setDateFormat(dateFormat);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void run() {
        ServerSocketChannel serverChannel;
        Selector selector = null;

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
                        } catch (IOException e) {
                            e.printStackTrace();
                            server1.close();
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

                        // alloco buffer
                        ByteBuffer buffer = ByteBuffer.allocate(ALLOCATION_SIZE);

                        // read message from channel
                        int byteReaded;
                        int totalReaded = 0;
                        int messageLength = -1;
                        StringBuilder messageReceived = new StringBuilder();
                        do {
                            byteReaded = client.read(buffer);
                            if (byteReaded == -1) break;
                            totalReaded += byteReaded;

                            buffer.flip();

                            // salvo lunghezza del messaggio
                            if (messageLength == -1)
                                messageLength = buffer.getInt();

                            messageReceived.append(StandardCharsets.UTF_8.decode(buffer).toString());

                            buffer.clear();
                        } while (totalReaded < messageLength);

                        // è stata chiusa la connessione dal client
                        if (byteReaded == -1) {
                            // se l'utente è online, devo fare log out
                            String username = attachment.getUsername();
                            if (username != null) {
                                try {
                                    data.logout(username);
                                    callbackService.notifyUsers(username, UserStatus.OFFLINE);
                                } catch (UserNotExistsException e) {
                                    e.printStackTrace();
                                }
                            }

                            key.cancel();
                            client.close();
                            continue;
                        }

                        // splitto messaggio attraverso separatore
                        String[] tokens = messageReceived.toString().split(CommunicationProtocol.SEPARATOR);
                        String command = tokens[0]; // la prima stringa è il comando

                        // decodifico gli argomenti
                        List<String> arguments = this.decodeMessageArguments(tokens);

                        // preparo response code
                        int responseCode = CommunicationProtocol.UNKNOWN;
                        // preparo response body
                        String responseBody = null;

                        // in base al comando, ci saranno diversi comportamenti
                        switch (command) { // todo manca qualcosa
                            // todo server scrive su chat multicast
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
                            case CommunicationProtocol.LISTPROJECTS_CMD: {
                                break; // todo
                            }
                            case CommunicationProtocol.CREATEPROJECT_CMD: {
                                // controllo numero di parametri
                                if (arguments.size() != 1) {
                                    responseCode = CommunicationProtocol.COMMUNICATION_ERROR;
                                    break;
                                }

                                // recupero l'utente che ha fatto la richiesta
                                String username = attachment.getUsername();

                                String projectName = arguments.get(0);
                                if (!projectName.matches(CommunicationProtocol.STRING_REGEX)) {
                                    responseCode = CommunicationProtocol.CREATEPROJECT_CHAR_NOT_ALLOW;
                                    break;
                                }

                                try {
                                    data.createProject(projectName, username);
                                } catch (ProjectAlreadyExistsException e) {
                                    responseCode = CommunicationProtocol.CREATEPROJECT_ALREADYEXISTS;
                                } catch (NoSuchAddressException e) {
                                    responseCode = CommunicationProtocol.CREATEPROJECT_NOMOREADDRESSES;
                                }
                                break;
                            }
                            case CommunicationProtocol.ADD_MEMBER_CMD: {
                                // controllo numero di parametri
                                if (arguments.size() != 2) {
                                    responseCode = CommunicationProtocol.COMMUNICATION_ERROR;
                                    break;
                                }
                                // il nome utente viene prelevato direttamente dal server
                                String username = attachment.getUsername();

                                String projectName = arguments.get(0);
                                String userToAdd = arguments.get(1);
                                try {
                                    data.addMember(projectName, userToAdd, username);
                                } catch (ProjectNotExistsException e) {
                                    responseCode = CommunicationProtocol.PROJECT_NOT_EXISTS;
                                } catch (UnauthorizedUserException e) {
                                    responseCode = CommunicationProtocol.UNAUTHORIZED;
                                } catch (UserAlreadyPresentException e) {
                                    responseCode = CommunicationProtocol.ADD_MEMBER_ALREADYPRESENT;
                                } catch (UserNotExistsException e) {
                                    responseCode = CommunicationProtocol.USER_NOT_EXISTS;
                                }
                                break;
                            }
                            case CommunicationProtocol.SHOW_MEMBERS_CMD: {
                                break; // todo
                            }
                            case CommunicationProtocol.SHOW_CARDS_CMD: {
                                // controllo numero di parametri
                                if (arguments.size() != 1) {
                                    responseCode = CommunicationProtocol.COMMUNICATION_ERROR;
                                    break;
                                }
                                // il nome utente viene prelevato direttamente dal server
                                String username = attachment.getUsername();

                                String projectName = arguments.get(0);
                                try {
                                    List<String> cards = data.showCards(projectName, username);
                                    responseBody = this.mapper.writeValueAsString(cards);
                                } catch (ProjectNotExistsException e) {
                                    responseCode = CommunicationProtocol.PROJECT_NOT_EXISTS;
                                } catch (UnauthorizedUserException e) {
                                    responseCode = CommunicationProtocol.UNAUTHORIZED;
                                }
                                break;
                            }
                            case CommunicationProtocol.SHOW_CARD_CMD: {
                                break; // todo
                            }
                            case CommunicationProtocol.ADD_CARD_CMD: {
                                // controllo numero di parametri
                                if (arguments.size() != 1) {
                                    responseCode = CommunicationProtocol.COMMUNICATION_ERROR;
                                    break;
                                }
                                // il nome utente viene prelevato direttamente dal server
                                String username = attachment.getUsername();

                                String projectName = arguments.get(0);
                                String cardName = arguments.get(1);
                                String description = arguments.get(2);
                                try {
                                    data.addCard(projectName, cardName, description, username);
                                } catch (ProjectNotExistsException e) {
                                    responseCode = CommunicationProtocol.PROJECT_NOT_EXISTS;
                                } catch (UnauthorizedUserException e) {
                                    responseCode = CommunicationProtocol.UNAUTHORIZED;
                                } catch (CardAlreadyExistsException e) {
                                    responseCode = CommunicationProtocol.ADD_CARD_ALREADYEXISTS;
                                }
                                break;
                            }
                            case CommunicationProtocol.MOVE_CARD_CMD: {
                                // controllo numero di parametri
                                if (arguments.size() != 4) {
                                    responseCode = CommunicationProtocol.COMMUNICATION_ERROR;
                                    break;
                                }
                                // il nome utente viene prelevato direttamente dal server
                                String username = attachment.getUsername();

                                String projectName = arguments.get(0);
                                String cardName = arguments.get(1);
                                CardStatus from = this.mapper.readValue(
                                        arguments.get(2),
                                        new TypeReference<CardStatus>() {}
                                );
                                CardStatus to = this.mapper.readValue(
                                        arguments.get(3),
                                        new TypeReference<CardStatus>() {}
                                );;

                                try {
                                    data.moveCard(projectName, cardName, from, to, username);
                                } catch (ProjectNotExistsException e) {
                                    responseCode = CommunicationProtocol.PROJECT_NOT_EXISTS;
                                } catch (UnauthorizedUserException e) {
                                    responseCode = CommunicationProtocol.UNAUTHORIZED;
                                } catch (CardNotExistsException e) {
                                    responseCode = CommunicationProtocol.CARD_NOT_EXISTS;
                                } catch (OperationNotAllowedException e) {
                                    responseCode = CommunicationProtocol.MOVE_CARD_NOT_ALLOWED;
                                }
                                break;
                            }
                            case CommunicationProtocol.CARD_HISTORY_CMD: {
                                break; // todo
                            }
                            case CommunicationProtocol.READ_CHAT_CMD: {
                                break; // todo
                            }
                            case CommunicationProtocol.CANCELPROJECT_CMD: {
                                // controllo numero di parametri
                                if (arguments.size() != 1) {
                                    responseCode = CommunicationProtocol.COMMUNICATION_ERROR;
                                    break;
                                }
                                // il nome utente viene prelevato direttamente dal server
                                String username = attachment.getUsername();

                                String projectName = arguments.get(0);
                                try {
                                    data.cancelProject(projectName, username);
                                } catch (ProjectNotExistsException e) {
                                    responseCode = CommunicationProtocol.PROJECT_NOT_EXISTS;
                                } catch (UnauthorizedUserException e) {
                                    responseCode = CommunicationProtocol.UNAUTHORIZED;
                                } catch (ProjectNotCloseableException e) {
                                    responseCode = CommunicationProtocol.CANCELPROJECT_NOTCLOSEABLE;
                                }
                                break;
                            }
                        }

                        // se codice ancora non identificato => successo
                        if (responseCode == CommunicationProtocol.UNKNOWN) {
                            responseCode = CommunicationProtocol.OP_SUCCESS;
                        }

                        // preparo messaggio di risposta
                        ResponseMessage response = new ResponseMessage(
                                responseCode,
                                responseBody
                        );

                        // lo serializzo e lo inserisco nel buffer
                        byte[] byteResponse = this.mapper.writeValueAsBytes(response);
                        // calcolo lunghezza messaggio
                        messageLength = byteResponse.length;
                        buffer = ByteBuffer.allocate(Integer.BYTES + messageLength);
                        // inserisco lunghezza e messaggio
                        buffer.putInt(messageLength).put(byteResponse);
                        buffer.flip();

                        // salvo il buffer negli attachment
                        attachment.setBuffer(buffer);

                        // preparo per scrittura su client
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE, attachment);
                    } else if (key.isWritable()) {
                        // client aspetta scrittura su channel
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
