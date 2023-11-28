
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileHandler {

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

    public JList<String> getFilesFromFolder(File folder) {

        JList<String> fileList = null;

        try {

            if (folder == null) {
                throw new FileNotFoundException("FOLDER NOT FOUND.");
            }

            File[] files = folder.listFiles();

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

        System.out.println("FILE OPENED: " +file.getAbsolutePath());

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