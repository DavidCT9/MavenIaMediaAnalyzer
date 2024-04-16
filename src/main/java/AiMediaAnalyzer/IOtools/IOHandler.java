package AiMediaAnalyzer.IOtools;

public abstract class IOHandler {
    public abstract void showInfo(String info);
    public abstract float getFloat(String info, String notValidInput);
    public abstract int getInt(String info, String notValidInput);

    public abstract String getString(String info, String notValidInput);

}


