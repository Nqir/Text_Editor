
import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

public class App extends JFrame {

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
    private String TEXT_EDITOR_TITLE = "My Text Editor";
    private final JPanel panel = new JPanel();
    private final JToolBar toolbar = new JToolBar();
    private final JPopupMenu popupMenu = new JPopupMenu();
    private final FileHandler fileHandler = new FileHandler();
    private final JPanel sidebarPanel = new JPanel();
    private final JPanel sidebarContainer = new JPanel();
    private final JTextArea text_area = new JTextArea();
    private final JLabel text_editor_title = new JLabel();
    private final JButton file_button = new JButton(LABEL.FILE.getLabel());
    private final JMenuItem save_button = new JMenuItem(LABEL.SAVE.getLabel());
    private final JMenuItem save_as_button = new JMenuItem(LABEL.SAVE_AS.getLabel());
    private final JMenuItem openFolderMenuItem = new JMenuItem(LABEL.OPEN_FOLDER.getLabel());
    private final JMenuItem openFileMenuItem = new JMenuItem(LABEL.OPEN_FILE.getLabel());
    private final JLabel folderLabel = new JLabel();
    private boolean isSaveButtonEnabled = false;
    private boolean isTextModified = false;
    
    public App() {
        renderWindow();
        addComponentsToFrame();
        configureComponents();
        addEventListeners();
        configureSidebar();
        configureTextArea();

        text_editor_title.setText(TEXT_EDITOR_TITLE);
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
        popupMenu.add(save_button);
        popupMenu.add(save_as_button);
        popupMenu.show(file_button, 0, file_button.getHeight());
    }

    private void showOpenFolderDialog() {
        File folder = fileHandler.openFolderDialog(this);
        JList<String> filesFromFolder = fileHandler.getFilesFromFolder(folder);
        
        sidebarPanel.removeAll();
        sidebarPanel.add(new JScrollPane(filesFromFolder));
        sidebarPanel.revalidate();
        sidebarPanel.repaint();

        folderLabel.setText(folder.getName());

        filesFromFolder.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) { 
                try {
                    File file = fileHandler.openFileFromFolder(folder, filesFromFolder.getSelectedValue());
                    String content = fileHandler.getContent(file);
                    loadContent(file, content);
                    handleSaveButton(file, content);
                    folderLabel.setText(folder.getName() + " - " + file.getName());
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
                loadContent(file, content);
                handleSaveButton(file, content);
                folderLabel.setText(file.getParentFile().getName() + " - " + file.getName());
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

    private void loadContent(File file, String content) {
        text_area.setText(content);
        System.out.println("CONTENT LOADED: " + file.getAbsolutePath());
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