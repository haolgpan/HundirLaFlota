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
import java.util.Optional;
import java.util.ResourceBundle;

public class FlotaController implements Initializable {
    @FXML
    private Label lblResponse;
    @FXML
    TextField txtNum;
    private String nom;
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
    private int pulsaciones = 0;

    private int pulsacionesEnemy = 0;
    String resp = "";

   boolean turnoPar=false;



   // ClientTcpLlista clientTcpLlista = new ClientTcpLlista();
    DatagramSocketClient client = new DatagramSocketClient() {
        @Override
        public int getResponse(byte[] data, int length) {
            resp = new String(data,0,length);
            String response = new String(data, 0, length);
            if(response.equals("Esperando respuesta , turno par")) turnoPar=true;

            try {
              int turno= Integer.parseInt( response);
              if(turno%2==0 && turnoPar) client.turno = true;
              else if (turno%2!=0 && !turnoPar)client.turno = true;
            }
            catch (NumberFormatException e){
                System.out.println("No se NumberFormatException ");

            }

            System.out.println("RecibidaRespuesta");
            System.out.println(response);
            Platform.runLater(() -> lblResponse.setText(resp));
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
        pulsacionesEnemy++;
        counterPush2.setText(String.valueOf(pulsacionesEnemy));

        try {
            String message = nom+" " + numBoton; // crea un mensaje con el valor actualizado del contador
            System.out.println("Estado booleano "+client.turno);
            System.out.println("boton numero " + numBoton);
            client.send(message.getBytes()); // envía el mensaje al servidor
            System.out.println("Estado booleano "+client.turno);

            client.runClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
        public void menuItemConnection(ActionEvent actionEvent) {

            Dialog<Pair<String,Integer>> dialog = new Dialog<>();
            dialog.setTitle("Client configuration");
            dialog.setHeaderText("Dades per la connexió al servidor");
            ButtonType conButton = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(conButton,ButtonType.CANCEL);

            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(10);
            gridPane.setPadding(new Insets(20,150,10,10));

            TextField txtName = new TextField("player1");
            TextField txtIp = new TextField("127.0.0.1");
            //txtIp.setPromptText("IP");
            TextField txtPort = new TextField("5555");
           // txtPort.setPromptText("Port");

            gridPane.add(new Label("Nom:"), 0, 0);
            gridPane.add(txtName, 1, 0);
            gridPane.add(new Label("IP:"), 0, 1);
            gridPane.add(txtIp, 1, 1);
            gridPane.add(new Label("Port:"), 0, 2);
            gridPane.add(txtPort, 1, 2);

            dialog.getDialogPane().setContent(gridPane);

            Platform.runLater(txtIp::requestFocus);

            dialog.setResultConverter(dButton -> {
                if(dButton == conButton) {
                    nom = txtName.getText();
                    return new Pair<>(txtIp.getText(),Integer.parseInt(txtPort.getText()));
                }
                return null;
            });

            Optional<Pair<String,Integer>> result = dialog.showAndWait();

            if(result.isPresent()) {
                try {
                    client.init(result.get().getKey(), result.get().getValue());
                    Thread.sleep(500);
                    circleClient.setFill(Color.BLUE);
                    lblResponse.setText("connectat com "+ nom+ ". Pulsa un boton para turno");
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
        if(result.isPresent()) {
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
        counterPush.setText(String.valueOf(pulsaciones));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}

