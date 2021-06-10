package lab8.Client;


import javafx.scene.control.Alert;
import lab8.Commands.SerializedCommands.*;
import lab8.Commands.SpecificCommands.*;
import lab8.Controllers.LoginController;
import lab8.Controllers.RegisterController;
import lab8.Data.Person;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Scanner;

public class Receiver {
    public static LoginController loginController;
    public static RegisterController registerController;

    private final Invoker invoker;
    private final Sender sender;
    private final CommandAsker commandAsker = new CommandAsker(new InputChecker());
    private final HashMap<String, Boolean> inStack = new HashMap<>();

    public Receiver(Invoker invoker, Sender sender) {
        this.invoker = invoker;
        this.sender = sender;
    }

    public static boolean isLogin = false;
    public static String handle = "";
    public static String password = "";
    public static String color = "";

    public void help(){
        StringBuilder response = new StringBuilder();
        invoker.getCommands().forEach((name, command) -> {
            response.append(command.aboutCommand());
        });
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Информация о командах:");
        alert.setContentText(String.valueOf(response));
        alert.showAndWait();
    }

    public void info() throws IOException {
        sender.sendObject(new SerializedArgumentCommand(new InfoCommand(), handle + " " + password));
        byte[] responseBytes = new byte[256];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException | SocketTimeoutException e){
            System.out.println("Problem occurred on the server!");
        }
            /*
            datagramSocket.setSoTimeout(5000);
            datagramSocket.receive(datagramPacket);*/
        String response = new String(responseBytes);
        //response = response.substring(7);
        response = response.trim();
        System.out.println(response);
    }

    public void login(String acc, String pass) throws IOException {

        sender.sendObject(new SerializedArgumentCommand(new LoginCommand(), acc + " " + pass));
        byte[] responseBytes = new byte[256];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
            String response = new String(responseBytes);
            System.out.println(response);
            //response = response.substring(7);
            response = response.trim();
            String[] resultArray = response.split(" ");
            if(resultArray[0].equals("Unsuccessfully")){
                isLogin = false;
                handle = "";
                password = "";
                color = "";
                loginController.warning2();
            } else{
                isLogin = true;
                handle = acc;
                password = pass;
                color = resultArray[1];
                loginController.success();
            }
        } catch(SocketException | SocketTimeoutException e){
            loginController.warning1();
            //loginController.success();
            //System.out.println("Problem occurred on the server!");

        }
    }

    public void register(String login, String pass, String c) throws IOException {
        sender.sendObject(new SerializedArgumentCommand(new RegisterCommand(), login + " " + pass + " " + c));
        byte[] responseBytes = new byte[256];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
            String response = new String(responseBytes);
            response = response.trim();
            System.out.println(response);
            if(response.equals("Unsuccessfully")){
                isLogin = false;
                handle = "";
                password = "";
                color = "";
                registerController.warning2();
            }
            else{
                isLogin = true;
                handle = login;
                password = pass;
                color = c;
                registerController.success();
            }
        } catch(SocketException | SocketTimeoutException e){
            registerController.warning1();
            //System.out.println("Problem occurred on the server!");
        }
    }
    public void show() throws IOException {
        sender.sendObject(new SerializedArgumentCommand(new ShowCommand(), handle + " " + password));
        byte[] responseBytes = new byte[2048];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException | SocketTimeoutException e){
            System.out.println("Problem occurred on the server!");
        }
        /*        datagramSocket.setSoTimeout(5000);
        datagramSocket.receive(datagramPacket);*/
        String response = new String(responseBytes);
        //response = response.substring(7);
        response = response.trim();
        System.out.println(response);
    }

    public void exit(){
        //System.out.println("Client: exit!");
        System.exit(0);
    }

    public void clear() throws IOException {
        sender.sendObject(new SerializedArgumentCommand(new ClearCommand(), handle + " " + password));
        byte[] responseBytes = new byte[256];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException | SocketTimeoutException e){
            System.out.println("Problem occurred on the server!");
        }
        /*        datagramSocket.setSoTimeout(5000);
        datagramSocket.receive(datagramPacket);*/
        String response = new String(responseBytes);
        //response = response.substring(7);
        response = response.trim();
        System.out.println(response);
    }

    public void executeScript(String fileName) throws IOException {
        if(inStack.get(fileName) != null){
            if(inStack.get(fileName)){
                System.out.println("To avoid infinite recursion. File " + fileName + " can't be executed!");
                return;
            }
        }

        inStack.put(fileName, true);

        File scriptFile = new File(fileName);
        Scanner fileScanner;
        try {
            fileScanner = new Scanner(scriptFile);
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("Script file doesn't exist. Please check the file path!");
            return ;
        }

        while(fileScanner.hasNextLine()){
            String[] userCommand = fileScanner.nextLine().trim().split(" ");
            invoker.executeCommand(userCommand);
            System.out.println("----------------------------------------");
        }

        inStack.put(fileName, false);
    }

    public void add() throws IOException {
        Person person = commandAsker.createPerson();
        person.setId(-1);
        person.setOwner(handle);
        sender.sendObject(new SerializedCombinedCommand(new AddCommand(), handle + " " + password, person));
        byte[] responseBytes = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException | SocketTimeoutException e){
            System.out.println("Problem occurred on the server!");
        }
        String response = new String(responseBytes);
        //response = response.substring(7);
        response = response.trim();
        System.out.println(response);
    }

    public void countLessThanBirthDay(String args) throws IOException {
        if(!commandAsker.birthdayValidCheck(args)){
            System.out.println("Birthday is inserted in incorrect format! Please check for validness!");
            return;
        }

        sender.sendObject(new SerializedCombinedCommand(
                new CountLessThanBirthdayCommand(), handle + " " + password, args
        ));
        byte[] responseBytes = new byte[256];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException | SocketTimeoutException e){
            System.out.println("Problem occurred on the server!");
        }
        /*        datagramSocket.setSoTimeout(5000);
        datagramSocket.receive(datagramPacket);*/
        String response = new String(responseBytes);
        //response = response.substring(7);
        response = response.trim();
        System.out.println(response);
    }

    public void groupCountingByID() throws IOException {
        sender.sendObject(new SerializedArgumentCommand(new GroupCountingByIDCommand(), handle + " " + password));
        byte[] responseBytes = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException | SocketTimeoutException e){
            System.out.println("Problem occurred on the server!");
        }
        /*        datagramSocket.setSoTimeout(5000);
        datagramSocket.receive(datagramPacket);*/
        String response = new String(responseBytes);
        //response = response.substring(7);
        response = response.trim();
        System.out.println(response);
    }

    public void printFieldAscendingHeight() throws IOException {
        sender.sendObject(new SerializedArgumentCommand(new PrintFieldAscendingHeightCommand(), handle + " " + password));
        byte[] responseBytes = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException | SocketTimeoutException e){
            System.out.println("Problem occurred on the server!");
        }
        /*        datagramSocket.setSoTimeout(5000);
        datagramSocket.receive(datagramPacket);*/
        String response = new String(responseBytes);
        //response = response.substring(7);
        response = response.trim();
        System.out.println(response);
    }

    public void removeByID(String arg) throws IOException {
        sender.sendObject(new SerializedArgumentCommand(new RemoveByIdCommand(), handle + " " + password + " " + arg));
        byte[] responseBytes = new byte[256];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException | SocketTimeoutException e){
            System.out.println("Problem occurred on the server!");
        }
        String response = new String(responseBytes);
        //response = response.substring(7);
        response = response.trim();
        System.out.println(response);
    }

    public void removeGreater() throws IOException {
        Person person = commandAsker.createPerson();
        person.setOwner(handle);
        sender.sendObject(new SerializedCombinedCommand(new RemoveGreaterCommand(), handle + " " + password, person));
        byte[] responseBytes = new byte[256];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        try {
            sender.getDatagramSocket().setSoTimeout(5000);
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException | SocketTimeoutException e){
            System.out.println("Problem occurred on the server!");
        }
        String response = new String(responseBytes);
        response = response.trim();
        System.out.println(response);
    }

    public void removeLower() throws IOException {
        Person person = commandAsker.createPerson();
        person.setOwner(handle);
        sender.sendObject(new SerializedCombinedCommand(new RemoveLowerCommand(), handle + " " + password, person));
        byte[] responseBytes = new byte[256];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        sender.getDatagramSocket().setSoTimeout(5000);
        try {
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException e){
            System.out.println("Problem occurred on the server!");
        }
        String response = new String(responseBytes);
        response = response.trim();
        System.out.println(response);
    }

    public void update() throws IOException{
        String id = commandAsker.idAsker();
        Person person = commandAsker.createPerson();
        person.setOwner(handle);
        person.setId(Integer.parseInt(id));
        sender.sendObject(new SerializedCombinedCommand(new UpdateCommand(),
                handle + " " + password + " " + id, person));
        byte[] responseBytes = new byte[256];
        DatagramPacket datagramPacket = new DatagramPacket(responseBytes, responseBytes.length);
        sender.getDatagramSocket().setSoTimeout(5000);
        try {
            sender.getDatagramSocket().receive(datagramPacket);
        } catch(SocketException e){
            System.out.println("Problem occurred on the server!");
        }
        String response = new String(responseBytes);
        response = response.trim();
        System.out.println(response);
    }
}
