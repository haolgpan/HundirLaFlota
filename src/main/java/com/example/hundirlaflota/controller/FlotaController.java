package com.example.hundirlaflota.controller;

import com.example.hundirlaflota.FlotaApp;
import com.example.hundirlaflota.net.DatagramSocketClient;
import com.example.hundirlaflota.net.DatagramSocketServer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Pair;


import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FlotaController implements Initializable {
    @FXML
    private Label lblResponse;
    @FXML
    private Label namePlayer;
    @FXML
    TextField txtNum;
    private String nom;
    @FXML
    GridPane gridPlayer;
    @FXML
    GridPane gridEnemy;

    @FXML
    Circle circleServer;
    @FXML
    Circle circleClient;
    @FXML
    private Button ButtonPlayer;
    @FXML
    private Label counterPush;
    @FXML
    private Text counterPush2;
    List<Button> botones;
    private int pulsaciones = 0;

    private int pulsacionesEnemy = 0;
    private int contadorBarcos = 0, barcos = 9;

    private Button botonBlanco;
    String resp = "";

    //boolean turnoPar=false;
    String posicionBarcos = "";


    // Obtener los botones del GridPane
    @FXML
    private void activarBotones(ActionEvent event) {
        setBotonesDisable(gridEnemy, false);
    }

    @FXML
    private void desactivarBotones(ActionEvent event) {
        setBotonesDisable(gridEnemy, true);
    }

    @FXML
    private void desactivarBotonesPlayer(ActionEvent event) {
        setBotonesDisable(gridPlayer, true);
    }

    @FXML
    private void desactivarBotonesColor(ActionEvent event) {
        List<Button> botones = gridEnemy.getChildren().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getStyle().matches("-fx-background-color: (black|red|deepskyblue)"))
                .collect(Collectors.toList());

        botones.forEach(button -> button.setDisable(true));
    }

    private void setBotonesDisable(GridPane gridPane, boolean disable) {
        List<Button> botones = gridPane.getChildren().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .collect(Collectors.toList());

        botones.forEach(button -> button.setDisable(disable));
    }

    //Reflejar Jugada
    @FXML
    private void reflejarJugada(String jugada, ActionEvent event) {
        // Obtener los botones del GridPane
        if (!jugada.equals("consultaTurno") && !jugada.contains("envio")) {
            String[] jugadaSplit = jugada.split(" ");
            String jugadaEnemy = jugadaSplit[1].replace("boton", "botonplayer");
            Button miBoton = (Button) gridPlayer.lookup("#" + jugadaEnemy);
            if (miBoton.getStyle().equals("-fx-background-color: black")) {
                miBoton.setStyle("-fx-background-color: red");
            } else {
                miBoton.setStyle("-fx-background-color: deepskyblue");
            }
        }
    }

    DatagramSocketClient client = new DatagramSocketClient() {
        @Override
        public int getResponse(byte[] data, int length) {
            resp = new String(data, 0, length);
            String response = new String(data, 0, length);
            if (response.contains("turno par")) {
                turnoPar = true;
                Platform.runLater(() -> lblResponse.setText(response));

            } else if (response.equals("blanco")) {
                botonBlanco.setStyle("-fx-background-color: red");
            } else if (response.matches("[^a-zA-Z]+")) {

                int numTurno = Integer.parseInt(response);

                if (numTurno % 2 == 0 && turnoPar) {
                    System.out.println("Turno par numero" + numTurno);
                    turno = true;
                    stopClientTorn();
                    Platform.runLater(() -> {
                        activarBotones(new ActionEvent());
                        desactivarBotonesColor(new ActionEvent());
                    });

                } else if (numTurno % 2 != 0 && !turnoPar) {
                    turno = true;
                    stopClientTorn();
                    System.out.println("Turno impar numero" + numTurno);
                    Platform.runLater(() -> {
                        activarBotones(new ActionEvent());
                        desactivarBotonesColor(new ActionEvent());
                    });
                }
            } else {
                Platform.runLater(() -> lblResponse.setText(response));
                reflejarJugada(response, new ActionEvent());
            }
            return length;
        }

        @Override
        public byte[] getRequest() {
            return String.valueOf(pulsacionesEnemy).getBytes();
        }
        @Override
        public boolean mustContinue(byte[] data) {

            return !resp.equals("Correcte");
        }
    };

    @FXML
    protected void handleEnemyButtonAction(ActionEvent event) {
        Button button = (Button) event.getSource(); // obtiene el botón que ha generado el evento
        button.setStyle("-fx-background-color: deepskyblue");
        String numBoton = button.getId(); // obtiene el número del botón a partir del ID
        botonBlanco = button;
        // Desactivar los botones
        desactivarBotones(event);
        pulsacionesEnemy++;
        counterPush2.setText(String.valueOf(pulsacionesEnemy));

        try {
            String message = nom + " " + numBoton; // crea un mensaje con el valor actualizado del contador

            client.send(message.getBytes()); // envía el mensaje al servidor

            client.runClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handlePlayerButtonAction(ActionEvent event) throws IOException {
        Button button = (Button) event.getSource(); // obtiene el botón que ha generado el evento
        if (button.getId().contains("player")) {
            posicionBarcos += button.getId() + ",";
            --barcos;
            if(barcos == 6 || barcos == 2 || barcos == 0) ++ contadorBarcos;
            button.setStyle("-fx-background-color: black");
            if (contadorBarcos == 3) {
                desactivarBotonesPlayer(new ActionEvent());
                posicionBarcos = posicionBarcos.substring(0, posicionBarcos.length() - 1);
                activarBotones(new ActionEvent());
                client.send(posicionBarcos.getBytes());
            }
        }
    }


    @FXML
    public void menuItemConnection(ActionEvent actionEvent) {

        Dialog<Pair<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Client configuration");
        dialog.setHeaderText("Dades per la connexió al servidor");
        ButtonType conButton = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(conButton, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField txtName = new TextField("player1");
        TextField txtIp = new TextField("127.0.0.1");
        TextField txtPort = new TextField("5555");

        gridPane.add(new Label("Nom:"), 0, 0);
        gridPane.add(txtName, 1, 0);
        gridPane.add(new Label("IP:"), 0, 1);
        gridPane.add(txtIp, 1, 1);
        gridPane.add(new Label("Port:"), 0, 2);
        gridPane.add(txtPort, 1, 2);

        dialog.getDialogPane().setContent(gridPane);
        Platform.runLater(txtName::requestFocus);
        dialog.setResultConverter(dButton -> {
            if (dButton == conButton) {
                nom = txtName.getText();
                posicionBarcos += nom + " ";
                return new Pair<>(txtIp.getText(), Integer.parseInt(txtPort.getText()));
            }
            return null;
        });

        Optional<Pair<String, Integer>> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                client.init(result.get().getKey(), result.get().getValue());
                Thread.sleep(500);
                circleClient.setFill(Color.BLUE);
                lblResponse.setText("connectat com " + nom + ". Pulsa un boton para turno");
                namePlayer.setText(nom);

            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void menuItemActiveServer(ActionEvent actionEvent) {
        showConfigServer();
        circleServer.setFill(Color.BLUE);

    }

    public void showConfigServer() {
        TextInputDialog dialog = new TextInputDialog("5555");
        dialog.setTitle("Config Server");
        dialog.setHeaderText("Activació del servidor local");
        dialog.setContentText("Port");
        dialog.setGraphic(new ImageView(FlotaApp.class.getResource("images/server.png").toString()));
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            DatagramSocketServer server = new DatagramSocketServer();
            Thread thServer = new Thread(() -> {
                try {
                    server.init(Integer.parseInt(result.get()));
                    server.runServer();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thServer.start();
        }

    }

    public void clickClose(ActionEvent actionEvent) {
        System.exit(0);
    }

    @FXML
    protected void handleButtonAction(ActionEvent event) {
        pulsaciones++;

        counterPush.setText(String.valueOf(pulsaciones));
    }

    public void initialize() {
        desactivarBotonesPlayer(new ActionEvent());
        counterPush.setText(String.valueOf(pulsaciones));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        desactivarBotones(new ActionEvent());
    }
}

