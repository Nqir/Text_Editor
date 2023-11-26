
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class TextEditorFrame extends JFrame {
    private static final String FILE_LABEL = "FILE";
    private static final String EDIT_LABEL = "EDIT";
    private static final String VIEW_LABEL = "VIEW";

    private JPanel panel = new JPanel();
    private JToolBar toolbar = new JToolBar();
    private JPopupMenu popupMenu = new JPopupMenu();
    
    private JLabel text_editor_title = new JLabel();
    private JTextArea text_area = new JTextArea();

    private final JButton file_button = new JButton(FILE_LABEL);
    private final JButton edit_button = new JButton(EDIT_LABEL);
    private final JButton view_button = new JButton(VIEW_LABEL);

    private JPanel sidebarPanel = new JPanel();
    private JPanel sidebarContainer = new JPanel();
    private final JMenuItem openFolderMenuItem = new JMenuItem("OPEN FOLDER");
    private final JMenuItem openFileMenuItem = new JMenuItem("OPEN FILE");
    private JLabel folderLabel = new JLabel();

    JFileChooser fileChooser = new JFileChooser();

    public TextEditorFrame() {
        displayFrame();
        addComponentsToFrame();
        addEventListeners();
        displayTextArea();
        displaySidebar();

        text_editor_title.setText("My Text Editor");
        text_editor_title.setHorizontalAlignment(JLabel.CENTER);
    }
    
    private void displayFrame() {
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
        toolbar.add(edit_button);
        toolbar.add(view_button);
    }

    private void displaySidebar() {
        sidebarPanel.setBackground(new Color(160, 160, 160));

        sidebarContainer.setLayout(new BoxLayout(sidebarContainer, BoxLayout.Y_AXIS));
        sidebarContainer.add(sidebarPanel);
        sidebarContainer.setPreferredSize(new Dimension(200, 200));
    
        sidebarContainer.add(folderLabel);

        panel.add(sidebarContainer, BorderLayout.EAST);
    }
    
    private void displayTextArea() {
        text_area.setPreferredSize(new Dimension(500, 500));
        text_area.setMargin(new Insets(10, 10, 10, 10));
        text_area.setBackground(new Color(34, 34, 34));
        text_area.setLineWrap(true);
        text_area.setWrapStyleWord(true);
        text_area.setForeground(new Color(255, 255, 255));
    }

    private void showFileMenu() {
        popupMenu.add(openFolderMenuItem);
        popupMenu.add(openFileMenuItem);
        
        popupMenu.show(file_button, 0, file_button.getHeight());
    }

    private void openFile() {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            System.out.println("OPENED FILE: ".concat(selectedFile.getName()));

            try(FileReader fileReader = new FileReader(selectedFile); 
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                
                    String line;
                    StringBuilder fileContents = new StringBuilder();

                    while((line = bufferedReader.readLine()) != null) {
                        fileContents.append(line).append('\n');
                    }

                    text_area.setText(fileContents.toString());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to open file: " + e.getMessage());
            }
        }
    }

    private void openFolder() {
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            File[] files = selectedFolder.listFiles();

            if (files != null) {
                DefaultListModel<String> listModel = new DefaultListModel<>();

                for (File file : files) {
                    listModel.addElement(file.getName());
                }

                JList<String> fileList = new JList<>(listModel);

                sidebarPanel.removeAll();

                sidebarPanel.add(new JScrollPane(fileList));
                sidebarPanel.revalidate();
                sidebarPanel.repaint();

                folderLabel.setText(selectedFolder.getName());

                fileList.addListSelectionListener(event -> handleFileSelection(fileList.getSelectedValue(), selectedFolder));
            }
        }
    }

    private void handleFileSelection(String file_name, File selectedFolder) {
        if (selectedFolder != null) {
            File selectedFile = new File(selectedFolder, file_name);
            loadFileContent(selectedFile);
        }
    }

    private void loadFileContent(File file) {
        System.out.println("OPENED FILE: ".concat(file.getName()));
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            StringBuilder content = new StringBuilder();
            String line;
            
            while((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }

            text_area.setText(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to open file: " + e.getMessage());
        }
    }

    private void addEventListeners() {
        file_button.addActionListener(event -> showFileMenu());
        openFileMenuItem.addActionListener(event -> openFile());
        openFolderMenuItem.addActionListener(event -> openFolder());
    }
}

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TextEditorFrame();
        });
    }
}