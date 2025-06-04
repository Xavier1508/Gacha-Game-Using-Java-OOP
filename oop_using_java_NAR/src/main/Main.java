package main;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import model.EnergyThread;
import model.Player;

public class Main {
    public static void main(String[] args) {
        MonOVoly game = new MonOVoly();
        game.start();
    }
}

class MonOVoly {
    private manager.AccountManager accountManager;
    private UserInterface ui;
    private manager.GameManager gameManager;
    private model.Player currentPlayer;
    private model.EnergyThread energyThread;
    
    public MonOVoly() {
        this.accountManager = new manager.AccountManager();
        this.ui = new UserInterface();
        this.gameManager = new manager.GameManager();
    }
    
    public void start() {
        displayTitleScreen();
    }
    
    private void displayTitleScreen() {
        boolean exit = false;
        while (!exit) {
            ui.clearScreen();
            ui.displayTitle();
            
            int choice = ui.getTitleScreenChoice();
            
            switch (choice) {
                case 1: // Login
                    handleLogin();
                    break;
                case 2: // Register
                    handleRegister();
                    break;
                case 3: // Exit
                    exit = true;
                    System.out.println("Thank you for playing monOVoly!");
                    break;
            }
        }
    }
    
    private void handleLogin() {
        ui.clearScreen();
        System.out.println("╔═════════════════════════════════╗");
        System.out.println("║              LOGIN              ║");
        System.out.println("╚═════════════════════════════════╝");
        
        while (true) {
            System.out.print("Email [0 to exit]: ");
            String email = ui.getInput().trim();
            
            if (email.equals("0")) {
                return;
            }
            
            System.out.print("Password [0 to exit]: ");
            String password = ui.getInput().trim();
            
            if (password.equals("0")) {
                return;
            }
            
            Player player = accountManager.login(email, password);
            
            if (player != null) {
                System.out.println("\nSuccessfully logged in!");
                System.out.println("Press ENTER to continue...");
                ui.getInput();
                currentPlayer = player;
                startGameScreen();
                return;
            } else {
                System.out.println("\nIncorrect credentials!");
                System.out.println("Press ENTER to continue...");
                ui.getInput();
                ui.clearScreen();
                System.out.println("╔═════════════════════════════════╗");
                System.out.println("║              LOGIN              ║");
                System.out.println("╚═════════════════════════════════╝");
            }
        }
    }
    
    private void handleRegister() {
        ui.clearScreen();
        System.out.println("╔═════════════════════════════════╗");
        System.out.println("║             REGISTER            ║");
        System.out.println("╚═════════════════════════════════╝");
        
        String username = "";
        String email = "";
        String password = "";
        String confirmPassword = "";
        
        // Username validation
        while (true) {
            System.out.print("Username [minimum length 4, 0 to exit]: ");
            username = ui.getInput().trim();
            
            if (username.equals("0")) {
                return;
            }
            
            if (username.length() < 4) {
                System.out.println("Username must be at least 4 characters long!");
                continue;
            }
            
            if (accountManager.isUsernameExist(username)) {
                System.out.println("Username is already taken!");
                continue;
            }
            
            break;
        }
        
        // Email validation
        while (true) {
            System.out.print("Email [contains '@' and '.com', 0 to exit]: ");
            email = ui.getInput().trim();
            
            if (email.equals("0")) {
                return;
            }
            
            if (!email.contains("@") || !email.contains(".com")) {
                System.out.println("Invalid email format! Email must contain '@' and '.com'.");
                continue;
            }
            
            if (accountManager.isEmailExist(email)) {
                System.out.println("Email already registered!");
                continue;
            }
            
            break;
        }
        
        // Password validation
        while (true) {
            System.out.print("Password [minimum length 6, 0 to exit]: ");
            password = ui.getInput().trim();
            
            if (password.equals("0")) {
                return;
            }
            
            if (password.length() < 6) {
                System.out.println("Password must be at least 6 characters long!");
                continue;
            }
            
            break;
        }
        
        // Confirm password
        while (true) {
            System.out.print("Confirm Password [must be same as password, 0 to exit]: ");
            confirmPassword = ui.getInput().trim();
            
            if (confirmPassword.equals("0")) {
                return;
            }
            
            if (!confirmPassword.equals(password)) {
                System.out.println("Password and Confirm Password do not match!");
                continue;
            }
            
            break;
        }
        
        Player newPlayer = accountManager.register(username, email, password);
        
        if (newPlayer != null) {
            System.out.println("\nUser successfully registered!");
            System.out.println("Press ENTER to continue...");
            ui.getInput();
            return;
        } else {
            System.out.println("\nRegistration failed!");
            System.out.println("Press ENTER to continue...");
            ui.getInput();
        }
    }
    
    private void startGameScreen() {
        energyThread = new EnergyThread(currentPlayer);
        energyThread.start();
        
        gameManager.startGame(currentPlayer, ui);
        
        energyThread.stopThread();
    }
}