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
import java.util.Iterator;

public class MyPApplet extends PApplet {

    PFont f;  // Global font variable

    float globalX = 0;
    float globalY = 0;

    int fontSize = 16;

    float skala = 1.0f;

    int maxDzialan = 15;
    float height = fontSize + 4;
    float bigHeight = maxDzialan * height;

    int tempFPS = 0;

    int fps = 60;

    String najdluzszyCiagPracownik = "Pracownik 9999 szukam maszyny dodajacej/odejmujacej";

    BufferedReader br;

    ArrayList<Fabryka> fabryki = new ArrayList<Fabryka>();
    ArrayList<Sklep> sklepy = new ArrayList<Sklep>();


    public void setup() {
        frameRate(fps);
        // size(600, 500, OPENGL);
        size(600, 500);
        frame.setResizable(true);


        fabryki.add(new Fabryka(0, 5));
        fabryki.add(new Fabryka(1, 5));
        fabryki.add(new Fabryka(2, 5));

        sklepy.add(new Sklep(0));
        sklepy.add(new Sklep(1));
        sklepy.add(new Sklep(2));


        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(Main.fileLocation), "UTF8"));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        Ani.init(this);
    }


    //Czytanie kolejnej linijki z pliku
    void read() {

        //    tempFPS++;

        tempFPS = 10000;

        if (tempFPS < fps / 4) {
            return;
        } else {
            tempFPS = 0;
        }

        try {

            String input;

            if ((input = br.readLine()) != null) {

                System.out.println(input);

                String[] split = input.split(" ");

                //Usuwanie pustych miejsc
                ArrayList<String> splitList = new ArrayList<String>(Arrays.asList(split));
                Iterator<String> iterator = splitList.iterator();
                while (iterator.hasNext()) {
                    String s = iterator.next();
                    if (s.equals("")) {
                        iterator.remove();
                    }
                }

                split = new String[splitList.size()];

                split = splitList.toArray(split);

                if (split.length == 1) {
                    return;
                }

                if (split[1].equals("Pracownik")) {

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


                } else if (split[1].equals("Prezes")) {

                    int firmaId = Integer.parseInt(split[0]);


                    if (split[2].equals("stworzylem")) {
                        fabryki.get(firmaId).dzialaniePrezesa(new Dzialanie(split[3], split[4], split[5]));
                    } else if (split[2].equals("wstawilem")) {
                        fabryki.get(firmaId).listaZadan.add(fabryki.get(firmaId).prezesOstatnie);
                        fabryki.get(firmaId).prezesOstatnie = null;
                        fabryki.get(firmaId).updateCoordinates();
                    } else {
                        System.out.println("Błędne wywolanie: " + Arrays.toString(split));
                        System.exit(1);
                    }

                } else if (split[1].equals("Klient")) {

                    int firmaId = Integer.parseInt(split[0]);

                    fabryki.get(firmaId).magazyn.clear();

                } else if (split[1].equals("od")) {

                    int fabrykaID = Integer.parseInt(split[2]);
                    int sklepID = Integer.parseInt(split[4]);

                    for (int i = 5; i < split.length; i = i + 5) {
                        Dzialanie dzialanie = new Dzialanie(split[i], split[i + 1], split[i + 2]);
                        println(dzialanie);
                        Dzialanie dzialanie1 = fabryki.get(fabrykaID).magazyn.remove(fabryki.get(fabrykaID).magazyn.indexOf(dzialanie));
                        sklepy.get(sklepID).dzialania.add(dzialanie1);
                    }


                } else if (split[1].equals("kupilem")) {

                    int sklepID = Integer.parseInt(split[3]);

                    Dzialanie dzialanie = new Dzialanie(split[4], split[5], split[6]);

                    sklepy.get(sklepID).dzialania.remove(sklepy.get(sklepID).dzialania.indexOf(dzialanie));

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

        for (Fabryka fabryka : fabryki) {
            fabryka.updateCoordinates();
            fabryka.draw();
        }

        for (Sklep sklep : sklepy) {
            sklep.updateCoordinates();
            sklep.draw();
        }

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
        globalX += mouseX - pmouseX;
        globalY += mouseY - pmouseY;
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

        public ArrayList<Dzialanie> dzialania = new ArrayList<Dzialanie>();
        int id;

        public Sklep(int id) {
            this.id = id;
        }

        public void draw() {

            text("SKLEP:", textWidth(najdluzszyCiagPracownik) + textWidth("Lista Zadań     ") + textWidth("Magazyn                                             ")
                    , bigHeight * id + height);

            for (Dzialanie dzialanie : dzialania) {
                dzialanie.draw();
            }


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

        public ArrayList<Dzialanie> listaZadan = new ArrayList<Dzialanie>();
        public ArrayList<Dzialanie> magazyn = new ArrayList<Dzialanie>();
        public ArrayList<Pracownik> pracownicy = new ArrayList<Pracownik>();

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

            for (Pracownik pracownik : pracownicy) {
                pracownik.draw();
            }

            text("Lista zadan: ", textWidth(najdluzszyCiagPracownik), bigHeight * id + height);

            for (Dzialanie dzialanie : listaZadan) {
                dzialanie.draw();
            }

            text("Magazyn: ", textWidth(najdluzszyCiagPracownik) + textWidth("Lista Zadań     "), bigHeight * id + height);

            for (Dzialanie dzialanie : magazyn) {
                dzialanie.draw();
            }

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

            Ani.to(this, 1.5f, "x", xNew);
            Ani.to(this, 1.5f, "y", yNew);
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

        public float x;
        public float y;

        public Dzialanie() {
            this("", "", "");
        }

        public Dzialanie(String a, String znak, String b) {
            this.a = a;
            this.b = b;
            this.znak = znak;
        }

        public void update(float xNew, float yNew) {
            Ani.to(this, 1.5f, "x", xNew);
            Ani.to(this, 1.5f, "y", yNew);
        }

        public void draw() {
            text(toString(), x, y);
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
