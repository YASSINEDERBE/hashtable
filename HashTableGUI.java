import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class TableCanvas extends JComponent {
    private ArrayList<String>[] table;
    private String animatingKey;
    private int animatingIndex;
    private int animatingChainPosition;
    private Timer animationTimer;
    private int animationStep;
    private int currentX, currentY;
    private int targetX, targetY;
    private static final int ANIMATION_STEPS = 20;
    private static final int ANIMATION_DELAY = 20;

    public TableCanvas(ArrayList<String>[] table) {
        this.table = table;
        this.animatingKey = null;
        this.animatingIndex = -1;
        this.animatingChainPosition = -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int cellWidth = 100;
        int cellHeight = 20;
        int padding = 5;

        for (int i = 0; i < table.length; i++) {
            int x = padding;
            int y = i * (cellHeight + padding) + padding;

            g2d.drawRect(x, y, cellWidth, cellHeight);
            g2d.drawString(String.valueOf(i), x + padding, y + cellHeight / 2 + padding);

            for (int j = 0; j < table[i].size(); j++) {
                int previousX = x;
                x += cellWidth + padding;
                g2d.drawRect(x, y, cellWidth, cellHeight);
                g2d.drawString(table[i].get(j), x + padding, y + cellHeight / 2 + padding);
                // Draw connecting line
                g2d.drawLine(previousX + cellWidth, y + cellHeight / 2, x, y + cellHeight / 2);
            }
            // Draw end-of-list symbol if the list is not empty
            if (!table[i].isEmpty()) {
                int previousX = x;
                x += cellWidth + padding;
                g2d.drawLine(previousX + cellWidth, y + cellHeight / 2, x, y + cellHeight / 2);
                // Draw vertical line
                g2d.drawLine(x, y + padding, x, y + cellHeight - padding);
                // Draw horizontal dashes
                for (int dashY = y + padding; dashY < y + cellHeight - padding; dashY += 5) {
                    g2d.drawLine(x - 5, dashY, x + 5, dashY);
                }
            }
        }

        if (animatingKey != null) {
            g2d.setColor(Color.RED);
            g2d.drawString(animatingKey, currentX + padding, currentY + cellHeight / 2 + padding);
        }
    }

    public void startAnimation(String key, int index) {
        animatingKey = key;
        animatingIndex = index;
        animatingChainPosition = table[index].size();
        animationStep = 0;

        int cellHeight = 20;
        int padding = 5;
        int cellWidth = 100;

        currentX = padding;
        currentY = -cellHeight;
        targetX = padding + (cellWidth + padding) * (animatingChainPosition + 1);
        targetY = index * (cellHeight + padding) + padding;

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        animationTimer = new Timer(ANIMATION_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (animationStep < ANIMATION_STEPS / 2) {
                    // Move towards index position
                    currentX += (targetX - currentX) / (ANIMATION_STEPS - animationStep + 1);
                    currentY += (targetY - currentY) / (ANIMATION_STEPS - animationStep + 1);
                } else {
                    // Move towards chain position
                    int chainX = padding + (cellWidth + padding) * (animatingChainPosition + 1);
                    currentX += (chainX - currentX) / (ANIMATION_STEPS - animationStep + 1);
                }

                animationStep++;

                if (animationStep >= ANIMATION_STEPS) {
                    animationTimer.stop();
                    table[animatingIndex].add(animatingKey);
                    animatingKey = null;
                    repaint();
                } else {
                    repaint();
                }
            }
        });

        animationTimer.start();
    }

    public void updateTable() {
        repaint();
    }
}

public class HashTableGUI extends JFrame {
    private int tableSize;
    private ArrayList<String>[] table;
    private int count;
    private JTextField inputField;
    private JTextArea displayArea;
    private JLabel statusLabel;
    private TableCanvas tableCanvas;

    public HashTableGUI(int size) {
        this.tableSize = size;
        this.table = new ArrayList[this.tableSize];
        for (int i = 0; i < this.tableSize; i++) {
            this.table[i] = new ArrayList<>();
        }
        this.count = 0;
    }

    private int hashFunction(String key) {
        int hashValue = 0;
        for (int i = 0; i < key.length(); i++) {
            hashValue = (hashValue * 31 + key.charAt(i)) % this.tableSize;
        }
        return hashValue;
    }

    public void add(String key) {
        int index = hashFunction(key);
        if (!table[index].contains(key)) {
            tableCanvas.startAnimation(key, index);
            count++;
            System.out.println("Added successfully: " + key);
        } else {
            System.out.println("Key already exists: " + key);
        }
    }

    public void remove(String key) {
        int index = hashFunction(key);
        ArrayList<String> list = table[index];

        int lastIndex = -1;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).equals(key)) {
                lastIndex = i;
                break;
            }
        }

        if (lastIndex != -1) {
            list.remove(lastIndex);
            count--;
            System.out.println("Removed successfully: " + key);
            tableCanvas.updateTable();
        } else {
            System.out.println("Key not found: " + key);
        }
    }

    public int getHashTableSize() {
        return count;
    }

    public boolean contains(String key) {
        int index = hashFunction(key);
        if (table[index].contains(key)) {
            System.out.println("Key '" + key + "' found");
            return true;
        } else {
            System.out.println("Key '" + key + "' not found");
            return false;
        }
    }

    public String display() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableSize; i++) {
            sb.append(i).append(": ").append(table[i]).append("\n");
        }
        return sb.toString();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(80, 30));
        button.setFocusPainted(false);
        button.setBackground(new Color(59, 89, 182));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Tahoma", Font.BOLD, 12));
        return button;
    }

    public void createAndShowGUI() {
        setTitle("HashTable GUI");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tableCanvas = new TableCanvas(table);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(tableCanvas, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel inputStatusPanel = new JPanel();
        inputStatusPanel.setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(2, 1));
        JLabel inputLabel = new JLabel("Input:");
        inputField = new JTextField();
        inputPanel.add(inputLabel);
        inputPanel.add(inputField);
        inputStatusPanel.add(inputPanel, BorderLayout.NORTH);

        statusLabel = new JLabel("Status: Ready");
        displayArea = new JTextArea(5, 20);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        inputStatusPanel.add(statusLabel, BorderLayout.CENTER);
        inputStatusPanel.add(scrollPane, BorderLayout.SOUTH);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 3, 5, 5)); 
        JButton addButton = createStyledButton("Add");
        JButton removeButton = createStyledButton("Remove");
        JButton containsButton = createStyledButton("Contains");
        JButton sizeButton = createStyledButton("Size");
        JButton displayButton = createStyledButton("Display");
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        //buttonPanel.add(containsButton);
        buttonPanel.add(sizeButton);
        buttonPanel.add(displayButton);

        bottomPanel.add(inputStatusPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String key = inputField.getText().trim();
                if (!key.isEmpty()) {
                    add(key);
                    statusLabel.setText("Added: " + key);
                    inputField.setText("");
                } else {
                    statusLabel.setText("Invalid input");
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String key = inputField.getText().trim();
                if (!key.isEmpty()) {
                    remove(key);
                    statusLabel.setText("Removed: " + key);
                    inputField.setText("");
                } else {
                    statusLabel.setText("Invalid input");
                }
            }
        });

        containsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String key = inputField.getText().trim();
                if (!key.isEmpty()) {
                    boolean result = contains(key);
                    statusLabel.setText("Contains " + key + ": " + result);
                    inputField.setText("");
                } else {
                    statusLabel.setText("Invalid input");
                }
            }
        });

        sizeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int size = getHashTableSize();
                statusLabel.setText("Current size: " + size);
            }
        });

        displayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String displayText = display();
                displayArea.setText(displayText);
            }
        });

        
        setLayout(new BorderLayout());
        add(tablePanel, BorderLayout.CENTER); 
        add(bottomPanel, BorderLayout.SOUTH); 

        setVisible(true);
    }

    public static void main(String[] args) {
        HashTableGUI hashTableGUI = new HashTableGUI(10);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                hashTableGUI.createAndShowGUI();
            }
        });
    }
}
