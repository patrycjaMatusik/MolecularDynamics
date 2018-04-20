package pakiet;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Optional;
import java.util.Random;

/**
 * Klasa odpowiedzialna za obsluge obiektu klasy MDjava i tworzaca layout aplikacji.
 */
public class MDtester {

    private static Scene scene;
    private static XYChart.Series ePot;
    private static XYChart.Series eKin;
    private static XYChart.Series eElastic;
    private static XYChart.Series eTotal;
    private static LineChart<Number, Number> figureEnergy;

    private static AnimationTimer atimer;

    private static Pane drawingPane;
    private static Button btnStart, btnStop;
    private static Label lblNumber;
    private static TextField txtNumber;
    private static Circle[] circles;
    private static final int boxWidth = 100;

    private static boolean running = false;
    private static int counter;

    /**
     * Tworzy scene, jej layout sklada w całosc korzystajac z metod odpowiedzialnych za tworzenie poszczegolnych elementow (przyciskow, wykresu, pola animacji).
     * Okresla sposob dzialania przyciskow. Przycisk Start rozpoczyna animacje ruchu czasteczek i rysowanie wykresow energii, przycisk Stop konczy animacje.
     * @return
     */

    public static Scene getScene() {

        counter = 0;

        VBox vBoxButtons = setInitialFields();
        setFigure();
        VBox vBoxMain = new VBox();
        drawingPane = new Pane();
        drawingPane.setPrefSize(400, 400);
        drawingPane.prefWidthProperty().bind(vBoxMain.widthProperty());


        btnStart.setOnAction(e -> {

            t: try {
                MDjava md;

                if (txtNumber.getText().equals("")) {
                    md = new MDjava(100);

                } else {
                    if(Integer.parseInt(txtNumber.getText())>1000) {
                        Optional<ButtonType> result = showAlert();
                        if (!(result.get()==null)) {
                            break t;
                        }
                    }
                    md = new MDjava(Integer.parseInt(txtNumber.getText()), boxWidth);

                }

                int nAtoms = md.getnAtoms();

                txtNumber.setText(String.valueOf(nAtoms));

                if (!running) {
                    ePot.getData().clear();
                    eKin.getData().clear();
                    eElastic.getData().clear();
                    eTotal.getData().clear();
                    counter = 0;
                    circles = new Circle[nAtoms];
                    for (int i = 0; i < nAtoms; i++) {
                        circles[i] = new Circle(3);
                        circles[i].setFill(Color.color(Math.random(), Math.random(), Math.random()));
                        drawingPane.getChildren().add(circles[i]);
                    }
                    btnStart.setDisable(true);
                    btnStop.setDisable(false);
                    running = true;

                }

                atimer = new AnimationTimer() {
                    private long lastUpdate;

                    @Override
                    public void handle(long now) {

                        if (now - lastUpdate > 50000000) {
                            drawAnimation(md, nAtoms);
                            lastUpdate = now;
                        }
                        counter++;
                        md.verletStep(0.001);
                    }
                };
                atimer.start();
            } catch (NumberFormatException e1) {
                showAlert();
            } catch (NegativeArraySizeException e2) {
                showAlert();
            }
        });

        btnStop.setOnAction(e -> {
            txtNumber.setText("");
            atimer.stop();
            running = false;

            drawingPane.getChildren().clear();
            circles = null;

            btnStart.setDisable(false);
            btnStop.setDisable(true);
        });

        vBoxMain.setPadding(new Insets(20, 20, 20, 20));
        vBoxMain.setSpacing(20);
        vBoxMain.getChildren().addAll(vBoxButtons, drawingPane, figureEnergy);
        scene = new Scene(vBoxMain);

        return scene;
    }

    /**
     * Tworzy przyciski, pola tekstowe i etykiety, ustawia ich parametry i połozenie.
     * @return VBox zawierajacy wszystkie przyciski, pola tekstowe i etykiety interfejsu
     */

    private static VBox setInitialFields() {

        lblNumber = new Label("Number of particles: ");
        txtNumber = new TextField();
        HBox hBox = new HBox();
        HBox hBox2 = new HBox();
        btnStart = new Button("Start");
        btnStart.setPrefWidth(200);
        btnStop = new Button("Stop");
        btnStop.setPrefWidth(200);
        hBox2.setAlignment(Pos.CENTER);
        hBox2.setSpacing(20);
        hBox2.getChildren().addAll(lblNumber, txtNumber);
        hBox.getChildren().addAll(btnStart, btnStop);
        hBox.setSpacing(20);
        hBox.setAlignment(Pos.CENTER);
        VBox vBox = new VBox();
        vBox.setSpacing(20);
        vBox.getChildren().addAll(hBox2, hBox);

        return vBox;
    }

    /**
     * Tworzy serie danych, wykres roznych rodzajow energii, ustawia jego osie i wlaściwosci.
     */

    private static void setFigure() {

        ePot = new XYChart.Series();
        ePot.setName("Ep");
        eKin = new XYChart.Series();
        eKin.setName("Ek");
        eElastic = new XYChart.Series();
        eElastic.setName("Elastic");
        eTotal = new XYChart.Series();
        eTotal.setName("Ec");

        NumberAxis steps = new NumberAxis();
        NumberAxis energy = new NumberAxis();
        steps.setLabel("steps");
        energy.setLabel("energy");


        // wykresy energii
        figureEnergy = new LineChart<Number, Number>(steps, energy);
        figureEnergy.getData().addAll(ePot, eKin, eElastic, eTotal);
        figureEnergy.setCreateSymbols(false);
        figureEnergy.setLegendVisible(true);
        figureEnergy.setLegendSide(Side.TOP);
    }

    /**
     * Dodaje do wykresu energii dane dla kolejnego kroku i przemieszcza atomy z symulacji do kolejnych polozen.
     * @param md obiekt zawierajacy dane symulacji dla pojedynczego kroku
     * @param nAtoms liczba atomow
     */

    private static void drawAnimation(MDjava md, int nAtoms) {
        double[] arrayX = md.getX();
        double[] arrayY = md.getY();

        ePot.getData().add(new XYChart.Data(counter, md.getePot()));
        eKin.getData().add(new XYChart.Data(counter, md.geteKin()));
        eElastic.getData().add(new XYChart.Data(counter, md.getElasticEnergy()));
        eTotal.getData().add(new XYChart.Data(counter, (md.geteTotal())));


        for (int i = 0; i < nAtoms; i++) {
            double[] xy = Scale.getInstance().getValues(arrayX[i], arrayY[i], drawingPane, boxWidth);
            circles[i].relocate(xy[0], xy[1]);
        }
    }

    /**
     * Tworzy okno - komunikat o bledzie.
     * @return przycisk potwierdzajacy przeczytanie komunikatu przez uzytkownika
     */

    private static Optional<ButtonType> showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Insert positive integer values not greater than 1000!", ButtonType.OK);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> optional = alert.showAndWait();
        return optional;
    }


}
