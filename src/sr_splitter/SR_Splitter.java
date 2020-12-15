
package sr_splitter;


public class SR_Splitter {

    public static MainFrame mainFrame;
    public static String path = "C:/SR/fullData.csv";
    public static String exportPath = "C:/SR/";
    public static MainController mainController;
    
    
    public static void main(String[] args){
        mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        mainController = new MainController();
    }

}
