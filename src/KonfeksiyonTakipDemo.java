import javax.swing.*;

public class KonfeksiyonTakipDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new KonfeksiyonGUI().setVisible(true);
        });
    }
}