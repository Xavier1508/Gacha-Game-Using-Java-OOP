package model;

public class Property {
    private String propertyId; // Menambahkan propertyId
    private String name;
    private int basePrice;
    private int totalValue;
    private String ownerId; // Player ID of the owner, null if unowned
    private int buildingLevel; // 0 to 3 (max level before landmark)
    private boolean hasLandmark; // True if a landmark is built
    private boolean inFestivalMode; // Festival mode status
    private int festivalTurnsLeft; // Turns remaining in festival mode

    // Constructor for an unowned property
    public Property(String propertyId, String name, int basePrice) {
        this.propertyId = propertyId;
        this.name = name;
        this.basePrice = basePrice;
        this.totalValue = basePrice;
        this.ownerId = null;
        this.buildingLevel = 0;
        this.hasLandmark = false;
        this.inFestivalMode = false;
        this.festivalTurnsLeft = 0;
    }

    // Getter for propertyId
    public String getPropertyId() {
        return propertyId;
    }

    // Getters
    public String getName() {
        return name;
    }
    
    public int getBasePrice() {
        return basePrice;
    }

    public int getTotalValue() {
        return totalValue;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public int getBuildingLevel() {
        return buildingLevel;
    }

    public boolean hasLandmark() {
        return hasLandmark;
    }

    public boolean isInFestivalMode() {
        return inFestivalMode;
    }

    public int getFestivalTurnsLeft() {
        return festivalTurnsLeft;
    }

    // Setters
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    // Calculate purchase price (base price + random amount between 500 and 1500 in 100 increments)
    public int calculatePurchasePrice() {
        int randomAmount = 500 + (int)(Math.random() * 11) * 100; // 500 to 1500 in steps of 100
        return basePrice + randomAmount;
    }

    // Calculate construction cost (base 300 + random 200-500 in 100 increments, increases by 1000 per level)
    public int calculateConstructionCost() {
        int randomAmount = 200 + (int)(Math.random() * 4) * 100; // 200 to 500 in steps of 100
        return 300 + randomAmount + (buildingLevel * 1000);
    }

    // Construct a building, update value
    public void constructBuilding() {
        if (buildingLevel < 3 && !hasLandmark) {
            int cost = calculateConstructionCost();
            buildingLevel++;
            totalValue += cost;
        }
    }

    // Build a landmark if at max building level
    public void buildLandmark() {
        if (buildingLevel == 3 && !hasLandmark) {
            hasLandmark = true;
            totalValue += 10000; // Landmark cost as per PDF
        }
    }

    // Calculate toll (30% of value, 1.25x if landmark)
    public int calculateToll() {
        double toll = totalValue * 0.3;
        if (hasLandmark) {
            toll *= 1.25;
        }
        return (int) toll;
    }

    // Calculate overtake cost (2x total value)
    public int calculateOvertakeCost() {
        return totalValue * 2;
    }

    // Activate festival mode for 3 turns
    public void activateFestivalMode() {
        inFestivalMode = true;
        festivalTurnsLeft = 3;
    }

    // Decrease festival turns, deactivate if 0
    public void updateFestivalStatus() {
        if (inFestivalMode) {
            festivalTurnsLeft--;
            if (festivalTurnsLeft <= 0) {
                inFestivalMode = false;
            }
        }
    }

    // Downgrade building level (e.g., for Tornado Disaster)
    public void downgradeBuilding() {
        if (hasLandmark) {
            hasLandmark = false;
            totalValue -= 10000;
        } else if (buildingLevel > 0) {
            buildingLevel--;
            // Reduce value by last construction cost (approximation)
            int lastCost = 300 + 200 + (buildingLevel * 1000); // Minimum random value used
            totalValue -= lastCost;
        } else {
            ownerId = null; // Remove ownership if no buildings
        }
    }
}