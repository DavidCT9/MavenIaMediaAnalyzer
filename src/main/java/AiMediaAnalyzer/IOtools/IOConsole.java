package AiMediaAnalyzer.IOtools;

import java.util.Scanner;

/**
 * @author Jafet
 * This class allows the user to get data from the console and validates it
 * It uses a scanner that connects with System.in
 */
public class IOConsole extends IOHandler {

    private Scanner scanner;

    /**
     * The constructor initializes the scanner without user input
     */
    public IOConsole() {
        setScanner(new Scanner(System.in));
    }

    /**
     *
     * @return scanner
     */
    public Scanner getScanner() {
        return scanner;
    }

    /**
     *
     * @param scanner
     */
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void showInfo(String info) {
        System.out.println(info);
    }

    /**
     * This method obtains a validated float from the user
     * @param info that will be shown to ask for the input
     * @param notValidInput text that will be shown when the input is not valid
     * @return float validated
     */
    @Override
    public float getFloat(String info, String notValidInput) {
        boolean validate = false;
        float data = 0;
        do {
            showInfo(info);
            try {
                String userInput = getScanner().nextLine();
                data = Float.parseFloat(userInput);
                validate = true;
            } catch (Exception e) {
                showInfo(notValidInput);
            }
        } while (!validate);
        return data;
    }

    @Override
    public int getInt(String info, String notValidInput) {
        boolean validate = false;
        int data = 0;
        do {
            showInfo(info);
            try {
                String userInput = getScanner().nextLine();
                data = Integer.parseInt(userInput);
                if (data!=2 && data!=1){
                    throw new RuntimeException();
                }
                validate = true;
            } catch (Exception e) {
                showInfo(notValidInput);
            }
        } while (!validate);
        return data;
    }

    @Override
    public String getString(String info, String notValidInput){
        boolean validate=false;
        String data = "";
        do {
            showInfo(info);
            try {
                data = getScanner().nextLine();
                validate = true;
            } catch (Exception e) {
                showInfo(notValidInput);
            }
        } while (!validate);
        return data;
    }
}
