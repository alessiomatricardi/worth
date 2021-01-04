package worth.server;

import worth.Card;
import worth.CardStatus;
import worth.Movement;
import worth.Project;
import worth.exceptions.UserNotExistsException;
import worth.exceptions.WrongPasswordException;

import java.util.List;

/**
 * Created by alessiomatricardi on 04/01/21
 *
 * Interfaccia che dichiara tutte le operazioni TCP che il server dovrà assolvere
 */
public interface TCPOperations { // todo interface

    void login(String username, String password) throws UserNotExistsException, WrongPasswordException;

    /*void logout(String username);

    List<Project> listProjects();

    void createProject(String projectName);

    void addMember(String projectName, String username);

    List<String> showMembers(String projectName);

    List<Card> showCards(String projectName);

    Card showCard(String projectName);

    void addCard(String projectName, String cardName, String description);

    void moveCard(String projectName, String cardName, CardStatus from, CardStatus to);

    List<Movement> getCardHistory(String projectName, String cardName);

    void cancelProject(String projectName);*/
}
