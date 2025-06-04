package main;

import java.util.Scanner;
import model.Player;

public class UserInterface {
    private Scanner scanner;
    
    public UserInterface() {
        this.scanner = new Scanner(System.in);
    }
    
    public void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
    
    public void displayTitle() {
        System.out.println("╔═══════════════════════════════════════════════╗");
        System.out.println("║                   monOVoly                    ║");
        System.out.println("╚═══════════════════════════════════════════════╝");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
    }
    
    public int getTitleScreenChoice() {
        int choice = 1;
        boolean selected = false;
        
        while (!selected) {
            System.out.print("Use W/S to navigate, Enter to select: ");
            String input = getInput().toUpperCase();
            
            if (input.equals("W") && choice > 1) {
                choice--;
            } else if (input.equals("S") && choice < 3) {
                choice++;
            } else if (input.equals("")) {  // Enter key
                selected = true;
            } else if (!input.equals("W") && !input.equals("S") && !input.equals("")) {
                System.out.println("Invalid input. Please use W, S, or Enter.");
            }
            
            if (!selected) {
                clearScreen();
                displayTitle();
                
                // Highlight current choice
                if (choice == 1) {
                    System.out.println("> 1. Login <");
                    System.out.println("2. Register");
                    System.out.println("3. Exit");
                } else if (choice == 2) {
                    System.out.println("1. Login");
                    System.out.println("> 2. Register <");
                    System.out.println("3. Exit");
                } else {
                    System.out.println("1. Login");
                    System.out.println("2. Register");
                    System.out.println("> 3. Exit <");
                }
            }
        }
        
        return choice;
    }
    
    public String getInput() {
        return scanner.nextLine();
    }
    
    public void displayMap(char[][] map) {
        for (char[] row : map) {
            for (char cell : row) {
                System.out.print(cell);
            }
            System.out.println();
        }
    }
    
    public void displayPlayerInfo(Player player) {
        System.out.println("╔═══════════════════════════════════════════════╗");
        System.out.printf("║ Username: %-35s ║\n", player.getUsername());
        System.out.printf("║ Level: %-38d ║\n", player.getLevel());
        System.out.printf("║ Gems: %-39d ║\n", player.getGems());
        System.out.printf("║ Energy: %d/%-36d ║\n", player.getCurrentEnergy(), player.getMaxEnergy());
        System.out.println("╚═══════════════════════════════════════════════╝");
    }
}