package Execute;

import javax.swing.SwingUtilities;

import GUI.*;
import model.*;
import utils.*;

public class Main {
    public static void main(String[] args) {
        // Start simulated Admin's file receiver
        FileTransferManager.startFileReceiver();

        // Then launch GUI
        SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
    }
}
