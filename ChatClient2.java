import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ChatClient2 extends UnicastRemoteObject implements ChatClientInterface {
    private static final long serialVersionUID = 1L;
    private ChatInterface chatServer;
    private String clientName;
    private JPanel chatPanel;
    private JTextField inputField;
    private JScrollPane scrollPane;

    protected ChatClient2(String name) throws Exception {
        super();
        this.clientName = name;
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        chatServer = (ChatInterface) registry.lookup("ChatService");
        chatServer.registerClient(this);
        createClientGUI();
    }

    private void createClientGUI() {
        JFrame frame = new JFrame("Chat - " + clientName);
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(0xECE5DD));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0x128C7E));
        headerPanel.setPreferredSize(new Dimension(500, 60));
        JLabel headerLabel = new JLabel(clientName + "'s Chat", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        frame.add(headerPanel, BorderLayout.NORTH);

        // Chat Panel
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(0xECE5DD));
        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(new Color(0xFFFFFF));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(new Color(0x34B7F1));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JButton emojiButton = new JButton("😊");
        emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        emojiButton.setFocusPainted(false);
        emojiButton.addActionListener(e -> showEmojiPicker());

        JButton fileButton = new JButton("📎");
        fileButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        fileButton.setFocusPainted(false);
        fileButton.addActionListener(e -> sendFile());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(fileButton);
        buttonPanel.add(emojiButton);
        buttonPanel.add(sendButton);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Action listeners
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        frame.setVisible(true);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            try {
                chatServer.sendMessage(message, clientName);
                inputField.setText("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Files (Images, Videos, Audio)", "jpg", "png", "mp4", "mp3", "wav"));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                chatServer.sendFile(fileData, selectedFile.getName(), clientName);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error sending file: " + e.getMessage());
            }
        }
    }

    @Override
    public void receiveMessage(String message) {
        String content = stripName(message);
        boolean isSent = message.startsWith(clientName + ":");
        addMessageBubble(content, isSent);
    }

    @Override
    public void receiveFile(byte[] fileData, String fileName) {
        addFileBubble(fileData, fileName, fileName.startsWith(clientName + ":"));
    }

    private String stripName(String message) {
        int colonIndex = message.indexOf(":");
        if (colonIndex != -1 && colonIndex < message.length() - 1) {
            return message.substring(colonIndex + 1).trim();
        }
        return message;
    }

    private void addMessageBubble(String message, boolean isSent) {
        JPanel messagePanel = new JPanel(new FlowLayout(
                isSent ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        messagePanel.setOpaque(false);

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setBackground(isSent ? new Color(0xDCF8C6) : new Color(0xFFFFFF));
        messageArea.setForeground(Color.BLACK);
        messageArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true)
        ));
        messagePanel.add(messageArea);
        chatPanel.add(messagePanel);
        chatPanel.add(Box.createVerticalStrut(5));
        chatPanel.revalidate();
        chatPanel.repaint();

        JScrollBar verticalScroll = scrollPane.getVerticalScrollBar();
        verticalScroll.setValue(verticalScroll.getMaximum());
    }

    private void addFileBubble(byte[] fileData, String fileName, boolean isSent) {
        JPanel messagePanel = new JPanel(new FlowLayout(
                isSent ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        messagePanel.setOpaque(false);

        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".png")) {
            ImageIcon imageIcon = new ImageIcon(fileData);
            Image scaledImage = imageIcon.getImage().getScaledInstance(200, -1, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 10, 5, 10),
                    BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true)
            ));
            imageLabel.setBackground(isSent ? new Color(0xDCF8C6) : new Color(0xFFFFFF));
            imageLabel.setOpaque(true);
            messagePanel.add(imageLabel);
        } else {
            JButton fileButton = new JButton("📎 " + stripName(fileName));
            fileButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            fileButton.setBackground(isSent ? new Color(0xDCF8C6) : new Color(0xFFFFFF));
            fileButton.setForeground(Color.BLUE);
            fileButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 10, 5, 10),
                    BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true)
            ));
            fileButton.addActionListener(e -> {
                try {
                    File tempFile = File.createTempFile("chat_", "_" + fileName);
                    Files.write(tempFile.toPath(), fileData);
                    Desktop.getDesktop().open(tempFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error opening file: " + ex.getMessage());
                }
            });
            messagePanel.add(fileButton);
        }

        chatPanel.add(messagePanel);
        chatPanel.add(Box.createVerticalStrut(5));
        chatPanel.revalidate();
        chatPanel.repaint();

        JScrollBar verticalScroll = scrollPane.getVerticalScrollBar();
        verticalScroll.setValue(verticalScroll.getMaximum());
    }

    private void showEmojiPicker() {
        JDialog emojiDialog = new JDialog();
        emojiDialog.setTitle("Pick an Emoji");
        emojiDialog.setModal(true);
        emojiDialog.setLayout(new FlowLayout());
        emojiDialog.setSize(300, 200);

        String[] emojis = {"😊", "😂", "😍", "👍", "👋", "😢", "😡", "🤓", "🎉", "❤️"};
        for (String emoji : emojis) {
            JButton emojiBtn = new JButton(emoji);
            emojiBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            emojiBtn.addActionListener(e -> {
                inputField.setText(inputField.getText() + emoji);
                emojiDialog.dispose();
            });
            emojiDialog.add(emojiBtn);
        }

        emojiDialog.setLocationRelativeTo(inputField);
        emojiDialog.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            String name = JOptionPane.showInputDialog("Enter your name:");
            if (name != null && !name.trim().isEmpty()) {
                new ChatClient2(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}