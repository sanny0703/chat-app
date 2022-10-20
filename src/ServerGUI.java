import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ServerGUI extends JFrame implements ActionListener {
    private static final int WIDTH = 450;
    private static final int HEIGHT = 700;
    private static final int X_COORDINATE = 200;
    private static final int Y_COORDINATE = 50;
    private static final Color APP_COLOR = new Color(7, 94, 84);
    private final Box verticalBox = Box.createVerticalBox();
    private final String userName;
    private JPanel msgAreaPanel;
    private JTextField msgInputField;
    private DataOutputStream outputStream;
    private JScrollBar verticalScrollBar;

    public ServerGUI(String userName) {
        this.userName = userName;
        setLayout(null);
        // set the size of the frame
        setSize(WIDTH, HEIGHT);
        // set the location of the frame
        setLocation(X_COORDINATE, Y_COORDINATE);
        setContent();
        setUndecorated(true); // just to remove the default header with close, minimize etc. buttons
        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) throws IOException {
        ServerGUI gui = new ServerGUI("Sanjay");
        gui.startConnection(6001);
    }

    private void setContent() {
        // let's create a header to hold all the necessary icons for our chat
        JPanel header = new JPanel();
        // since we are using null layout, we have to specify bounds for each and every component
        header.setBounds(0, 0, WIDTH, HEIGHT / 10);
        header.setBackground(APP_COLOR);
        header.setLayout(null);
        // add a back button
        JLabel back = createIcon("icons/3.png", 25, 25);
        back.setBounds(5, 15, 25, 25);
        // when the back button is pressed, the chat should be closed
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });
        header.add(back);
        // create a profile icon
        JLabel profile = createIcon("icons/zoro.png", 50, 50);
        profile.setBounds(40, 10, 50, 50);
        header.add(profile);
        // create a label for username
        JLabel name = createText(userName, 110, 15, 100, 18, 18);
        header.add(name);
        // a label for displaying the user is active just below the username
        JLabel activeStatus = createText("Active Now", 110, 35, 100, 18, 14);
        header.add(activeStatus);
        // add a video call icon
        JLabel video = createIcon("icons/video.png", 30, 30);
        video.setBounds(300, 20, 30, 30);
        video.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // since we haven't implemented it yet, just add a frame to display that
                setFeatureNotReady();
            }
        });
        header.add(video);
        // add a normal call icon
        JLabel phone = createIcon("icons/phone.png", 30, 30);
        phone.setBounds(360, 20, 30, 30);
        phone.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setFeatureNotReady();
            }
        });
        header.add(phone);
        add(header);
        // this is the panel which handles the messages we send and receive
        msgAreaPanel = new JPanel();
        JScrollPane scroll = new JScrollPane(msgAreaPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBounds(5, 75, 440, 570);
        scroll.setPreferredSize(new Dimension(440, 570));
        // we can just hide the scrollbar but with scroll function working
        verticalScrollBar = scroll.getVerticalScrollBar();
        verticalScrollBar.setPreferredSize(new Dimension(0, 0));
        verticalScrollBar.setUnitIncrement(10);
        scroll.setVerticalScrollBar(verticalScrollBar);
        add(scroll);

        // this is the input field for sending the message
        msgInputField = new JTextField();
        msgInputField.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        msgInputField.setBounds(5, 650, 310, 40);
        add(msgInputField);
        // send button to send the message which is given via typeMsgField
        JButton sendMsgButton = new JButton("Send");
        sendMsgButton.setBackground(APP_COLOR);
        sendMsgButton.setForeground(Color.WHITE);
        sendMsgButton.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        sendMsgButton.setBounds(320, 650, 125, 40);
        sendMsgButton.addActionListener(this);
        add(sendMsgButton);
    }

    private JLabel createIcon(String resource, int scaleWidth, int scaleHeight) {
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource(resource));
        Image image = icon.getImage().getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_DEFAULT);
        icon.setImage(image);
        return new JLabel(icon);
    }

    private JLabel createText(String text, int xCoordinate, int yCoordinate, int width, int height, int fontSize) {
        JLabel label = new JLabel(text);
        label.setBounds(xCoordinate, yCoordinate, width, height);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SAN_SERIF", Font.BOLD, fontSize));
        return label;
    }

    private void setFeatureNotReady() {
        JFrame detachFrame = new JFrame();
        detachFrame.setLayout(null);
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(60, 50, 90));
        panel.setBounds(0, 0, WIDTH + 50, 300);
        JLabel text = createText("Feature is not added yet", 10, 10, WIDTH, 50, 18);
        panel.add(text);
        detachFrame.add(panel);
        detachFrame.getContentPane().setBackground(Color.WHITE);
        detachFrame.setBounds(X_COORDINATE - 20, 300, WIDTH + 50, 300);
        detachFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String message = msgInputField.getText();
        msgAreaPanel.setLayout(new BorderLayout());
        JPanel rightMsgPanel = new JPanel(new BorderLayout());
        rightMsgPanel.add(createFormattedLabel(message), BorderLayout.LINE_END);
        verticalBox.add(rightMsgPanel);
        verticalBox.add(Box.createVerticalStrut(15)); // to maintain vertical gap between messages
        msgAreaPanel.add(verticalBox, BorderLayout.PAGE_START);
        trueRepaint();
        if (outputStream != null) {
            try {
                outputStream.writeUTF(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        msgInputField.setText("");// once we have sent the message, input field should be empty again
        // scroll to the bottom when a new message is added
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());
    }

    private JPanel createFormattedLabel(String message) {
        JPanel msgPanel = new JPanel();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        // we can use html for formatting,so that we can maintain a proper width
        JLabel formattedLabel = new JLabel("<html><p style=\" width:150px\">" + message + "</p></html>");
        formattedLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formattedLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
        JPanel formattedPanel = new RoundedPanel(25, new Color(37, 100, 200));
        formattedPanel.add(formattedLabel);
        msgPanel.add(formattedPanel);
        // now let's add time stamp at the bottom of each message
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        JLabel timeLabel = new JLabel(formatter.format(calendar.getTime()));
        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.add(timeLabel, BorderLayout.LINE_END);
        msgPanel.add(timePanel);
        return msgPanel;

    }

    public void startConnection(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        Socket socket = serverSocket.accept();
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream((socket.getOutputStream()));
        while (true) {
            String message = inputStream.readUTF();
            msgAreaPanel.setLayout(new BorderLayout());
            JPanel left = new JPanel(new BorderLayout());
            left.add(createFormattedLabel(message), BorderLayout.LINE_START);
            verticalBox.add(left);
            verticalBox.add(Box.createVerticalStrut(15));
            msgAreaPanel.add(verticalBox, BorderLayout.PAGE_START);
            trueRepaint();
            // scroll to new message
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        }

    }

    private void trueRepaint() {
        repaint();
        revalidate();
    }


}
