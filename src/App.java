
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private boolean isSaveButtonEnabled = false;
    private boolean isTextModified = false;
    
    public TextEditorFrame() {
        renderWindow();
        addComponentsToFrame();
        configureComponents();
        addEventListeners();
        configureSidebar();
        configureTextArea();

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
        save_button.setEnabled(false);
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
        text_area.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleTextChange();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                handleTextChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleTextChange();
            }
        });
    }

    private void handleSaveButton(File file, String file_name) {
        save_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isTextModified && isSaveButtonEnabled) {
                    try {
                        saveContent(file, file_name);
                        isTextModified = false;
                        isSaveButtonEnabled = false;
                    } catch(IOException err) {
                        System.err.println(err.getMessage());
                    }
                }
            }
        });
    }

    private void showFileMenu() {
        popupMenu.add(openFolderMenuItem);
        popupMenu.add(openFileMenuItem);
        popupMenu.show(file_button, 0, file_button.getHeight());
    }

    private void showOpenFolderDialog() {
        File folder = fileHandler.openFolderDialog(this);
        JList<String> filesFromFolder = fileHandler.getFilesFromFolder(folder);
        
        sidebarPanel.removeAll();
        sidebarPanel.add(new JScrollPane(filesFromFolder));
        sidebarPanel.revalidate();
        sidebarPanel.repaint();

        filesFromFolder.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) { 
                try {
                    File file = fileHandler.openFileFromFolder(folder, filesFromFolder.getSelectedValue());
                    String content = fileHandler.getContent(file);
                    fileHandler.loadContent(file, content, text_area);
                    handleSaveButton(file, content);
                } catch (IOException err) {
                    System.err.println(err.getMessage());
                }
            }
        });
    }


    private void showOpenFileDialog() {
        File file = fileHandler.openFileDialog(this);
        if (file != null) {
            try {
                String content = fileHandler.getContent(file);
                fileHandler.loadContent(file, content, text_area);
                handleSaveButton(file, content);
            } catch(IOException err) {
                System.err.println(err.getMessage());
            }
        }
    }

    private void showSaveAsDialog(String content) {
        if (isTextModified) {
            try {
                fileHandler.saveAsDialog(this, content);
            } catch (IOException err) {
                System.err.println(err.getMessage());
            }
        }
    }

    private void saveContent(File file, String file_name) throws IOException {
        try {
            fileHandler.save(file, text_area.getText());
        } catch (FileNotFoundException err) {
            System.err.println(err.getMessage());
        }
    }

    private void handleTextChange() {
        isTextModified = true;
        isSaveButtonEnabled = true;
        save_button.setEnabled(isSaveButtonEnabled);
    }

    private void addEventListeners() {
        file_button.addActionListener(event -> showFileMenu());
        openFileMenuItem.addActionListener(event -> showOpenFileDialog());
        openFolderMenuItem.addActionListener(event -> showOpenFolderDialog());
        save_as_button.addActionListener(event -> showSaveAsDialog(text_area.getText()));
    }

}

class FileHandler {

    public File openFolderDialog(Component component) {
        File folder = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(component);

        if (result == JFileChooser.APPROVE_OPTION) {
            folder = fileChooser.getSelectedFile();
            return folder;
        }
        
        return folder;
    }

    public JList<String> getFilesFromFolder(File selectedFolder) {
        JList<String> fileList = null;

        try {
            if (selectedFolder == null) {
                throw new FileNotFoundException("FOLDER NOT FOUND.");
            }

            File[] files = selectedFolder.listFiles();

            if (files != null) {
                DefaultListModel<String> listModel = new DefaultListModel<>();

               System.out.println("FOLDER OPENED: "); 
                for (File file : files) {
                    listModel.addElement(file.getName());
                    System.out.println("- " + file.getName());
                }

                fileList = new JList<>(listModel);

                return fileList;
            }
        } catch (FileNotFoundException err) {
            System.err.println(err.getMessage());
        }

        return fileList;
    }

    public File openFileFromFolder(File folder, String file_name) throws IOException {
        File file = new File(folder, file_name);

        if (file.isDirectory()) {
            throw new IOException("SELECTED ITEM IS NOT A VALID FILE TO OPEN: " + file.getAbsolutePath());
        }

        return file;
    }

    public File openFileDialog(Component component) {

        JFileChooser fileChooser = new JFileChooser();
        File selectedFile = null;

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        System.out.println("SELECTING FILE...");
        int result = fileChooser.showOpenDialog(component);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            System.out.println("FILE OPENED: " + selectedFile.getName());
        }

        return selectedFile;
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

    public void loadContent(File file, String content, JTextArea text_area) {
        text_area.setText(content);
        System.out.println("CONTENT LOADED: " + file.getAbsolutePath());
    }

    public void save(File file, String content) throws IOException, FileNotFoundException {
        if (file == null || !file.isFile()) {
            throw new IOException("FILE FAILED TO SAVE: " + file.getAbsolutePath());
        }

        try (
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)
        ) {

            String[] lines = content.split("\n");

            for (String line : lines) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            System.out.println("FILE SAVED SUCCESSFULLY: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    public void saveAsDialog(Component component, String content) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(component);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try (
                FileWriter fileWriter = new FileWriter(selectedFile);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)
            ) {
                if (selectedFile != null) {
                    bufferedWriter.write(content);
                    System.out.println("FILE SUCCESSFULLY SAVED AS: " + selectedFile.getName());
                }
            } catch(IOException err) {
                System.err.println(err.getMessage());
                throw err;
            }
        }
    }
}

public class App {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TextEditorFrame();
        });
    }
}