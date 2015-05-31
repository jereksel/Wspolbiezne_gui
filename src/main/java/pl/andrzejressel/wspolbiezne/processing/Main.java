package pl.andrzejressel.wspolbiezne.processing;

import processing.core.PApplet;

public class Main {

    public static String fileLocation;

    public static void main(String args[]) {

        if (args.length == 0) {
            printUsage();
            System.exit(-1);
        }

        fileLocation = args[0];

        PApplet.main(new String[]{"pl.andrzejressel.wspolbiezne.processing.MyPApplet"});
    }


    private static void printUsage() {
        System.out.println("USAGE: <sciezka do pliku testowego>");
    }
}