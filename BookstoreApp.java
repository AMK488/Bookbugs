// Online Bookstore Management System

import javax.swing.*;
import java.awt.BorderLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.*;
import java.util.*;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;




// Book Class
class Book {
    private String id, title, author, genre;
    private int stock;

    public Book(String id, String title, String author, String genre, int stock) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.stock = stock;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String toString() {
        return title + " by " + author + " (" + genre + ") - Stock: " + stock;
    }
}

// User Classes
abstract class User {
    protected String name, id;
    protected List<Transaction> history = new ArrayList<>();

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Transaction> getHistory() { return history; }

    public abstract void displayInfo();
}

class Customer extends User {
    private List<Book> borrowCart = new ArrayList<>();

    public Customer(String id, String name) {
        super(id, name);
    }

    public void addToCart(Book book) {
        borrowCart.add(book);
    }

    public void checkout() {
        for (Book b : borrowCart) {
            if (b.getStock() > 0) {
                b.setStock(b.getStock() - 1);
                history.add(new Transaction(this, b));
            }
        }
        borrowCart.clear();
    }

    public void displayInfo() {
        System.out.println("Customer: " + name);
    }
}

class Admin extends User {
    public Admin(String id, String name) {
        super(id, name);
    }

    public void displayInfo() {
        System.out.println("Admin: " + name);
    }
}

// Transaction Class
class Transaction {
    private Date date;
    private Book book;
    private User user;

    public Transaction(User user, Book book) {
        this.date = new Date();
        this.user = user;
        this.book = book;
    }

    public String toString() {
        return user.getName() + " borrowed " + book.getTitle() + " on " + date;
    }
}

// Backend Store
class BookStore {
    public static Map<String, Book> bookCatalog = new HashMap<>();
    public static Map<String, User> users = new HashMap<>();
    public static Map<String, String> userPasswords = new HashMap<>();

    public static void registerUser(User user, String password) {
        users.put(user.getId(), user);
        userPasswords.put(user.getId(), password);
    }

    public static User getUser(String id) {
        return users.get(id);
    }

    public static boolean userExists(String id) {
        return users.containsKey(id);
    }

    public static boolean authenticate(String id, String password) {
        return userPasswords.containsKey(id) && userPasswords.get(id).equals(password);
    }
}

// GUI Frame
public class BookstoreApp {
    private JFrame frame;
    private JTextField userField, passField;
    private JTextArea adminOutputArea;
    private JTextArea userOutputArea;
;

    public BookstoreApp() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Look and feel setup failed");
        }

        frame = new JFrame("Online Bookstore Management System");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel loginPanel = new JPanel(new GridLayout(5, 2));
        JLabel title = new JLabel("BOOKSTORE MANAGEMENT SYSTEM", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.BLUE);
        frame.add(title, BorderLayout.NORTH);

        userField = new JTextField();
        passField = new JPasswordField();

        loginPanel.add(new JLabel("User ID:"));
        loginPanel.add(userField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passField);

        JButton loginUserBtn = new JButton("Login as User");
        JButton loginAdminBtn = new JButton("Login as Admin");
        JButton registerBtn = new JButton("Register New User");
        Font buttonFont = new Font("Arial", Font.PLAIN, 16);
        loginUserBtn.setFont(buttonFont);
        loginAdminBtn.setFont(buttonFont);
        registerBtn.setFont(buttonFont);

        loginUserBtn.addActionListener(_ -> {
            String id = userField.getText();
            String pass = passField.getText();
            if (BookStore.authenticate(id, pass)) {
                User user = BookStore.getUser(id);
                if (user instanceof Customer) openUserDashboard();
                else JOptionPane.showMessageDialog(null, "Access Denied. Not a Customer.");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid credentials!");
            }
        });

        loginAdminBtn.addActionListener(_ -> {
            String id = userField.getText();
            String pass = passField.getText();
            if (BookStore.authenticate(id, pass)) {
                User user = BookStore.getUser(id);
                if (user instanceof Admin) openAdminDashboard();
                else JOptionPane.showMessageDialog(null, "Access Denied. Not an Admin.");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid credentials!");
            }
        });

        registerBtn.addActionListener(_ -> registerNewUser());

        loginPanel.add(loginUserBtn);
        loginPanel.add(loginAdminBtn);
        loginPanel.add(new JLabel());
        loginPanel.add(registerBtn);

        frame.add(loginPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void openUserDashboard() {
        JFrame userFrame = new JFrame("User Dashboard");
        userFrame.setSize(600, 400);
        userFrame.setLayout(new BorderLayout());
        userOutputArea = new JTextArea();
        userOutputArea.setEditable(false);
        userFrame.add(new JScrollPane(userOutputArea), BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel();
        JButton showAll = new JButton("Show All Books");
        Font ButtonFont = new Font("Arial", Font.PLAIN, 14);
        showAll.setFont(ButtonFont);
        JTextField searchField = new JTextField(10);
        String[] criteria = {"Title", "Author", "Genre", "Book ID"};
        JComboBox<String> comboBox = new JComboBox<>(criteria);
        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(ButtonFont);

        showAll.addActionListener(_ -> showAllBooks(userOutputArea));
        searchBtn.addActionListener(_ -> {
            String keyword = searchField.getText();
            String selected = (String) comboBox.getSelectedItem();
            searchBooksBy(keyword, selected);
        });

        bottomPanel.add(showAll);
        bottomPanel.add(new JLabel("Search:"));
        bottomPanel.add(searchField);
        bottomPanel.add(comboBox);
        bottomPanel.add(searchBtn);

        userFrame.add(bottomPanel, BorderLayout.SOUTH);
        userFrame.setVisible(true);
    }

    private void openAdminDashboard() {
        JFrame adminFrame = new JFrame("Admin Dashboard");
        adminFrame.setSize(600, 400);
        adminFrame.setLayout(new BorderLayout());
        adminOutputArea = new JTextArea();
        adminOutputArea.setEditable(false);
        adminFrame.add(new JScrollPane(adminOutputArea), BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel();
        JButton addBook = new JButton("Add Book");
        JButton showAll = new JButton("Show All Books");
        Font Buttonfont = new Font("Arial", Font.PLAIN, 14);
        showAll.setFont(Buttonfont);
        JButton updateStock = new JButton("Update Stock");
        JButton removeBook = new JButton("Remove Book");
        JButton viewUsers = new JButton("View All Users");

        addBook.addActionListener(_ -> addBook());
        showAll.addActionListener(_ -> showAllBooks(userOutputArea));
        updateStock.addActionListener(_ -> updateStock());
        removeBook.addActionListener(_ -> removeBook());
        viewUsers.addActionListener(_ -> showAllUsers());

        bottomPanel.add(addBook);
        bottomPanel.add(showAll);
        bottomPanel.add(updateStock);
        bottomPanel.add(removeBook);
        bottomPanel.add(viewUsers);

        adminFrame.add(bottomPanel, BorderLayout.SOUTH);
        adminFrame.setVisible(true);
    }

    
    private void showAllBooks(JTextArea target) {
        target.setText("All Books:\n");
        for (Book b : BookStore.bookCatalog.values()) {
            target.append(b.toString() + "\n");
    }
}

        

    private void searchBooksBy(String keyword, String field) {
        userOutputArea.setText("Search Results:\n");
        for (Book b : BookStore.bookCatalog.values()) {
            boolean match = false;
            switch (field) {
                case "Title": match = b.getTitle().toLowerCase().contains(keyword.toLowerCase()); break;
                case "Author": match = b.getAuthor().toLowerCase().contains(keyword.toLowerCase()); break;
                case "Genre": match = b.getGenre().toLowerCase().contains(keyword.toLowerCase()); break;
                case "Book ID": match = b.getId().equalsIgnoreCase(keyword); break;
            }
            if (match) userOutputArea.append(b.toString() + "\n");
        }
    }

    private void addBook() {
        String id = JOptionPane.showInputDialog("Book ID:");
        String title = JOptionPane.showInputDialog("Title:");
        String author = JOptionPane.showInputDialog("Author:");
        String genre = JOptionPane.showInputDialog("Genre:");
        int stock = Integer.parseInt(JOptionPane.showInputDialog("Stock:").trim());
        BookStore.bookCatalog.put(id, new Book(id, title, author, genre, stock));
        adminOutputArea.setText("Book added successfully!\n");
        showAllBooks(adminOutputArea);
    }

    private void updateStock() {
        String id = JOptionPane.showInputDialog("Enter Book ID to update stock:");
        Book book = BookStore.bookCatalog.get(id);
        if (book != null) {
            int newStock = Integer.parseInt(JOptionPane.showInputDialog("New stock quantity:").trim());
            book.setStock(newStock);
            adminOutputArea.setText("Stock updated!\n");
            showAllBooks(adminOutputArea);

        } else {
            adminOutputArea.setText("Book not found!\n");
        }
    }

    private void removeBook() {
        String id = JOptionPane.showInputDialog("Enter Book ID to remove:");
        if (BookStore.bookCatalog.remove(id) != null) {
            adminOutputArea.setText("Book removed successfully!\n");
        } else {
            adminOutputArea.setText("Book not found!\n");
        }
        showAllBooks(adminOutputArea);
    }

    private void registerNewUser() {
        String id = JOptionPane.showInputDialog("Enter new user ID:");
        if (BookStore.userExists(id)) {
            JOptionPane.showMessageDialog(null, "User ID already exists!");
            return;
        }
        String name = JOptionPane.showInputDialog("Enter user name:");
        String password = JOptionPane.showInputDialog("Set a password:");
        String[] roles = {"Customer", "Admin"};
        String role = (String) JOptionPane.showInputDialog(null, "Select role:", "Role Selection", JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);

        User user = role.equals("Admin") ? new Admin(id, name) : new Customer(id, name);
        BookStore.registerUser(user, password);
        JOptionPane.showMessageDialog(null, "User registered successfully!");
    }

    private void showAllUsers() {
        adminOutputArea.setText("Registered Users:\n");
        for (User user : BookStore.users.values()) {
            adminOutputArea.append(user.getId() + " - " + user.getName() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BookstoreApp::new);
    }
}
