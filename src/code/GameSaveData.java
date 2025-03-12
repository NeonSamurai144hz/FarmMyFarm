package code;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    private int playerMoney;
    private PlayerLevel playerLevel;
    private Map<CropType, Integer> cropStorage;
    private Map<AnimalType, Integer> animalStorage;
    private Map<CropType, Integer> feedStorage;
    private Map<Resource, Integer> resourceStorage;
    private ArrayList<Finance.Transaction> transactions;
    private ParcelData[][] gridData;

    public GameSaveData(int playerMoney,
                        Map<CropType, Integer> cropStorage,
                        Map<AnimalType, Integer> animalStorage,
                        Map<CropType, Integer> feedStorage,
                        Map<Resource, Integer> resourceStorage,
                        ArrayList<Finance.Transaction> transactions,
                        ParcelData[][] gridData,
                        PlayerLevel playerLevel) {
        this.playerMoney = playerMoney;
        this.playerLevel = playerLevel;
        this.cropStorage = new HashMap<>(cropStorage);
        this.animalStorage = new HashMap<>(animalStorage);
        this.feedStorage = new HashMap<>(feedStorage);
        this.resourceStorage = new HashMap<>(resourceStorage);
        this.transactions = new ArrayList<>(transactions);
        this.gridData = gridData;
    }

    // Getters
    public int getPlayerMoney() {
        return playerMoney;
    }

    public PlayerLevel getPlayerLevel() {
        return playerLevel;
    }

    public Map<CropType, Integer> getCropStorage() {
        return cropStorage;
    }

    public Map<AnimalType, Integer> getAnimalStorage() {
        return animalStorage;
    }

    public Map<CropType, Integer> getFeedStorage() {
        return feedStorage;
    }

    public Map<Resource, Integer> getResourceStorage() {
        return resourceStorage;
    }

    public ArrayList<Finance.Transaction> getTransactions() {
        return transactions;
    }

    public ParcelData[][] getGridData() {
        return gridData;
    }
}