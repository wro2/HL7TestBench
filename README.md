# HL7 Test Bench

A Java desktop application for testing HL7 v2 message transmission. Designed for Healthcare IT professionals to send HL7 messages to server endpoints using MLLP (TCP) or HTTP/HTTPS protocols.

## Features

- **Dual Transport Modes**: Send messages via MLLP (TCP) or HTTP/HTTPS
- **TLS/SSL Support**: Secure connections with custom keystore configuration
- **Server Configuration Management**: Save and load frequently used server configurations
- **Batch Message Support**: Load multiple messages from a file and send individually or all at once
- **Message Editing**: Modify messages before sending
- **Transport History**: View send history with color-coded status (green=success, red=error)
- **Response Viewer**: Inspect raw ACK/NAK responses from the server

## Requirements

- **Java 17 or higher** (JDK required for building, JRE sufficient for running)

### Installing Java

**Windows:**
```
winget install EclipseAdoptium.Temurin.21.JDK
```
Or download from: https://adoptium.net/temurin/releases/

**Ubuntu/Debian:**
```bash
sudo apt update && sudo apt install openjdk-21-jdk
```

**macOS:**
```bash
brew install openjdk@21
```

Verify installation:
```bash
java -version
javac -version
```

## Building

### Windows

Open Command Prompt in the project directory and run:
```
build.bat
```

### Linux/macOS

Open Terminal in the project directory and run:
```bash
chmod +x build.sh
./build.sh
```

The build script will:
1. Compile all Java source files
2. Create `out/jar/HL7TestBench.jar`

## Running the Application

After building, run:

```bash
java -jar out/jar/HL7TestBench.jar
```

Or on Windows, you can double-click the JAR file if Java is properly associated.

## Usage Guide

### 1. Configure Server Connection

The **Server Connection** panel at the top allows you to configure where to send messages:

1. **Saved Servers**: Select a previously saved configuration from the dropdown
2. **Mode**: Choose between:
   - `MLLP (TCP)`: Traditional HL7 transport using Minimal Lower Layer Protocol
   - `HTTP/HTTPS`: RESTful transport over HTTP
3. **Connection Details**:
   - For MLLP: Enter Host/IP and Port (default: localhost:2575)
   - For HTTP: Enter the full URL (e.g., `http://localhost:8080/hl7`)
4. **Timeout**: Connection timeout in milliseconds (default: 10000ms)
5. **TLS/SSL**: Enable for secure connections; optionally specify a keystore file

**Saving a Server Configuration:**
1. Configure your connection settings
2. Click "Save Server"
3. Enter a name for this configuration
4. The configuration will appear in the dropdown for future use

### 2. Load or Enter Messages

The **HL7 Message** panel provides two ways to input messages:

**Option A - Paste directly:**
1. Copy an HL7 message from another source
2. Paste it into the text area on the left
3. Click "Send Message" to send

**Option B - Load from file:**
1. Click "Load File..."
2. Select a `.hl7` or `.txt` file containing one or more HL7 messages
3. Messages are automatically parsed and listed in the table on the right
4. Click on a message in the table to load it into the editor
5. Click "Send Message" to send the current message, or "Send All" to send all loaded messages

### 3. View Results

The **Send History** panel shows results of all send operations:

- **Time**: When the message was sent
- **Control ID**: The MSH-10 message control ID
- **Transport**: MLLP or HTTP
- **Status**: ACK status (AA=accepted, AE=error, AR=rejected)
- **RTT**: Round-trip time in milliseconds

Click on any row to view the full server response in the **Server Response** area below.

**Color Coding:**
- Green: Successful (ACK AA)
- Orange: Application Error (ACK AE/AR)
- Red: Connection Error or Timeout

## Sample HL7 Message

Here's a sample ADT^A01 message for testing:

```
MSH|^~\&|SendingApp|SendingFac|ReceivingApp|ReceivingFac|20231215120000||ADT^A01|MSG00001|P|2.5
EVN|A01|20231215120000
PID|1||12345^^^Hospital^MR||Doe^John^A||19800101|M|||123 Main St^^Anytown^CA^12345
PV1|1|I|ICU^101^A|E|||1234^Smith^Jane^M^Dr|||MED||||1|||1234^Smith^Jane^M^Dr|IN||||||||||||||||||||||||||20231215120000
```

## File Format for Batch Messages

When loading a file with multiple messages, each message should start with `MSH|`. The parser automatically splits messages based on the MSH segment. Messages can be separated by blank lines or run together.

Example file with two messages:
```
MSH|^~\&|App1|Fac1|App2|Fac2|20231215120000||ADT^A01|MSG001|P|2.5
PID|1||12345^^^Hosp^MR||Doe^John||19800101|M

MSH|^~\&|App1|Fac1|App2|Fac2|20231215120001||ADT^A08|MSG002|P|2.5
PID|1||12345^^^Hosp^MR||Doe^John||19800101|M
```

## Configuration Storage

Server configurations are saved to:
- **Windows**: `%USERPROFILE%\.hl7testbench\servers.cfg`
- **Linux/Mac**: `~/.hl7testbench/servers.cfg`

## Troubleshooting

**"Connection refused" error:**
- Verify the server is running and accepting connections
- Check the host and port are correct
- Ensure no firewall is blocking the connection

**"Timeout" error:**
- The server may be slow to respond; try increasing the timeout value
- Check network connectivity to the server

**"Invalid HL7" warning:**
- Ensure your message starts with `MSH|`
- Check that the message is properly formatted

**TLS/SSL connection issues:**
- Verify the keystore file path and password are correct
- Ensure the keystore contains the appropriate certificates
- For self-signed certificates, you may need to import the server's certificate

## Project Structure

```
src/main/java/com/hl7testbench/
├── HL7TestBench.java         # Application entry point
├── controller/               # MVC Controllers
│   ├── MainController.java
│   └── TransportWorker.java
├── model/                    # Data models
│   ├── ConnectionConfig.java
│   ├── HL7Message.java
│   ├── SavedServer.java
│   └── TransportResult.java
├── observer/                 # Observer pattern
│   ├── TransportObserver.java
│   └── TransportSubject.java
├── transport/                # Transport strategies
│   ├── TransportStrategy.java
│   ├── MllpTransport.java
│   ├── HttpTransport.java
│   └── TransportFactory.java
├── util/                     # Utilities
│   ├── HL7Parser.java
│   └── UIConstants.java
└── view/                     # Swing UI components
    ├── MainFrame.java
    ├── ConnectionPanel.java
    ├── MessagePanel.java
    ├── HistoryPanel.java
    └── StatusBar.java
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
