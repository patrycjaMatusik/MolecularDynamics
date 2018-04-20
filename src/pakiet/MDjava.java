package pakiet;

import java.util.Random;

/**
 * Klasa odpowiedzialna za tworzenie ukladow dynamiki molekularnej
 */
public class MDjava {

    /**
     * liczba atomow w symulacji
     */
    private int nAtoms;
    /**
     * szerokosc / wysokosc pojemnika, w ktorym moga poruszac sie czasteczki
     */
    private int boxWidth;
    private int stepCounter;

    /**
     * tablice chwilowych polozen, predkosci oraz przyspieszen wszystkich czastek w ukladzie
     */
    private double[] x, y, vx, vy, ax, ay;

    /**
     * minimalna odleglosc miedzy czasteczkami, dla ktorej zanikaja oddzialywania
     */
    private final double rCut = 16.0;
    /**
     * sztywnosc sciany (wspolczynnik sprezystosci)
     */
    private final double wallStiffness = 50;
    /**
     * chwilowa wartosc energii potencjalnej
     */
    private double ePot;
    /**
     * chwilowa wartosc energii kinetycznej
     */
    private double eKin;
    /**
     * chwilowa wartosc energii sprezystosci scianki
     */
    private double elasticEnergy;
    /**
     * minimalny dystans, jaki musza zachowac miedzy soba czasteczki
     */
    private double minDistance = 3;

    public double[] getX() {
        return x;
    }

    public double[] getY() {
        return y;
    }

    public double getePot() {
        return ePot;
    }

    public double geteKin() {
        return eKin;
    }

    public double geteTotal() {
        return (ePot+eKin+elasticEnergy);
    }

    public int getnAtoms() {
        return nAtoms;
    }

    public double getElasticEnergy() {
        return elasticEnergy;
    }

    /**
     * Konstruktor parametryczny symulatora ruchu czasteczek w dynamice molekularnej
     * @param nAtoms liczba atomow w symulacji
     * @param boxWidth szerokosc / wysokosc obszaru, po ktorym moga sie poruszac atomy
     */
    public MDjava(int nAtoms, int boxWidth) {
        this.nAtoms = nAtoms;
        this.boxWidth = boxWidth;

        x = new double[nAtoms];
        y = new double[nAtoms];
        vx = new double[nAtoms];
        vy = new double[nAtoms];


       generateRandomInitialValues();

        ax = new double[nAtoms];
        ay = new double[nAtoms];

        calculateAcceleration();
        calculateEnergy();

    }
    /**
     * Konstruktor parametryczny symulatora ruchu czasteczek w dynamice molekularnej. Liczba atomow generowana jest losowo.
     * @param boxWidth szerokosc / wysokosc obszaru, po ktorym moga sie poruszac atomy
     */
    public MDjava(int boxWidth) {

        Random random = new Random();

        this.boxWidth = boxWidth;

        nAtoms = random.nextInt(1000);

        x = new double[nAtoms];
        y = new double[nAtoms];
        vx = new double[nAtoms];
        vy = new double[nAtoms];

        generateRandomInitialValues();

        ax = new double[nAtoms];
        ay = new double[nAtoms];

        calculateAcceleration();
        calculateEnergy();

    }


    /**
     * Metoda wykonujaca obliczenia polozen, predkosci, przyspieszen i energii atomow w ukladzie dla jednego kroku calkowania.
     * @param dt dlugosc kroku calkowania
     */
    public void verletStep(double dt) {

            double vxMid[] = new double[nAtoms];
            double vyMid[] = new double[nAtoms];

            for (int i = 0; i < nAtoms ; i++) {
                vxMid[i] = vx[i] + dt * ax[i] / 2;
                vyMid[i] = vy[i] + dt * ay[i] / 2;
                x[i] = x[i] + dt * vxMid[i];
                y[i] = y[i] + dt * vyMid[i];
            }

            calculateAcceleration();

            for (int i = 0; i < nAtoms; i++) {
                vx[i] = vxMid[i] + dt * ax[i] / 2;
                vy[i] = vyMid[i] + dt * ay[i] / 2;
            }

            calculateEnergy();
            if(stepCounter%50 == 0)
            stepCounter++;

        }

    /**
     * Metoda obliczajaca chwilowe przyspieszenia w dwoch wymiarach dla kazdej czasteczki w ukladzie, uwzglednia zderzenia ze sciankami i oblicza chwilowe wartosci energii potencjalnej i sprezystosci ukladu.
     */
    private void calculateAcceleration() {

        for (int i = 0; i < nAtoms; i++) {
            ax[i] = 0;
            ay[i] = 0;
        }

        ePot = 0;
        elasticEnergy = 0;
        for (int i = 0; i < nAtoms - 1; i++) {
            for (int j = i + 1; j < nAtoms; j++) {
                double dx = x[i] - x[j];
                double dy = y[i] - y[j];
                double rij2 = dx * dx + dy * dy;

              //if(rij2<rCut) {
                    double fr2 = 1. / rij2;
                    double fr6 = fr2 * fr2 * fr2;
                    double fr = 48 * fr2 * fr6 * (fr6 - 0.5);

                    ax[i] += fr * dx;
                    ay[i] += fr * dy;

                    ax[j] -= fr * dx;
                    ay[j] -= fr * dy;
                    ePot += 4*fr6*(fr6-1.0);
                //}

            }

        }

        for (int i=0; i<nAtoms;i++){
            double d = 0;
            if (x[i]<0.5){
                d = 0.5 - x[i];
                ax[i] += wallStiffness * d;
                elasticEnergy += 0.5 * wallStiffness * d * d;
            }
            if (x[i]>(boxWidth - 0.5)){
                d = boxWidth - 0.5 - x[i];
                ax[i] += wallStiffness * d;
                elasticEnergy += 0.5 * wallStiffness * d * d;
            }
        }

        for (int i=0; i<nAtoms;i++){
            double d = 0;
            if (y[i]<0.5){
                d = 0.5 - y[i];
                ay[i] += wallStiffness * d;
                elasticEnergy += 0.5 * wallStiffness * d * d;
            }
            if (y[i]>(boxWidth - 0.5)){
                d = boxWidth - 0.5 - y[i];
                ay[i] += wallStiffness * d;
                elasticEnergy += 0.5 * wallStiffness * d * d;
            }
        }


    }

    /**
     * Metoda obliczajaca chwilowa energie kinetyczna ukladu.
     */
    private void calculateEnergy(){

        double v2;
        eKin = 0;
        for (int i = 0; i<nAtoms; i++){

            v2 = vx[i]*vx[i]+vy[i]*vy[i];
            eKin += v2/2;

        }

    }

    /**
     * Metoda generujaca losowe wartosci poczatkowe polozen czastek w ilosci nAtoms tak, aby kazda znajdowala sie
     * w odleglosci nie mniejszej niz minDistance od pozostalych. Poczatkowe predkosci generowane sa losowo w oparciu
     * o rozklad normalny (Gaussa) z odchyleniem standardowym rownym 3.
     */
    private void generateRandomInitialValues(){

        Random random = new Random();

        for (int i = 0; i<nAtoms; i++){
            x[i] = random.nextDouble()*boxWidth;
            y[i] = random.nextDouble()*boxWidth;

            if(i>0) {
                for (int j = 0; j < i; j++) {
                    double dx = x[i] - x[j];
                    double dy = y[i] - y[j];
                    double rij = Math.sqrt(dx * dx + dy * dy);
                    if (rij < minDistance) {
                        i--;
                        break;
                    }
                }
            }

            vx[i] = random.nextGaussian()*3;
            vy[i] = random.nextGaussian()*3;

        }

    }




}
