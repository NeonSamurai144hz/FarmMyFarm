package code;

import java.io.*;

public class GameSaveManager {
    private static final String SAVE_FILE = "farm_game_save.dat";

    public static void saveGame(GameSaveData saveData) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(saveData);
            System.out.println("Game saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    public static GameSaveData loadGame() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(SAVE_FILE))) {
            return (GameSaveData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Failed to load game: " + e.getMessage());
            return null;
        }
    }
}