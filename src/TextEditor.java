import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {
    private List<MatchedWord> foundWords = new ArrayList<>();
    private MatchedWord lastSelectedWord = null;
    private JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

    //menu components
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("File");
    private JMenu findMenu = new JMenu("Find");
    private JMenuItem menuSaveItem = new JMenuItem("Save");
    private JMenuItem menuLoadItem = new JMenuItem("Load");
    private JMenuItem menuExitItem = new JMenuItem("Exit");
    private JMenuItem menuUseRegexItem = new JMenuItem("Use regex");
    private JMenuItem menuSearchItem = new JMenuItem("Search");
    private JMenuItem menuSearchPreviousItem = new JMenuItem("Previous");
    private JMenuItem menuSearchNextItem = new JMenuItem("Next");

    //top panel elements
    JPanel topPanel = new JPanel(new FlowLayout(0, 10, 10));
    JButton saveButton = new JButton(new ImageIcon("save.png"));
    JButton loadButton = new JButton(new ImageIcon("load.png"));
    JButton searchButton = new JButton(new ImageIcon("find.png"));
    JButton searchPrevious = new JButton(new ImageIcon("previous.png"));
    JButton searchNext = new JButton(new ImageIcon("next.png"));
    JCheckBox useRegex = new JCheckBox("use regex");

    //text panel
    JTextArea textArea = new JTextArea(17,42);
    JScrollPane scrollPane = new JScrollPane(textArea);
    JPanel textPanel = new JPanel();

    JTextField searchField = new JTextField(14);

    public TextEditor() {
        this.setLayout(new BorderLayout(10,2));
        setTitle("Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);

        //add menu components
        fileMenu.add(menuSaveItem);
        fileMenu.add(menuLoadItem);
        fileMenu.addSeparator();
        fileMenu.add(menuExitItem);
        findMenu.add(menuSearchItem);
        findMenu.add(menuSearchPreviousItem);
        findMenu.add(menuSearchNextItem);
        findMenu.add(menuUseRegexItem);
        menuBar.add(fileMenu);
        menuBar.add(findMenu);
        setJMenuBar(menuBar);

        //add text panel
        textPanel.add(scrollPane);
        textPanel.setPreferredSize(new Dimension(20,20));

        //top panel elements
        loadButton.setPreferredSize(new Dimension(30,30));
        saveButton.setPreferredSize(new Dimension(30,30));
        searchButton.setPreferredSize(new Dimension(30,30));
        searchPrevious.setPreferredSize(new Dimension(30,30));
        searchNext.setPreferredSize(new Dimension(30,30));
        searchField.setPreferredSize(new Dimension(10,30));

        //add to top panel
        topPanel.add(saveButton);
        topPanel.add(loadButton);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(searchPrevious);
        topPanel.add(searchNext);
        topPanel.add(useRegex);

        //action listeners
        addActionListeners();

        //add panels to GUI
        add(topPanel, BorderLayout.NORTH);
        add(textPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void save(String text) {
        //choose file
        int returnValue = fileChooser.showSaveDialog(null);
        File selectedFile = new File("doc.txt");
        if(returnValue == JFileChooser.APPROVE_OPTION){
            selectedFile = fileChooser.getSelectedFile();
            //add ".txt" if absent
            if(!selectedFile.getPath().matches(".*\\.txt")){
                selectedFile = new File(selectedFile.getPath()+".txt");
            }
        }

        //save file
        try {
            Files.write(selectedFile.toPath(), text.getBytes());
        }
        catch(IOException exc){
            System.out.println(exc);
        }
    }

    private String load(){
        //choose file
        int returnValue = fileChooser.showOpenDialog(null);
        File file = new File("doc.txt");
        if(returnValue == JFileChooser.APPROVE_OPTION){
            file = fileChooser.getSelectedFile();
        }

        //load file
        String loadedString = "";
        try{
            loadedString = Files.readString(file.toPath());
        }
        catch(IOException exc){
            System.out.println(exc);
        }
        return loadedString;
    }

    private void search(String text, String toFind, boolean useRegex){
        List<MatchedWord> words = new ArrayList<>();
        if(!text.isEmpty() && !toFind.isEmpty()) {
            if (useRegex) {
                Pattern pattern = Pattern.compile(toFind);
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    words.add(new MatchedWord(matcher.start(), matcher.end(), matcher.group()));
                }
            } else {
                int foundIndex = 0;
                int lastFoundIndex = 0;

                do {
                    foundIndex = text.indexOf(toFind, lastFoundIndex);
                    if (foundIndex != -1) {
                        words.add(new MatchedWord(foundIndex, foundIndex + toFind.length(), toFind));
                    }
                    lastFoundIndex = foundIndex + 1;
                } while (foundIndex != -1);
            }
        }
        foundWords = words;
    }

    private void addActionListeners(){
        saveButton.addActionListener(actionEvent -> save(textArea.getText()));
        loadButton.addActionListener(actionEvent -> textArea.setText(load()));
        menuSaveItem.addActionListener(actionEvent -> save(textArea.getText()));
        menuLoadItem.addActionListener(actionEvent -> textArea.setText(load()));
        menuExitItem.addActionListener(actionEvent -> dispose());
        searchButton.addActionListener(actionEvent -> {
            Thread searchThread = new Thread(() ->
                    search(textArea.getText(), searchField.getText(), useRegex.isSelected()));
            try {
                searchThread.start();
                searchThread.join();
            }
            catch(InterruptedException exc){
                System.out.println(exc);
            }
            if(!foundWords.isEmpty()){
                lastSelectedWord = foundWords.get(0);
                textArea.setCaretPosition(lastSelectedWord.end());
                textArea.select(lastSelectedWord.start(), lastSelectedWord.end());
                textArea.grabFocus();
            }
        });
        menuSearchItem.addActionListener(actionEvent -> {
            Thread searchThread = new Thread(() ->
                    search(textArea.getText(), searchField.getText(), useRegex.isSelected()));
            try {
                searchThread.start();
                searchThread.join();
            }
            catch(InterruptedException exc){
                System.out.println(exc);
            }
            if(!foundWords.isEmpty()){
                lastSelectedWord = foundWords.get(0);
                textArea.setCaretPosition(lastSelectedWord.end());
                textArea.select(lastSelectedWord.start(), lastSelectedWord.end());
                textArea.grabFocus();
            }
        });
        searchPrevious.addActionListener(actionEvent -> {
            if(lastSelectedWord != null && foundWords.size()>1){
                Collections.reverse(foundWords);
                ListIterator iterator = foundWords.listIterator();
                MatchedWord nextWord = new MatchedWord();
                while(!nextWord.equals(lastSelectedWord)){
                    nextWord = (MatchedWord)iterator.next();
                }
                if(iterator.hasNext()){
                    lastSelectedWord = (MatchedWord)iterator.next();
                }
                textArea.setCaretPosition(lastSelectedWord.end());
                textArea.select(lastSelectedWord.start(), lastSelectedWord.end());
                textArea.grabFocus();
                Collections.reverse(foundWords);
            }
        });
        menuSearchPreviousItem.addActionListener(actionEvent -> {
            if(lastSelectedWord != null && foundWords.size()>1){
                Collections.reverse(foundWords);
                ListIterator iterator = foundWords.listIterator();
                MatchedWord nextWord = new MatchedWord();
                while(!nextWord.equals(lastSelectedWord)){
                    nextWord = (MatchedWord)iterator.next();
                }
                if(iterator.hasNext()){
                    lastSelectedWord = (MatchedWord)iterator.next();
                }
                textArea.setCaretPosition(lastSelectedWord.end());
                textArea.select(lastSelectedWord.start(), lastSelectedWord.end());
                textArea.grabFocus();
                Collections.reverse(foundWords);
            }
        });
        searchNext.addActionListener(actionEvent -> {
            if(lastSelectedWord != null && foundWords.size()>1){
                ListIterator iterator = foundWords.listIterator();
                MatchedWord nextWord = new MatchedWord();
                while(!nextWord.equals(lastSelectedWord)){
                    nextWord = (MatchedWord)iterator.next();
                }
                if(iterator.hasNext()){
                    lastSelectedWord = (MatchedWord)iterator.next();
                }
                textArea.setCaretPosition(lastSelectedWord.end());
                textArea.select(lastSelectedWord.start(), lastSelectedWord.end());
                textArea.grabFocus();
            }
        });
        menuSearchNextItem.addActionListener(actionEvent -> {
            if(lastSelectedWord != null && foundWords.size()>1){
                ListIterator iterator = foundWords.listIterator();
                MatchedWord nextWord = new MatchedWord();
                while(!nextWord.equals(lastSelectedWord)){
                    nextWord = (MatchedWord)iterator.next();
                }
                if(iterator.hasNext()){
                    lastSelectedWord = (MatchedWord)iterator.next();
                }
                textArea.setCaretPosition(lastSelectedWord.end());
                textArea.select(lastSelectedWord.start(), lastSelectedWord.end());
                textArea.grabFocus();
            }
        });
        menuUseRegexItem.addActionListener(actionEvent -> {
            if(useRegex.isSelected()){
                useRegex.setSelected(false);
            }
            else{
                useRegex.setSelected(true);
            }
        });
    }
}
