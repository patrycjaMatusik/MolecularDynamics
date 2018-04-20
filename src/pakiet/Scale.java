package pakiet;

import javafx.scene.layout.Pane;
import javafx.stage.Screen;

/**
 * Singleton skalujacy zadane wartosci.
 */
public class Scale {

    private static volatile Scale instance;
    private double xChanged;
    private double yChanged;
    private double x;
    private double y;
    private double xScale;
    private double yScale;

    /**
     * Prywatny konstruktor domyslny obiektu skalujacego zadane wartosci
     */
    private Scale() {
        xScale = Screen.getPrimary().getVisualBounds().getWidth();
        yScale = Screen.getPrimary().getVisualBounds().getHeight();
        x = 0;
        y = 0;
        xChanged = 0;
        yChanged = 0;
    }

    /**
     * Metoda pozwalajaca "dostac sie" do jedynego obiektu klasy Scale.
     * @return Obiekt - singleton - klasy Scale.
     */
    public static Scale getInstance() {
        if (instance == null) {
            synchronized (Scale.class) {
                if (instance == null) {
                    instance = new Scale();
                }
            }
        }
        return instance;
    }

    /**
     * Metoda skalujaca zadane wartosci
     * @param x zadane x
     * @param y zadane y
     * @param pane pojemnik, wzgledem ktorego maja byc skalowane wartosci
     * @param boxWidth szerokosc pojemnika, w ktorym znajduja sie atomy
     * @return tablica wartosci przeskalowanych - pierwsza wartosc to x, a druga to y
     */
    public double[] getValues(double x, double y, Pane pane, int boxWidth) {
        this.x = x;
        this.y = y;
        xScale = pane.getWidth();
        yScale = pane.getHeight();
        xChanged = (x/boxWidth)*xScale;
        yChanged = ((boxWidth-y)/boxWidth)*yScale;
        double [] out = {instance.xChanged, instance.yChanged};
        return out;
    }
}

