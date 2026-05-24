import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;

public class DanidvasDashboard {

    public static void main(String[] args) {
        // List and select available serial ports
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            System.out.println("No serial ports found. Please connect your DANIDVAS device via USB.");
            return;
        }

        // Automatically choose the first available port (e.g., COM3 or /dev/ttyUSB0)
        SerialPort comPort = ports[0];
        comPort.setBaudRate(115200);

        if (comPort.openPort()) {
            System.out.println("Successfully connected to DANIDVAS on port: " + comPort.getSystemPortName());
        } else {
            System.err.println("Failed to open serial port.");
            return;
        }

        // Continuous stream listening loop
        try (Scanner dataScanner = new Scanner(comPort.getInputStream())) {
            System.out.println("\n--- DANIDVAS MONITORING SYSTEM ACTIVE ---");
            while (dataScanner.hasNextLine()) {
                String inputLine = dataScanner.nextLine();
                
                // Parse and handle the visual alert logging if decibel keyword is found
                if (inputLine.contains("Estimated dB:")) {
                    try {
                        String[] parts = inputLine.split("Estimated dB: ");
                        double dbValue = Double.parseDouble(parts[1].trim());
                        evaluateNoiseLevel(dbValue);
                    } catch (Exception e) {
                        // Suppress parsing errors caused by incomplete serial fragments
                    }
                }
            }
        } finally {
            comPort.closePort();
            System.out.println("Serial port closed safely.");
        }
    }

    /**
     * Translates decibel levels into dashboard alerts mirroring the hardware logic[cite: 57].
     */
    private static void evaluateNoiseLevel(double db) {
        System.out.print("[" + String.format("%.2f", db) + " dB] STATUS: ");
        if (db < 70.0) {
            System.out.println("🔵 [BLUE] Safe/Normal traffic noise."); // [cite: 57]
        } else if (db >= 70.0 && db < 80.0) {
            System.out.println("🟢 [GREEN] Moderate environment noise."); // [cite: 57]
        } else if (db >= 80.0 && db <= 95.0) {
            System.out.println("🟡 [YELLOW] WARNING: High noise / Horn nearby!"); // [cite: 57]
        } else {
            System.out.println("🔴 [RED] CRITICAL ALERT: Siren or extreme emergency detected!"); // [cite: 57]
        }
    }
}