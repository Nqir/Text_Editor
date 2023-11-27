
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

class TextEditorFrame extends JFrame {

    private enum LABEL {
        SAVE("SAVE"),
        SAVE_AS("SAVE AS"),
        FILE("FILE"),
        OPEN_FOLDER("OPEN FOLDER"),
        OPEN_FILE("OPEN FILE");

        private final String label;

        LABEL(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final JPanel panel = new JPanel();
    private final JToolBar toolbar = new JToolBar();
    private final JPopupMenu popupMenu = new JPopupMenu();
    private final FileHandler fileHandler = new FileHandler();
    private final JPanel sidebarPanel = new JPanel();
    private final JPanel sidebarContainer = new JPanel();
    private final JTextArea text_area = new JTextArea();
    private final JLabel text_editor_title = new JLabel();
    private final JButton file_button = new JButton(LABEL.FILE.getLabel());
    private final JButton save_button = new JButton(LABEL.SAVE.getLabel());
    private final JButton save_as_button = new JButton(LABEL.SAVE_AS.getLabel());
    private final JMenuItem openFolderMenuItem = new JMenuItem(LABEL.OPEN_FOLDER.getLabel());
    private final JMenuItem openFileMenuItem = new JMenuItem(LABEL.OPEN_FILE.getLabel());
    
    private final JLabel folderLabel = new JLabel();
    
    public TextEditorFrame() {
        renderWindow();
        addComponentsToFrame();
        configureComponents();
        addEventListeners();
        configureTextArea();
        configureSidebar();

        text_editor_title.setText("My Text Editor");
        text_editor_title.setHorizontalAlignment(JLabel.CENTER);
    }
    
    private void renderWindow() {
        this.setVisible(true);
        this.setSize(1000, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    private void addComponentsToFrame() {
        this.add(panel);
        panel.setLayout(new BorderLayout());
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(text_editor_title, BorderLayout.SOUTH);
        panel.add(new JScrollPane(text_area), BorderLayout.CENTER);
        toolbar.add(file_button);
        toolbar.add(save_button);
        toolbar.add(save_as_button);
    }

    private void configureComponents() {
        file_button.setFocusPainted(false);
        save_button.setFocusPainted(false);
        save_as_button.setFocusPainted(false);
    }

    private void configureSidebar() {
        sidebarPanel.setBackground(new Color(160, 160, 160));
        sidebarContainer.setLayout(new BoxLayout(sidebarContainer, BoxLayout.Y_AXIS));
        sidebarContainer.setPreferredSize(new Dimension(200, 200));
        sidebarContainer.add(sidebarPanel);
        sidebarContainer.add(folderLabel);
        panel.add(sidebarContainer, BorderLayout.EAST);
    }
    
    private void configureTextArea() {
        text_area.setPreferredSize(new Dimension(500, 500));
        text_area.setMargin(new Insets(10, 10, 10, 10));
        text_area.setBackground(new Color(34, 34, 34));
        text_area.setLineWrap(true);
        text_area.setWrapStyleWord(true);
        text_area.setForeground(new Color(255, 255, 255));
        text_area.setCaretColor(new Color(255, 255, 255));
    }

    private void showFileMenu() {
        popupMenu.add(openFolderMenuItem);
        popupMenu.add(openFileMenuItem);
        popupMenu.show(file_button, 0, file_button.getHeight());
    }

    private void openFolder() {
        fileHandler.openFolder(this, sidebarPanel, text_area);
    }

    private void openFile() {
        File selectedFile = fileHandler.openFile(this);

        if (selectedFile != null) {

            try {
                String content = fileHandler.getContent(selectedFile);
                fileHandler.loadContent(text_area, content, selectedFile.getAbsolutePath());
            } catch(IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void addEventListeners() {
        file_button.addActionListener(event -> showFileMenu());
        openFileMenuItem.addActionListener(event -> openFile());
        openFolderMenuItem.addActionListener(event -> openFolder());
    }
}

class FileHandler {

    public void openFolder(Component component, JPanel panel, JTextArea text_area) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(component);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            File[] files = selectedFolder.listFiles();
            
            if (files != null) {
                DefaultListModel<String> listModel = new DefaultListModel<>();

                for (File file : files) {
                    listModel.addElement(file.getName());
                }

                JList<String> fileList = new JList<>(listModel);

                panel.removeAll();

                panel.add(new JScrollPane(fileList));

                panel.revalidate();
                panel.repaint();

                fileList.addListSelectionListener(event -> {
                    if (!event.getValueIsAdjusting()) {
                        try {
                            openFileFromList(selectedFolder, fileList.getSelectedValue(), text_area);
                        } catch (FileNotFoundException e) {
                            System.err.println(e.getMessage());
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                    }
                });

            }
        }
    }

    public File openFile(Component component) {

        JFileChooser fileChooser = new JFileChooser();
        File selectedFile = null;

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        System.out.println("CHOOSING FILE...");
        int result = fileChooser.showOpenDialog(component);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            System.out.println("FILE " + selectedFile.getName() + " OPENED.");
        }

        return selectedFile;
    }

    private void openFileFromList(File selectedFolder, String file_name, JTextArea text_area) throws FileNotFoundException, IOException {

        try {
            File selectedFile = new File(selectedFolder, file_name);
            String path = selectedFile.getAbsolutePath();

            if (!selectedFile.exists()) {
                throw new FileNotFoundException("FILE NOT FOUND: " + path);
            }

            if (selectedFile.isDirectory()) {
                throw new IOException("SELECTED ITEM IS NOT VALID AS FILE: " + path);
            }

            String content = getContent(selectedFile);
            loadContent(text_area, content, path);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } 
    } 

    public String getContent(File selectedFile) throws IOException {

        StringBuilder content = new StringBuilder();

        try (
            FileReader fileReader = new FileReader(selectedFile); 
            BufferedReader bufferedReader = new BufferedReader(fileReader)
            ) {
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line).append('\n');
                }

            } catch(IOException e) {
                System.err.println(e.getMessage());
                throw e;
            }

        return content.toString();
    }

    public void loadContent(JTextArea text_area, String content, String path) {
        text_area.setText(content);
        System.out.println("CONTENT LOADED: " + path);
    }
}

public class App {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TextEditorFrame();
        });
    }
}