package ca.uvic.concurrency.gmmurguia.a1.santaclaus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Elf implements Runnable {

    private static Logger logger = LogManager.getLogger();

    String name;

    private SantaClaus santaClaus;

    private Object santasShop;

    boolean waitForMoreElves = true;

    public Elf(String name, SantaClaus santaClaus) {
        this.name = name;
        this.santaClaus = santaClaus;
        this.santasShop = santaClaus.santasShop;
    }

    @Override
    public void run() {
        synchronized (santasShop) {
            if (santaClaus.isShopFull() || santaClaus.santaHelpingElves) {
                santaClaus.elfLeaves();
                return;
            }
        }

        getInLineForHelp();

        synchronized (santasShop) {
            do {
                try {
                    santasShop.wait();
                } catch (InterruptedException e) {}
            } while (waitForMoreElves);
        }

        santaClaus.getHelp(this);
    }


    private void getInLineForHelp() {
        logger.info(String.format("%s waiting in line", name));
        santaClaus.reportHelpNeeded(this);
    }
}