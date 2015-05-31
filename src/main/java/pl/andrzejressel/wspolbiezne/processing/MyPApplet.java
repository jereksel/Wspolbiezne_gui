package pl.andrzejressel.wspolbiezne.processing;

import de.looksgood.ani.Ani;
import processing.core.PApplet;
import processing.core.PFont;
import processing.event.MouseEvent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MyPApplet extends PApplet {

    PFont f;

    float globalX = 0;
    float globalY = 0;

    int fontSize = 16;

    float skala = 1.0f;

    int maxDzialan = 15;
    float height = fontSize + 4;
    float bigHeight = maxDzialan * height;

    int fps = 60;

    String najdluzszyCiagPracownik = "Pracownik 9999 szukam maszyny dodajacej/odejmujacej";

    BufferedReader br;

    ArrayList<Fabryka> fabryki = new ArrayList<>();
    ArrayList<Sklep> sklepy = new ArrayList<>();


    public void setup() {
        frameRate(fps);
        size(600, 500);
        frame.setResizable(true);

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(Main.fileLocation), "UTF8"));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        Ani.setDefaultEasing(Ani.LINEAR);
        Ani.setDefaultTimeMode(Ani.SECONDS);
        Ani.init(this);
    }


    //Czytanie kolejnej linijki z pliku
    void read() {

        try {

            String input;

            if ((input = br.readLine()) != null) {

                System.out.println(input);

                String[] split = input.split(" ");

                //Usuwanie pustych miejsc
                List<String> splitList = new ArrayList<>(Arrays.asList(split));

                splitList = splitList.stream().filter(s -> !s.equals("")).collect(Collectors.toList());

                split = splitList.toArray(new String[splitList.size()]);

                if (split.length == 1) {
                    return;
                }


                if (split[0].equals("Opcje")) {

                    int iloscFabryk = Integer.parseInt(split[1]);
                    int iloscPracownikow = Integer.parseInt(split[2]);
                    int iloscSklepow = Integer.parseInt(split[3]);

                    for (int i = 0; i < iloscFabryk; i++) {
                        fabryki.add(new Fabryka(i, iloscPracownikow));
                    }

                    for (int i = 0; i < iloscSklepow; i++) {
                        sklepy.add(new Sklep(i));
                    }


                }

                switch (split[1]) {

                    case "Pracownik": {

                        int pracownikId = Integer.parseInt(split[2]);
                        int firmaId = Integer.parseInt(split[0]);

                        if (split[3].equals("pobralem")) {

                            Dzialanie dzialanie = new Dzialanie(split[4], split[5], split[6]);

                            Dzialanie dzialanie1 = fabryki.get(firmaId).listaZadan.get(fabryki.get(firmaId).listaZadan.indexOf(dzialanie));

                            fabryki.get(firmaId).listaZadan.remove(dzialanie1);
                            fabryki.get(firmaId).updateCoordinates();

                            fabryki.get(firmaId).pracownicy.get(pracownikId).dzialanie = dzialanie1;
                            fabryki.get(firmaId).pracownicy.get(pracownikId).wypisanieDzialania = true;

                            fabryki.get(firmaId).pracownicy.get(pracownikId).update();


                        } else if (split[3].contains("wykonałem") || split[3].contains("wykonalem")) {

                            Dzialanie dzialanie = fabryki.get(firmaId).pracownicy.get(pracownikId).dzialanie;
                            dzialanie.wynik = split[8];
                            fabryki.get(firmaId).magazyn.add(dzialanie);
                            fabryki.get(firmaId).pracownicy.get(pracownikId).dzialanie = null;
                            fabryki.get(firmaId).pracownicy.get(pracownikId).czynnosc = "czekam";
                            fabryki.get(firmaId).updateCoordinates();

                        } else {

                            String robota = "";

                            for (int i = 3; i < split.length; i++) {
                                robota += split[i] + " ";
                            }

                            fabryki.get(firmaId).pracownicy.get(pracownikId).czynnosc = robota;
                            fabryki.get(firmaId).pracownicy.get(pracownikId).wypisanieDzialania = false;

                        }


                        break;
                    }

                    case "Prezes": {

                        int firmaId = Integer.parseInt(split[0]);


                        switch (split[2]) {
                            case "stworzylem":
                                fabryki.get(firmaId).dzialaniePrezesa(new Dzialanie(split[3], split[4], split[5]));
                                break;
                            case "wstawilem":
                                fabryki.get(firmaId).listaZadan.add(fabryki.get(firmaId).prezesOstatnie);
                                fabryki.get(firmaId).prezesOstatnie = null;
                                fabryki.get(firmaId).updateCoordinates();
                                break;
                            default:
                                System.out.println("Błędne wywolanie: " + Arrays.toString(split));
                                System.exit(1);
                        }

                        break;
                    }

                    case "Klient": {

                        int firmaId = Integer.parseInt(split[0]);

                        fabryki.get(firmaId).magazyn.clear();

                        break;
                    }

                    case "od": {

                        int fabrykaID = Integer.parseInt(split[2]);
                        int sklepID = Integer.parseInt(split[4]);

                        int ETA = Integer.parseInt(split[6]);

                        for (int i = 7; i < split.length; i = i + 5) {
                            Dzialanie dzialanie = new Dzialanie(split[i], split[i + 1], split[i + 2]);
                            Dzialanie dzialanie1 = fabryki.get(fabrykaID).magazyn.remove(fabryki.get(fabrykaID).magazyn.indexOf(dzialanie));
                            dzialanie1.wDrodze(ETA);
                            sklepy.get(sklepID).dzialania.add(dzialanie1);
                            fabryki.get(fabrykaID).updateCoordinates();
                            sklepy.get(sklepID).updateCoordinates();
                        }


                        break;
                    }

                    case "dostarczony": {

                        int sklepID = Integer.parseInt(split[3]);

                        for (int i = 4; i < split.length; i = i + 5) {
                            Dzialanie dzialanie = new Dzialanie(split[i], split[i + 1], split[i + 2]);
                            sklepy.get(sklepID).dzialania.get(sklepy.get(sklepID).dzialania.indexOf(dzialanie)).dojechalo();
                            sklepy.get(sklepID).updateCoordinates();
                        }


                        break;
                    }

                    case "kupilem": {

                        int sklepID = Integer.parseInt(split[3]);

                        Dzialanie dzialanie = new Dzialanie(split[4], split[5], split[6]);

                        sklepy.get(sklepID).dzialania.remove(sklepy.get(sklepID).dzialania.indexOf(dzialanie));
                        sklepy.get(sklepID).updateCoordinates();

                        break;
                    }
                }
            }


        } catch (IOException io) {
            io.printStackTrace();
            println("ERROR");
        }


    }


    public void draw() {

        f = createFont("Arial", fontSize, true);
        textFont(f, fontSize);

        read();

        scale(skala);

        background(255);
        fill(0);

        textAlign(LEFT);

        fabryki.forEach(MyPApplet.Fabryka::draw);
        sklepy.forEach(MyPApplet.Sklep::draw);

    }


    @Override
    public void mouseWheel(MouseEvent event) {
        float e = event.getCount();
        skala -= e * 0.1;

        if (skala <= 0f) {
            skala = 0.1f;
        }

    }

    @Override
    public void mouseDragged() {
        globalX += (mouseX - pmouseX) / skala;
        globalY += (mouseY - pmouseY) / skala;
    }

    @Override
    public void text(String str, float x, float y) {
        super.text(str, x + globalX, y + globalY);
    }

    @Override
    public void keyPressed() {
        if (key == CODED) {
            if (keyCode == UP) {
                globalY += 10;
            } else if (keyCode == DOWN) {
                globalY -= 10;
            } else if (keyCode == RIGHT) {
                globalX -= 10;
            } else if (keyCode == LEFT) {
                globalX += 10;
            }
        }

        if (key == 'a') {
            maxDzialan++;
        } else if (key == 'q') {
            maxDzialan--;
        }

    }

    //Inne klasy

    public class Sklep {

        public ArrayList<Dzialanie> dzialania = new ArrayList<>();
        int id;

        public Sklep(int id) {
            this.id = id;
        }

        public void draw() {

            text("SKLEP:", textWidth(najdluzszyCiagPracownik) + textWidth("Lista Zadań     ") + textWidth("Magazyn                                             ")
                    , bigHeight * id + height);

            dzialania.forEach(MyPApplet.Dzialanie::draw);

        }

        public void updateCoordinates() {

            Collections.sort(dzialania);

            for (int i = 0; i < dzialania.size(); i++) {
                dzialania.get(i).update(textWidth(najdluzszyCiagPracownik) + textWidth("Lista Zadań     ") + textWidth("Magazyn                                             ")
                        , bigHeight * id + height * (i + 2));
            }


        }
    }

    public class Fabryka {

        int id;

        public Dzialanie prezesOstatnie = new Dzialanie();

        public ArrayList<Dzialanie> listaZadan = new ArrayList<>();
        public ArrayList<Dzialanie> magazyn = new ArrayList<>();
        public ArrayList<Pracownik> pracownicy = new ArrayList<>();

        public Fabryka(int id, int iloscPracownikow) {

            this.id = id;

            for (int i = 0; i < iloscPracownikow; i++) {
                Pracownik pracownik = new Pracownik(i);
                pracownik.x = 0;
                pracownik.y = bigHeight * id + (height * (i + 2));
                pracownicy.add(pracownik);
            }


        }

        public void dzialaniePrezesa(Dzialanie dzialanie) {
            prezesOstatnie = dzialanie;
            dzialanie.y = bigHeight * id;
            prezesOstatnie.update(textWidth("Pracownik 99"), bigHeight * id + height);
        }

        public void draw() {

            text("Prezes: ", 0, bigHeight * id + height);

            if (prezesOstatnie != null) {
                prezesOstatnie.draw();
            }

            pracownicy.forEach(MyPApplet.Pracownik::draw);

            text("Lista zadan: ", textWidth(najdluzszyCiagPracownik), bigHeight * id + height);

            listaZadan.forEach(MyPApplet.Dzialanie::draw);

            text("Magazyn: ", textWidth(najdluzszyCiagPracownik) + textWidth("Lista Zadań     "), bigHeight * id + height);

            magazyn.forEach(MyPApplet.Dzialanie::draw);

        }


        public void updateCoordinates() {

            height = (int) (fontSize * 1.5f);
            bigHeight = maxDzialan * height;

            for (int i = 0; i < pracownicy.size(); i++) {
                pracownicy.get(i).update(0, bigHeight * id + height * (i + 2));
            }

            for (int i = 0; i < listaZadan.size(); i++) {
                listaZadan.get(i).update(textWidth(najdluzszyCiagPracownik), bigHeight * id + height * (i + 2));
            }

            Collections.sort(magazyn);

            for (int i = 0; i < magazyn.size(); i++) {
                magazyn.get(i).update(textWidth(najdluzszyCiagPracownik) + textWidth("Lista Zadań     "), bigHeight * id + height * (i + 2));
            }

        }

    }

    public class Pracownik {

        public float x;
        public float y;

        public Dzialanie dzialanie;
        public String czynnosc = "";
        public boolean wypisanieDzialania = false;

        private int id;

        public Pracownik(int id) {
            this.id = id;
        }

        public void draw() {

            if (wypisanieDzialania && dzialanie != null) {
                dzialanie.draw();
            } else {

                if (dzialanie != null) {
                    dzialanie.draw();
                    fill(0, 90);
                }

                text(czynnosc, x + textWidth("Pracownik 99"), y);
                fill(0);
            }

            text(toString(), x, y);

        }

        public void update() {
            update(x, y);
        }

        public void update(float xNew, float yNew) {

            if (dzialanie != null) {
                dzialanie.update(xNew + textWidth("Pracownik 99"), yNew);
            }

            Ani.to(this, 0.5f, "x", xNew);
            Ani.to(this, 0.5f, "y", yNew);
        }

        @Override
        public String toString() {
            return "Pracownik " + Integer.toString(id);
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (other == this) return true;
            if (!(other instanceof Pracownik)) return false;
            Pracownik otherMyClass = (Pracownik) other;

            return otherMyClass.id == id;
        }
    }


    public class Dzialanie implements Comparable<Dzialanie> {

        private String a;
        private String b;
        private String znak;
        private String wynik;

        private Ani Anix;
        private Ani Aniy;

        public float x;
        public float y;

        //Gdy działanie jedzie do sklepu
        private boolean wPodrozy = false;
        private int ETA;

        public Dzialanie() {
            this("", "", "");
        }

        public Dzialanie(String a, String znak, String b) {
            this.a = a;
            this.b = b;
            this.znak = znak;
        }

        public void wDrodze(int ETA) {
            this.ETA = ETA;
            wPodrozy = true;
            Anix = null;
            Aniy = null;
        }

        public void dojechalo() {
            wPodrozy = false;
        }

        public void update(float xNew, float yNew) {

            if (!(wPodrozy)) {
                //Twierdzenie Pitagorasa
                float droga = sqrt(pow((x - xNew), 2.0f) + pow((y - yNew), 2.0f));

                if (x != xNew) {
                    Ani.to(this, 0.001f * droga, "x", xNew);
                }
                if (y != yNew) {
                    Ani.to(this, 0.001f * droga, "y", yNew);
                }


            } else if (Anix == null && Aniy == null) {
                //Magazyn -> Sklep
                Anix = Ani.to(this, ETA, "x", xNew);
                Aniy = Ani.to(this, ETA, "y", yNew);
            }

        }

        public void draw() {
            if (wPodrozy) {
                fill(255, 0, 0);
            }
            text(toString(), x, y);
            fill(0);
        }

        @Override
        public String toString() {
            if (wynik == null) {
                return a + " " + znak + " " + b;
            } else {
                return a + " " + znak + " " + b + " = " + wynik;
            }
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (other == this) return true;
            if (!(other instanceof Dzialanie)) return false;
            Dzialanie otherMyClass = (Dzialanie) other;

            return otherMyClass.a.equals(a) && otherMyClass.b.equals(b) && otherMyClass.znak.equals(znak);
        }

        @Override
        public int compareTo(Dzialanie dzialanie) {

            if (znak.equals(dzialanie.znak)) return 0;

            if (znak.equals("+")) return -1;
            if (dzialanie.znak.equals("+")) return 1;

            if (znak.equals("*")) return 1;
            if (dzialanie.znak.equals("*")) return -1;


            return 0;
        }


    }

}
