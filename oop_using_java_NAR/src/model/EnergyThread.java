package model;

public class EnergyThread extends Thread {
    private Player player;
    private volatile boolean running;

    public EnergyThread(Player player) {
        this.player = player;
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(10000);
                player.addEnergy(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stopThread() {
        running = false;
    }
}