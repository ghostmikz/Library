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

    public MainController(MainFrame view, User user) {
        this.view = view;
        this.user = user;
        buildPanels();
        view.setLogoutListener(this::doLogout);
    }

    private void buildPanels() {
        DashboardPanel dash = new DashboardPanel();
        DashboardController dashCtrl = new DashboardController(dash, user);
        view.addPanel(dash, "DASHBOARD");

        BooksPanel books = new BooksPanel();
        BooksController booksCtrl = new BooksController(books, user);
        view.addPanel(books, "BOOKS");

        BorrowsPanel borrows = new BorrowsPanel();
        BorrowsController borrowsCtrl = new BorrowsController(borrows, user.getToken());
        view.addPanel(borrows, "BORROWS");

        // When books load, update the borrow form's available-book dropdown
        booksCtrl.setOnBooksLoaded(borrowsCtrl::updateBooks);

        // When a borrow or return happens, refresh the book list + dashboard stats
        borrowsCtrl.setMutationListener(() -> {
            booksCtrl.reload();
            dashCtrl.reload();
        });

        if (user.isAdmin()) {
            LibrariansPanel lib = new LibrariansPanel();
            new LibrariansController(lib, user);
            view.addPanel(lib, "LIBRARIANS");
        }

        view.showPanel("DASHBOARD");
    }

    private void doLogout() {
        LoginFrame login = new LoginFrame();
        new LoginController(login);
        login.setVisible(true);
        view.close();
        new Thread(() -> {
            try {
                SocketClient.getInstance().send("LOGOUT", user.getToken());
                SocketClient.getInstance().disconnect();
            } catch (Exception ignored) {}
        }).start();
    }
}
