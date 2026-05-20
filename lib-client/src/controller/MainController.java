package controller;

import client.SocketClient;
import model.User;
import view.LoginFrame;
import view.MainFrame;
import view.panels.BooksPanel;
import view.panels.BorrowsPanel;
import view.panels.DashboardPanel;
import view.panels.LibrariansPanel;

public class MainController {

    private final MainFrame view;
    private final User      user;
    private BorrowsController borrowsController;

    public MainController(MainFrame view, User user) {
        this.view = view;
        this.user = user;
        buildPanels();
        view.setLogoutListener(this::doLogout);
    }

    private void buildPanels() {
        DashboardPanel dash = new DashboardPanel();
        new DashboardController(dash, user);
        view.addPanel(dash, "DASHBOARD");

        BooksPanel books = new BooksPanel();
        BooksController booksCtrl = new BooksController(books, user);
        view.addPanel(books, "BOOKS");

        BorrowsPanel borrows = new BorrowsPanel();
        borrowsController = new BorrowsController(borrows, user.getToken());
        booksCtrl.setOnBooksLoaded(borrowsController::updateBooks);
        view.addPanel(borrows, "BORROWS");

        if (user.isAdmin()) {
            LibrariansPanel lib = new LibrariansPanel();
            new LibrariansController(lib, user);
            view.addPanel(lib, "LIBRARIANS");
        }

        view.showPanel("DASHBOARD");
    }

    private void doLogout() {
        try {
            SocketClient.getInstance().send("LOGOUT", user.getToken());
            SocketClient.getInstance().disconnect();
        } catch (Exception ignored) {}
        LoginFrame login = new LoginFrame();
        new LoginController(login);
        login.setVisible(true);
        view.close();
    }
}
