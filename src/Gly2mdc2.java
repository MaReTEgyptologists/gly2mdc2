/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Heidi Jauhiainen
 * MaReTE Machine-readable Texts for Egyptologists
 * University of Helsinki
 * Version 2.0 of Gly2Mdc that converts a .gly file produced with JSesh to 3 different texts:
                1. cleaned Manuel de Codage encoding
                2. stripped Manuel de Codage encoding in Ramses Transliteration Corpus style
                3. unicode characters.
                Additionally makes a JSON-format text where the signs have been annotated with encoding, unicode, and Thot Sign List designations
 *
 */
public class Gly2mdc2 {
    
    private static TreeMap<String, Sign> signs;
    private static TreeMap<String, String> TSLs;
    private static TreeMap<String, String> mdcTranslits;
    private static TreeMap<String, String> codepoints;
    private static Model model;
    private static Font currentFont;
    private static TreeMap<String, String> insertions;
    private static TreeMap<String, String> annotations;
    private static String latestLine;
    private static String origMdc;
    private static String cleanedMdc;
    private static String unicodes;
    private static int lineCount;
    private static String dirToOpen;
    
    
    public Gly2mdc2() {
        
        JFrame frame = new JFrame();
        frame.setTitle("Gly2mdc2");
        frame.setLayout(new BorderLayout());
        frame.setSize(new Dimension(800, 850));
        frame.getContentPane().setBackground(Color.WHITE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Gly2Mdc v. 2.0", SwingConstants.CENTER);
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 18));
        title.setPreferredSize(new Dimension(250, 50));
        titlePanel.add(title, BorderLayout.CENTER);
        
        //InfoPanel: together with titlePanel and selectPanel forms the upper part of the upperPanel, contains the infoTextArea
        JPanel infoPanel = getInfoPanel();
        
        //button for selecting the file to show
        JButton fileDialogButton = getButton("Select File to Open");
        //button for extra info
        JButton infoButton;
        infoButton = getButton("INFO");
        infoButton.setFont(new Font("", Font.BOLD, 12));
        
        //Toolbar that contains the fileDialogButton and the infoButton 
        JToolBar toolbar = new JToolBar();
        toolbar.add(fileDialogButton);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(infoButton, codepoints);
        toolbar.addSeparator(); 
        
        //panel with toolbar, in upperPanel>mainPanel
        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.add(toolbar);

        JPanel upperPanel = new JPanel(new BorderLayout());
        upperPanel.add(titlePanel, BorderLayout.NORTH);
        upperPanel.add(infoPanel, BorderLayout.CENTER);
        upperPanel.add(selectPanel, BorderLayout.SOUTH);
        
        //Four text panels with textAreas
        //Manuel de Codage = original encoding without info meant for the editor
        JTextArea mdcTextArea = getTabTextArea("Manuel de Codage", "", 12);
        JPanel mdcTextPanel = getTabPanel("Manuel de Codage", mdcTextArea); 
        //Pure MdC = encoding stripped from control characters and converted to "pure" mdc without transliteration
        JTextArea modifiedTextArea = getTabTextArea("Pure MdC", "", 12);
        JPanel modifiedTextPanel = getTabPanel("Mdc stripped from annotations", modifiedTextArea);
        //Unicode
        String fontName = currentFont.getFontName();
        JTextArea unicodeTextArea = getTabTextArea("Unicode", fontName, 20);
        JPanel unicodeTextPanel = getTabPanel("Unicode", unicodeTextArea);
        //JSON format
        JTextArea jsonTextArea = getTabTextArea("Json", "Aegyptus", 18);
        JPanel jsonTextPanel = getTabPanel("Json", jsonTextArea);
        
        //tabs contais the four textpanels in tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBounds(200,200,200,200);  
        tabs.add("Manuel de Codage", mdcTextPanel);
        tabs.add("Pure MdC", modifiedTextPanel);
        tabs.add("Unicode", unicodeTextPanel);
        tabs.add("Json", jsonTextPanel);
        
        //toolbar for the button and checklist for saving the file
        JToolBar toolbar2 = new JToolBar();
        toolbar2.addSeparator(); 
        
        JLabel label = new JLabel("SAVE FILES Select: ");
        label.setFont(new Font(title.getFont().getName(), Font.BOLD, 12));
        toolbar2.add(label);

        JButton selectAll = getButton("All/Clear");
        toolbar2.add(selectAll);
        toolbar2.addSeparator();
        
        //checkboxes
        JCheckBox checkMdc = new JCheckBox("MdC");
        toolbar2.add(checkMdc);
        JCheckBox modifiedMdc = new JCheckBox("Cleaned");
        toolbar2.add(modifiedMdc);
        JCheckBox uniMdc = new JCheckBox("Unicode");
        toolbar2.add(uniMdc);
        toolbar2.addSeparator();
        JCheckBox json = new JCheckBox("JSON");
        toolbar2.add(json);
        toolbar2.addSeparator();
        //button for choosing the directory to save in, in saveControlPanel<selectPanel<upperPanel<mainPanel
        JButton saveDialogButton = getButton("Save file(s)");
        toolbar2.add(saveDialogButton);
        
        //panel with save buttons, in upperPanel>mainPanel
        JPanel selectPanel2 = new JPanel(new BorderLayout());
        selectPanel2.add(toolbar2);
        
        //lowerPanel contais the selectpanel2 for the save buttons
        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        lowerPanel.add(selectPanel2, BorderLayout.CENTER);
 
        //Add all the parts to the main panel
        mainPanel.add(upperPanel, BorderLayout.NORTH);
        mainPanel.add(tabs);
        mainPanel.add(lowerPanel, BorderLayout.SOUTH);
        
        //FUNCTIONALITY
        //OPEN FILE TO VIEW
        FileDialog openDialog = new FileDialog(new Frame(), "Choose a file", FileDialog.LOAD);
        String[] fileToOpen = new String[1];
        String file = "";
        //always open in home directory
        //dirToOpen=("~/");
        openDialog.setDirectory(dirToOpen);
        fileDialogButton.addActionListener((e) -> {
            
            openDialog.setVisible(true);
            fileToOpen[0] = openDialog.getDirectory()+openDialog.getFile();
            if (!fileToOpen[0].equals("nullnull")) {
                processText(fileToOpen[0]);
                //remove previous texts from the textAreas
                if (mdcTextArea.getLineCount() != 0) {
                    mdcTextArea.removeAll();
                    unicodeTextArea.removeAll();
                    modifiedTextArea.removeAll();
                    jsonTextArea.removeAll();
                }
                mdcTextArea.setText(origMdc);
                mdcTextArea.select(0, 0);
                modifiedTextArea.setText(cleanedMdc);
                modifiedTextArea.select(0, 0);

                unicodeTextArea.setText(unicodes);
                unicodeTextArea.select(0, 0);

                String origJson = getGson();
                jsonTextArea.setText(origJson);
                jsonTextArea.select(0, 0);
            }
        });
        
        FileDialog saveDialog = new FileDialog(new Frame(), "Save file as...(extension txt/json will be added)", FileDialog.SAVE);

        saveDialogButton.addActionListener((e) -> {
            saveDialog.setDirectory(fileToOpen[0].replaceFirst("/[^/]*$", ""));
            saveDialog.setFile(fileToOpen[0].replaceAll("[^/]*/", "").replaceFirst("\\..*", ""));
            saveDialog.setVisible(true);
            String dir = saveDialog.getDirectory();
            String filename = saveDialog.getFile();
            String toFile;
            if (checkMdc.isSelected()) {
                toFile = mdcTextArea.getText();
                try {
                    saveToFile(toFile, dir, "mdc_"+filename+".txt");
                } catch (IOException ex) {
                    Logger.getLogger(Gly2mdc2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (modifiedMdc.isSelected()) {
                toFile = modifiedTextArea.getText();
                try {
                    saveToFile(toFile, dir, "pureMdc"+filename+".txt");
                } catch (IOException ex) {
                    Logger.getLogger(Gly2mdc2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (uniMdc.isSelected()) {
                toFile = unicodeTextArea.getText();
                try {
                    saveToFile(toFile, dir, "unicode_"+filename+".txt");
                } catch (IOException ex) {
                    Logger.getLogger(Gly2mdc2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (json.isSelected()) {
                toFile = jsonTextArea.getText();
                try {
                    saveToFile(toFile, dir, filename+".json");
                } catch (IOException ex) {
                    Logger.getLogger(Gly2mdc2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
         });
        
        selectAll.addActionListener((e) ->{
            if (!checkMdc.isSelected() || !modifiedMdc.isSelected() || !uniMdc.isSelected() || !json.isSelected()) {
                checkMdc.setSelected(true);
                modifiedMdc.setSelected(true);
                uniMdc.setSelected(true);
                json.setSelected(true);
            }
            else {
                checkMdc.setSelected(false);
                modifiedMdc.setSelected(false);
                uniMdc.setSelected(false);
                json.setSelected(false);
            }
        });

        frame.add(mainPanel, BorderLayout.CENTER);
    }
    
    private static JPanel getInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());

        String info = String.format("<html><body style=\"text-align: justify;  text-justify: inter-word;\">%s</body></html>",""
                + "  Gly2Mdc version 2 converts a <i>.gly</i> file produced with JSesh to 3 different texts: <br/>"
                + "         &emsp 1. cleaned Manuel de Codage encoding<br/>"
                + "         &emsp 2. stripped Manuel de Codage encoding in Ramses Transliteration Corpus style<br/>"
                + "         &emsp 3. unicode characters.<br/>"
                + "  Additionally makes a JSON-format text where the signs have been annotated with encoding, unicode, and Thot Sign List designations.\n"
                + "<br/>"
                + "  Select file with extension .gly to view. You can then choose which versions to save to file.<br/>");

        JLabel label = new JLabel(info);
        Dimension size = label.getPreferredSize();
        label.setBounds(10, 120, size.width, size.height);
        infoPanel.add(label);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return infoPanel;
    }
    
    private static JButton getButton(String text) {
        JButton button = new JButton(text);
        button.setBorder(new RoundedBorder(4));
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        return button;
    }
    
    
    private static JPanel getTabPanel(String text, JTextArea textArea) {
        JPanel thisPanel = new JPanel();
        thisPanel.setLayout(new BorderLayout());
        
        JLabel thisLabel = new JLabel(text);
        thisLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        
        thisPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return thisPanel;
    }
    
    private static JTextArea getTabTextArea(String text, String font, int size) {
        JTextArea textArea = new JTextArea(20,20);
        textArea.setText(text);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(5, 5, 5,5));
        textArea.setFont(new Font(font, 0, size));
        return textArea;
    }
    
    
    private static String getGson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String jsonOutput = gson.toJson(model);
        return jsonOutput;
    }
    
    public static String getMdc(String filename) {
        String longString = readBytes(filename);
        String[] lines = longString.split("\n"), lineArray;
        String line, toFile = "", sign, kind;
        for (int i=0; i<lines.length; i++) {
            line = lines[i];
            if (!line.startsWith("++")) {
                line = line.replaceFirst("\\-\\!", "");
                line = line.replaceAll("_?\\-", " ");
                if (line.startsWith("|")) {
                    String lineNr = getMatch(line, "\\|([^ ]*) ");
                    line = line.replaceFirst("\\|[^ ]* ", "");
                    line = lineNr+" - "+line;
                }
                toFile += line+"\n";
            }
        }
        return toFile;
    }
    
    //read the text from the file that was selected
    public static void processText(String filename) {
        origMdc = "";
        cleanedMdc = "";
        latestLine = "";
        unicodes = "";
        lineCount = 0;
        model = new Model();
        String longString = readBytes(filename);
        String[] lines = longString.split("\n");
        String line;
        for (String line1 : lines) {
            line = line1;
            //ignore lines meant for the hieroglyphic text editor
            if (!line.startsWith("++")) {
                line = line.replaceFirst("\\-?!!?", "");
                line = line.replaceFirst("_$", "");
                if (!line.startsWith("+")) {
                    line = line.replaceAll("_?\\-", " ");
                }
                String lineNr = "";
                //if line starts with | it has the line number or designation in the beginning
                if (line.startsWith("|")) {
                    lineNr = getMatch(line, "\\|([^ ]*) ");
                    line = line.replaceFirst("\\|[^ ]* ", "");
                }
                //comments
                if (line.startsWith("+")) {
                    line = line.replaceAll("\\+s", " ");
                    line = line.replaceAll("\\+[libtchgr]", " ");
                    if (!model.getTextName().equals("")) {
                        model.setInfo(line);
                    }
                    if (model.getTextName().equals("")) {
                        model.setTextName(line);
                    }
                    line = "+"+line;
                    
                }
                
                line = line.replaceAll(" +", " ");
                //send to line to converted to "pure" encoding
                processMdc(line, lineNr);
                if (!lineNr.equals("")) {
                    line = lineNr+" - "+line;
                }
                //send line to be converted to Unicode
                processUnicode(line);

                line = line.replaceFirst("\\+ ?", "");

                origMdc += line.trim()+"\n";
            }
        }
    }
    
    //convert the encoding to "pure" encoding without control character and transliteration
    private static void processMdc(String line, String lineNr) {
        //ignore comment lines
        if (!line.startsWith("+")) {
            String tempLine = "", tempMdc;
            line = line.replaceAll("\\+s", " ");
            line = line.replaceAll("[:*&^\\[\\]_']", " ");
            line = line.replaceAll("\\\\ ", " ");
            line = line.replaceAll("\\{\\{[^\\}]*\\}\\}", "");
            line = line.replaceAll("<[^ ]*", "");
            line = line.replaceAll("[^ ]*>", "");
            line = line.replaceAll("\\.\\.", "\t");
            line = line.replaceAll("\\.", "  ");

            line = line.replaceAll("[\\(\\)]", "");
            line = line.replaceAll("#[^ ]*", " ");
            line = line.replaceAll(" [hv]?/{1,2}", " LACUNA");
            line = line.replaceAll("[!<>]", " ");
            line = line.replaceAll(" +", " ");
            String[] lineArray = line.split(" ");
            for (String mdc : lineArray) {
                tempMdc = "";
                //change transliterations to Gardiner sign list codes
                //certain transliteration transliterations in the list have rotation, get those first
                if (mdc.matches("[^ ]*\\\\R[0-9]+")) {
                    if (mdcTranslits.containsKey(mdc)) {
                        tempMdc = mdcTranslits.get(mdc);
                    }
                }
                //remove rotation and size designations and change then transliteration
                mdc = mdc.replaceAll("\\\\R?[0-9]+", "");
                if (mdcTranslits.containsKey(mdc)) {
                    mdc = mdcTranslits.get(mdc);
                }
                if (!tempMdc.equals("")) {
                    mdc = tempMdc;
                }
                tempLine += mdc+" ";
            }
            line = tempLine;
            
            //put line number back to the beginning
            if (!lineNr.equals("")) {
                line = lineNr+" - "+line;
            }
            line = line.replaceAll("[*^&:]", " ");
            line = line.replaceAll(" +", " ");
        }
        else {
            line = line.replaceFirst("\\+ ?", "");
        }
        cleanedMdc += line+"\n";
    }
    
    private static String cleanLine(String line) {
        //if comment
        if (line.startsWith("+")) {
            line = line.replaceAll("\\+s", " ");
            line = line.replaceAll("\\+[libtchgr]", " ");
            line = line.replaceAll("\\+\\+", " ");
            line = line.replaceAll(" +", " ");
            if (model.getTextName().equals("")) {
                model.setTextName(line);
            }
            return line.trim();
        }
        line = line.replaceAll("\\+(\\+)|([libtchgr])[^\\+]+\\+s", "");
        line = line.replaceAll("\\+s", " ");
        line = line.replaceAll("\\+", " ");
        line = line.replaceAll(" +", " ");
        return line;
    }
    
    //convert the line to Unicode characters
    private static void processUnicode(String line) {
        //handle comment lines first
        if (line.startsWith("+")) {
            line = line.replaceFirst("\\+ ?", "");
            unicodes += cleanLine(line)+"\n"; 
        }
        else {
            lineCount++;

            //change ** which indicates absolute positioning to *
            //it is impossible to know what the placing is
            line = line.replaceAll("_?\\*\\*", "*");
            line = line.replaceAll(" & ([^\\]])", "&$1");
            line = line.replaceAll("_?&+", "&");
            line = line.replaceAll("\\^+", "^");

            String[] lineArray = line.trim().split(" ");
            String shade = "";
            String mdc;
            Stack signStack = new Stack();
            String mdcLine = "";
            String thisChar;
            String uniLine = "";
            String jsonLine = "";
            
            //add the encoded signs to a stack
            for (int j=0; j<lineArray.length; j++) {
                mdc = lineArray[j];
                if (!mdc.contains("[") && !mdc.contains("]")) {
                    while (mdc.contains("(") && !mdc.contains(")")) {
                        mdc = mdc+"*"+lineArray[++j];
                    }
                }
                signStack.add(mdc);
            }
            annotations = new TreeMap<>();
            
            //start from the last sign
            while (!signStack.empty()) {
                String shaded;
                mdc = signStack.pop().toString();
                //separate different addition to signs/sign groups and add back to stack
                if (mdc.contains("[") && mdc.contains("]")) {
                    //separate the brackets from the word
                    mdc = mdc.replaceAll("\\[", " [");
                    mdc = mdc.replaceAll("\\]", "] ");
                    mdc = mdc.replaceAll("\\] \\{", "]{");
                    mdc = mdc.replaceAll("\\[ ([\\[&\\{\"'\\(\\?])", "[$1").trim();
                    mdc = mdc.replaceAll("([\\]&\\}\"'\\)\\?]) \\]", "$1]").trim();
                    
                    //handle shades that consern the bracketed region
                    String endShade = "";
                    if (mdc.matches("[^#]+#[1-4]{1,4}$")) {
                        endShade = getMatch(mdc, "(#[1-4]{1,4}$)");
                        mdc = getMatch(mdc, "([^#]+)#[1-4]{1,4}$");
                    }
                    if (mdc.contains("{") || mdc.contains("\\")) {
                        mdc = getAnnotations(mdc);
                    }
                    if (mdc.contains("&") || mdc.contains("^")) {
                       mdc = getInsertions(mdc);
                    }
                    mdc = addSpaces(mdc, false);
                    if (!endShade.equals("")) {
                        mdc = addShades(mdc, endShade).replaceAll(" +", " ");
                    }

                    signStack.addAll(Arrays.asList(mdc.split(" ")));
                }

                else if (mdc.contains("{") || mdc.contains("\\")) {
                    String toAdd = getAnnotations(mdc);
                    signStack.add(toAdd);
                }
                //end of shading, all signs till the #b get full shading added
                else if (mdc.equals("#e")) {
                   shaded = "";
                   mdc = signStack.pop().toString();
                   while (!mdc.equals("#b")) {
                        mdc = addSpaces(mdc, false);
                        shaded = addShades(mdc, "#1234")+" "+shaded;
                        mdc = signStack.pop().toString();
                   }
                    signStack.addAll(Arrays.asList(shaded.split(" ")));
                }
                //when sign or sign group is followed by shading information, put the shading for each sign according to its position in the group
                else if (mdc.contains("#") && !mdc.startsWith("#")) {
                    if (mdc.matches("[^#]+##[^1-4].*")) {
                        mdc = mdc.replaceFirst("##", " ## ");
                        signStack.add(mdc.split(" ")[0]);
                        signStack.add(mdc.split(" ")[1]);
                        signStack.add(mdc.split(" ")[2]);
                        continue;
                    }
                    shade = "#"+mdc.split("#")[1];
                    mdc = mdc.split("#")[0];
                    shaded = "";
                    mdc = addSpaces(mdc, false);
                    //the actual assignment of shading to each sign is done in addShades
                    shaded = addShades(mdc, shade);//+" "+shaded;
                    signStack.addAll(Arrays.asList(shaded.split(" ")));

                }
                //handle insertion e.g. ligatures and sign placed inside/under etc. of each other
                else if ((mdc.contains("&") || mdc.contains("^")) && (!mdc.matches("\\[[&\\{\\[\"'\\?]") && !mdc.matches("[&\\}\\]\"'\\?]\\]"))) {
                    mdc = getInsertions(mdc);
                    signStack.addAll(Arrays.asList(mdc.split(" ")));
                }
                //clean signs and sign groups are ready for adding to the finale line
                else {
                    
                    mdc = addSpaces(mdc, false);
                    if (!annotations.isEmpty()) {
                        String[] array = mdc.split(" ");
                        String toAdd = "";
                        ArrayList<String> toRemove = new ArrayList<>();
                        boolean added = false;
                        for (String arr : array) {
                            for (Map.Entry<String, String> entry : annotations.entrySet()) {
                                if (arr.startsWith(entry.getKey())) {
                                    toAdd += arr+" "+entry.getValue()+" ";
                                    toRemove.add(entry.getKey());
                                    added = true;
                                }
                            }
                            if (!added) {
                                toAdd += arr+" ";
                            }
                            for (String rem : toRemove) {
                                annotations.remove(rem);
                            }
                        }
                        mdc = toAdd;
                    }
                    //line in mdc encoding
                    mdcLine = mdc+" "+mdcLine;
                    //get the unicodes for the sign(s)
                    thisChar = getUnicodes(mdc);
                    //line with Unicode characters without spaces
                    uniLine = thisChar+""+uniLine;
                    //line with Unicode characters with spaces, for the JSON file
                    jsonLine = thisChar+" "+jsonLine;
                }
            }
            uniLine = uniLine.replaceAll(" *", "");
            jsonLine = jsonLine.replaceAll(" +", " ").trim();
            //add the tokens of the line to the JSON model
            addToModel(jsonLine, mdcLine, lineCount);

            unicodes += uniLine+"\n";
        }
    }
    
    //if sign has modifiers (size, placing, rotation), re-add them after the sign after Unicodes and pure mdc have been handled
    private static String getAnnotations(String mdc) {
        String divided = mdc.replaceAll("([:\\*&^])", " $1 ");
        String[] array = divided.split(" ");
        String toAdd = "";
        for (String arr : array) {
            if (arr.contains("{")) {
                arr = arr.replaceAll("\\{\\{", " {{");
                String[] arrArray = arr.split(" ");
                annotations.put(arrArray[0], arrArray[1]);
                toAdd += arrArray[0];
            }
            else if (arr.contains("\\")) {
                arr = arr.replace("\\", " €");
                String[] arrArray = arr.split(" ");
                annotations.put(arrArray[0], arrArray[1]);
                toAdd += arrArray[0];
            }
            else {
                toAdd += arr;
            }
        }
        return toAdd;
    }
    
    //add information for each sign in the line to the JSON model
    private static void addToModel(String thisChars, String mdcLine, int count) {
        Item item;
        String lineNr = "";
        mdcLine = mdcLine.replaceAll(" +", " ");
        if (mdcLine.contains("-")) {
            lineNr = mdcLine.split(" - ")[0];
            latestLine = lineNr;
        }
        mdcLine = mdcLine.replaceFirst("^[^-]*- ", "");
        if (lineNr.equals("")) {
            if (latestLine.equals("")) {
                lineNr = Integer.toString(count);
            }
            else {
                lineNr = latestLine;
            }
        }
        thisChars = thisChars.replaceFirst("^[^\\-]*-", "");  

        //ignore comment lines
        if (!mdcLine.startsWith("+")) {
            String uniLine = thisChars.replaceAll(" +", " ");
            mdcLine = mdcLine.replaceAll("\\++[^\\+]\\+s", "");
            String[] mdcArray = mdcLine.split(" ");
            String[] uniArray = uniLine.split(" ");
            int j = 0;
            String mdc, uni, encoding ="", codepoint ="", tsl="";
            Sign sign, sign2;
            for (int i=0; i<mdcArray.length; i++) { 
                encoding = mdcArray[i];
                //placing, rotation and size
                if (encoding.startsWith("{{") || encoding.startsWith("€")) {
                    item = model.getLast();
                    //absolute placing
                    if (encoding.startsWith("{{")) {
                        item.setPlacing(encoding);
                    }
                    //rotation and size
                    else {
                        encoding = encoding.replace("€", "");
                        if (encoding.contains("R")) {
                            if (mdcTranslits.containsKey(item.getMdc()+""+encoding)) {
                                item.setMdc(mdcTranslits.get(item.getMdc()+""+encoding));
                                item.setRotation(encoding);
                                item.setUnicode(signs.get(item.getMdc()).getUni());
                            }
                            else {
                                item.setRotation(encoding);
                            }
                            j++;
                        }
                        else {
                            item.setSize(encoding);
                        }
                        
                    }
                    model.deleteLast();
                    model.setItem(item);
                    continue;
                }
                mdc = "";
                if (mdcTranslits.containsKey(encoding)) {
                    mdc = mdcTranslits.get(encoding);
                }
                else if (!signs.containsKey(encoding)) {
                    if (encoding.matches("[a-zA-Z]+[0-9]+[A-Za-z]")) {
                        String shortMdc = encoding.replaceAll("([0-9])[A-Za-z]$", "$1");
                        mdc = shortMdc;
                    }
                    else {
                        mdc = encoding;
                    }
                }
                else {
                    mdc = encoding;
                }
                
                uni = uniArray[j];

                if (signs.containsKey(mdc)) {
                    sign = signs.get(mdc);
                    codepoint = sign.getCodepoint();
                    tsl = sign.getTsl();
                }
                else {
                    String uni2 = "";
                    int count2 = 0;
                    for (int k=j; k<i+mdc.length(); k++) {
                        if (k<uniArray.length) {
                            uni2 += uniArray[k];
                            count2++;
                        }
                    }
                    if (uni2.equals(mdc)) {
                        uni = uni2;
                        j += count2-1;
                    }
                }
                
                item = new Item(lineNr, i+1, encoding, mdc, uni, tsl, codepoint);
                if (codepoint.startsWith("1343") || codepoint.startsWith("1344") || codepoint.startsWith("1345")) {
                    item.setControlCharacter("Yes");
                }
                if (i<mdcArray.length-1 && mdcArray[i+1].startsWith("#")) {
                    item.setShading("YES");
                }
                model.setItem(item);
                j++;
            }
        }
    }
    
    //separate the encodings of the sign groups
    private static String addSpaces(String line, boolean original) {
        line = line.replaceAll(":", " : ");
        line = line.replaceAll("&", " & ");
        line = line.replaceAll("\\^", " ^ ");
        line = line.replaceAll("\\*", " * ");
        line = line.replaceAll("\\(", " ( ");
        line = line.replaceAll("\\)", " ) ");
        if (original) {
            line = line.replaceAll("\\\\", " €");
        }
        return line.trim();
    }
    
    //handle the shading of signs which in Unicode is added after each sign separately
    //mdc = sign group, shade the shading for the entire group
    private static String addShades(String mdc, String shade) {
        String[] mdcArray;
        String thisShaded = "";
        mdcArray = mdc.replaceAll(" +", " ").trim().split(" ");
        if (mdcArray.length == 1) {
            return mdc+" "+shade+" ";
        }
        for (String thisMdc : mdcArray) {
            //ignore control characters and shading
            if (!thisMdc.matches("[:\\*&\\)\\(]") && !thisMdc.startsWith("#")) {
                int thisIndex = mdc.indexOf(thisMdc);
                int colon = mdc.indexOf(":");
                int asterisk = mdc.indexOf("*");
                int place = -1;
                //consider the placing of signs and colons/asterisks to each other
                if (thisIndex < colon) {
                    if (thisIndex < mdc.indexOf("(")) {
                        thisShaded += thisMdc+" "+shade+" ";
                    }
                    else if (thisIndex < asterisk) {
                        if (asterisk < colon) {
                            place = 1;
                        }
                        else {
                            place = 12;
                        }
                    }
                    else if (asterisk == -1) {
                        place = 12;
                    }
                    else {
                        place = 2;
                    }
                }
                else if (colon > -1) {
                    if (thisIndex > mdc.indexOf(")") && mdc.indexOf(")") > -1) {
                        thisShaded += thisMdc+" "+shade+" ";
                    }
                    else if (thisIndex < asterisk) {
                        place = 3;
                    }
                    else if (asterisk > -1 && asterisk > colon) {
                        place = 4;
                    }
                    else {
                        place = 34;
                    }
                }
                else {
                    if (thisIndex < asterisk) {
                        place = 13;
                    }
                    else if (asterisk > -1) {
                        place = 24;
                    }
                    else {
                        place = 1234;
                    }
                }
                //depending on the place of the sign in the group and the shading,
                //the sign gets partly or entirely shaded or not
                switch (place) {
                    case 1:
                        if (shade.contains("1")) {
                            thisShaded += thisMdc+" "+"#1234 ";
                        break;
                        }
                        else {
                            thisShaded += thisMdc+" ";
                            break;
                        }
                    case 2:
                        if (shade.contains("2")) {
                            thisShaded += thisMdc+" "+"#1234 ";
                        break;
                        }
                        else {
                            thisShaded += thisMdc+" ";
                            break;
                        }
                    case 3:
                        if (shade.contains("3")) {
                            thisShaded += thisMdc+" "+"#1234 ";
                        break;
                        }
                        else {
                            thisShaded += thisMdc+" ";
                            break;
                        }
                    case 4:
                        if (shade.contains("4")) {
                            thisShaded += thisMdc+" "+"#1234 ";
                        break;
                        }
                        else {
                            thisShaded += thisMdc+" ";
                            break;
                        }
                    case 12:
                        if (shade.contains("12")) {
                            thisShaded += thisMdc+" "+"#1234 ";
                        break;
                        }
                        else if (shade.contains("1")) {
                            thisShaded += thisMdc+" "+"#13 ";
                        break;
                        }
                        else if (shade.contains("2")) {
                            thisShaded += thisMdc+" "+"#24 ";
                        break;
                        }
                        else {
                            thisShaded += thisMdc+" ";
                            break;
                        }
                    case 13:
                        if (shade.contains("1") && shade.contains("3")) {
                            thisShaded += thisMdc+" "+"#1234 ";
                        break;
                        }
                        else if (shade.contains("1")) {
                            thisShaded += thisMdc+" "+"#12 ";
                        break;
                        }
                        else if (shade.contains("3")) {
                            thisShaded += thisMdc+" "+"#34 ";
                        break;
                        }
                        else {
                            thisShaded += thisMdc+" ";
                            break;
                        }
                    case 24:
                        if (shade.contains("2") && shade.contains("4")) {
                            thisShaded += thisMdc+" "+"#1234 ";
                        break;
                        }
                        else if (shade.contains("2")) {
                            thisShaded += thisMdc+" "+"#12 ";
                        break;
                        }
                        else if (shade.contains("4")) {
                            thisShaded += thisMdc+" "+"#34 ";
                        break;
                        }
                        else {
                            thisShaded += thisMdc+" ";
                            break;
                        }
                    case 34:
                        if (shade.contains("34")) {
                            thisShaded += thisMdc+" "+"#1234 ";
                        break;
                        }
                        else if (shade.contains("3")) {
                            thisShaded += thisMdc+" "+"#13 ";
                        break;
                        }
                        else if (shade.contains("4")) {
                            thisShaded += thisMdc+" "+"#24 ";
                        break;
                        }
                        else {
                            thisShaded += thisMdc+" ";
                            break;
                        }
                    default:
                        break;
                }
            }
            else if (!thisMdc.startsWith("#")) {
                thisShaded += thisMdc+" ";
            }
        }
        return thisShaded;
    }
    
    //add Unicode control character designations of insertions (= over, above, under etc.) to sign groups
    private static String getInsertions(String mdc) {
        mdc = mdc.replaceAll("[\\(\\)]", "");
        String[] mdcArray = mdc.split("&");
        String toReturn = "";
        //ligatures in JSesh
        if (mdc.equals("H&a") || mdc.equals("V28&a") || mdc.equals("V28&D36") || mdc.equals("H&D36")) {
            toReturn = mdcArray[0]+" ## "+mdcArray[1];
        }
        else if (mdc.equals("G36&X1&r") || mdc.equals("G36&X1&D21")) {
            toReturn = mdcArray[0]+" topEnd "+mdcArray[1]+" : "+mdcArray[2];
        }
        else if (mdc.equals("H&b&Xr") || mdc.equals("V28&b&Xr") || mdc.equals("V28&D58&Xr") || mdc.equals("V28&D58&T28") 
                || mdc.equals("V28&b&T28") || mdc.equals("H&b&T28") || mdc.equals("H&D58&T28") || mdc.equals("H&D58&Xr")) {
            toReturn = mdcArray[0]+" "+mdcArray[1]+" topStart "+mdcArray[2];
        }
        else if (mdc.equals("M27&t&Z1") || mdc.equals("M27&t&1") || mdc.equals("M27&X1&Z1") || mdc.equals("M27&X1&1")) {
            toReturn = mdcArray[0]+" bottomStart "+mdcArray[1]+" bottomEnd "+mdcArray[2];
        }
        else if (mdc.equals("R8&i&t") || mdc.equals("R8&M17&t") || mdc.equals("R8&M17&x1") || mdc.equals("R8&i&X1")) {
            toReturn = mdcArray[0]+" * "+mdcArray[1]+" : "+mdcArray[2];
        }
        else if (mdc.equals("D&md&N") || mdc.equals("I10&md&N") || mdc.equals("I10&S43&N") || mdc.equals("I10&S43&S3")
                || mdc.equals("I10&md&S3") || mdc.equals("D&S43&N") || mdc.equals("D&S43&S3") || mdc.equals("D&md&S3")) {
            toReturn = mdcArray[0]+" bottomStart "+mdcArray[1]+" * "+mdcArray[2];
        }
        else if (mdc.equals("D&md&md&md") || mdc.equals("I10&md&md&md") || mdc.equals("I10&S43&S43&S43") || mdc.equals("D&S43&S43&S43")) {
            toReturn = mdcArray[0]+" bottomStart "+mdcArray[1]+" * "+mdcArray[2]+" * "+mdcArray[3];
        }
        else if (mdc.equals("D&md&1") || mdc.equals("I10&md&1") || mdc.equals("I10&S43&1") || mdc.equals("I10&S43&Z1")
                || mdc.equals("I10&md&Z1") || mdc.equals("D&S43&1") || mdc.equals("D&S43&Z1") || mdc.equals("D&md&Z1")) {
            toReturn = mdcArray[0]+" bottomStart "+mdcArray[1]+" * "+mdcArray[2];
        }
        else if (mdc.equals("stp&n&ra") || mdc.equals("U21&n&ra") || mdc.equals("U21&N35&ra") || mdc.equals("U21&N35&N5")
                || mdc.equals("U21&n&N5") || mdc.equals("stp&N35&ra") || mdc.equals("stp&N35&N5") || mdc.equals("stp&n&N5")) {
            toReturn = mdcArray[0]+" topEnd "+mdcArray[1]+" : "+mdcArray[2];
        }
        else if (mdc.equals("s&mt") || mdc.equals("S29&mt") || mdc.equals("s&D52") || mdc.equals("S29&D52")) {
            toReturn = mdcArray[0]+" ## "+mdcArray[1];
        }
        else if (mdc.equals("G26&t&y") || mdc.equals("G26&X1&y") || mdc.equals("G26&t&Z4") || mdc.equals("G26&X1&Z4")) {
            toReturn = mdcArray[0]+" bottomStart "+mdcArray[1]+" bottomEnd "+mdcArray[2];
        }
        //add Dd to the ligatures
        else if (mdc.equals("D&d") || mdc.equals("I10&d") || mdc.equals("I10&dD46") || mdc.equals("D&D46")) {
            toReturn = mdcArray[0]+" bottomStart "+mdcArray[1];
        }
        if (!toReturn.equals("")) {
            return toReturn;
        }

        String devided = addSpaces(mdc, false);
        
        devided = devided.replaceAll("(\\[+)", " $1 ").trim();
        devided = devided.replaceAll("(\\]+)", " $1 ").trim();
        devided = devided.replaceAll(" +", " ");
        mdcArray =  devided.split(" ");
        
        String thisMdc, pos ="", toAdd = "", mdc1, mdc2, mdcForSign1, mdcForSign2;
        Sign sign1, sign2;
        String[] insert = new String[mdcArray.length];
        for (int i=0; i<mdcArray.length; i++) {
            mdc1 = mdcArray[i];
            mdc1 = isTranslit(mdc1);
            sign1 = signs.get(mdc1);
            insert[i] = "";
            if (mdc1.matches("[&\\^]")) {
                insert[i] = mdc1;
            }
            else if (sign1 != null && sign1.getInsert()) {
                insert[i] = "I";
            }
            else {
                insert[i] = "N";
            }
        }
        toAdd = "";
        for (int i=1; i<mdcArray.length; i+=2) {
            toAdd = toAdd.replaceAll(" +", " ");
            mdcForSign1 = "";
            mdcForSign2 = "";
            mdc2 = "";
            //get previous sign
            mdc1 = mdcArray[i-1];
            if (mdcTranslits.containsKey(mdc1)) {
                mdcForSign1 = mdcTranslits.get(mdc1);
            }
            else {
                mdcForSign1 = mdc1;
            }
            sign1 = signs.get(mdcForSign1);
            //get the following sign
            if (i<mdcArray.length-1) {
                mdc2 = mdcArray[i+1];
                mdcForSign2 = isTranslit(mdc2);
                if (mdcForSign2.equals("")) {
                    mdcForSign2 = mdc2;
                }
            }       
            sign2 = signs.get(mdcForSign2);
            //get the sign to consider
            thisMdc = mdcArray[i];
            //the easy ones above and next
            if (thisMdc.equals(":") || thisMdc.equals("*")) {
                if (toAdd.length() == 0) {
                    toAdd += mdc1+" "+thisMdc+" "+mdc2+" ";
                }
                else {
                    toAdd += " "+thisMdc+" "+mdc2+" ";
                }
            }
            //insertion before and after
            //find the position using the possible places for this sign then mark the insertion in the group
            else if (thisMdc.equals("&") || thisMdc.equals("^")) {
                if (thisMdc.equals("^")) {
                    if (sign2 != null) {
                        pos = sign2.getBegin();
                        if (pos.equals("")) {
                            pos = sign2.getTop();
                        }
                        else if (pos.equals("")) {
                            pos = sign1.getEnd();
                        }
                        else if (pos.equals("")) {
                            pos = sign1.getBottom();
                        }
                        else {
                            pos = "*";
                        }
                    }
                    if (!toAdd.equals("") && toAdd.split(" ").length == i) {
                            toAdd += " "+pos+" "+mdc2+" ";
                        }
                        else {
                            toAdd += mdc1+" "+pos+" "+mdc2+" ";
                        }
                }
                else if (thisMdc.equals("&")) {
                    String toDo1 = insert[i-1];
                    String toDo2 = insert[i+1];
                    if (toDo1.equals("I")) {
                        pos = sign1.getEnd();
                        if (pos.equals("")) {
                            pos = sign1.getMiddle();
                        }
                        if (pos.equals("")) {
                            pos = sign1.getTop();
                        }
                        if (pos.equals("")) {
                            pos = sign1.getBottom();
                        }
                        if (pos.equals("") && !annotations.isEmpty()) {
                            toAdd += mdc1+"##"+mdc2;
                            i++;
                            continue;
                        }
                        if (pos.equals("") && toDo2.equals("I") && sign2 != null) {
                            pos = sign2.getBegin();
                            if (!pos.equals("")) {
                                String[] toAddArray = toAdd.split(" ");
                                if (toAdd.length()>0 && toAddArray.length == i) {
                                    if (insert[i-2].equals("^")) {
                                        toAdd += pos+" "+mdc2;
                                    }
                                }
                                else {
                                    toAdd += mdc2+" "+pos+" "+mdc1+" ";
                                }
                                i++;
                                continue;
                            }
                        }
                        if (pos.equals("")) {
                            pos = sign1.getBegin();
                        }
                        if (!toAdd.equals("") && toAdd.split(" ").length == i) {
                            toAdd += pos+" "+mdc2+" ";
                        }
                        else {
                            toAdd += mdc1+" "+pos+" "+mdc2+" ";
                        }
                    }
                    else if (toDo2.equals("I")) {
                        pos = sign2.getBegin();
                        
                        if (pos.equals("")) {
                            pos = sign2.getMiddle();
                        }
                        if (pos.equals("")) {
                            pos = sign2.getTop();
                        }
                        if (pos.equals("")) {
                            pos = sign2.getBottom();
                        }
                        if (!toAdd.equals("") && toAdd.split(" ").length == i) {
                            toAdd += pos+" "+mdc2+" ";
                        }
                        else {
                            toAdd += mdc2+" "+pos+" "+mdc1+" ";
                        }
                    }
                    else {
                        if (toAdd.length() == 0) {
                            toAdd += mdc1+" * "+mdc2+" ";
                            
                        }
                        else {
                            toAdd += " * "+mdc2+" ";
                        }
                    }
                }
            }
        }
        return toAdd.trim();
    }
    
    //Check if the encoding is in the list of translit > encoding
    private static String isTranslit(String mdc) {
        if (mdcTranslits.containsKey(mdc)) {
            return mdcTranslits.get(mdc);
        }
        return "";
    }
    
    //get the Unicode sign for the given encoding
    private static String getUniForSign(String mdc) {
        if (signs.containsKey(mdc)) {
                Sign sign = signs.get(mdc);
                return sign.getUni();
            }
        return "";
    }
    
    //get the Unicodes for the sign or sign group
    private static String getUnicodes(String mdc) {
        String thisChar = "", uni, translit;
        //remove parentheses
        mdc = mdc.replaceAll("\\{\\{[^\\}]+\\}\\}", "");
        String[] mdcArray = mdc.split(" "), charArray;
        for (String thisMdc : mdcArray) {
            translit = isTranslit(thisMdc);
            if (!translit.equals("")) {
                String thisUni = "";
                if (translit.contains("*") || translit.contains(":")) {
                    charArray = translit.split("[\\*:]");
                    for (String character : charArray) {
                        thisUni += signs.get(character).getUni();
                    }
                    uni= thisUni;
                }
                else {
                    uni = getUniForSign(translit);
                }
            }
            else {
                uni = getUniForSign(thisMdc);
            }
            //if not an encoding in the list, check without letter at the end
            if (uni.equals("") && thisMdc.matches("[a-zA-Z]+[0-9]+[A-Za-z]")) {
                String shortMdc = thisMdc.replaceAll("([0-9])[A-Za-z]$", "$1");
                uni = getUniForSign(shortMdc);
            }
            //if still not an encoding in the list, check without rotations/size indications
            else if (uni.equals("") && thisMdc.matches(".*€R[0-9]+")) {
                String partMdc = thisMdc.split("€")[0];
                String rotation = "\\"+thisMdc.split("€")[1];
                String uniRot = "";
                uniRot = getUniForSign(rotation);
                uni = getUniForSign(partMdc);
                if (!uni.equals("")) {
                    if (!uniRot.equals("")) {
                        uni += " "+uniRot;
                    }
                }
                else {
                    uni = uniRot;
                }
            }
            if (!uni.isBlank()) {
                thisChar += " "+uni;
            }
            else if (!thisMdc.contains("€")) {
                thisChar += " "+thisMdc;
            }
        }
        return thisChar;
    }
    
    
    private static void saveToFile(String toFile, String dir, String filename) throws IOException {
        WriteToFile writer = new WriteToFile(dir+"/"+filename);
        writer.write(toFile);
        writer.end();
    }
    
    //read the text from the file
    private static String readBytes(String filename) {
        byte[] bytes = new byte[0];
        try (FileInputStream fis = new FileInputStream(filename)) {
            bytes = fis.readAllBytes();
            for (byte b : bytes) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String s = new String(bytes, StandardCharsets.UTF_8);
        return s;
    }
    
    //get a match for the pattern given
    private static String getMatch(String line, String pattern) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(line);
        
        String match = "";
        if (matcher.find()) {
            match = matcher.group(1);
        }
        return match;
    }
    
    
    //read unicode codepoints for mdc codes from mdc2uni.txt (mdc\tunicodepoint)
    private static void readUnicode(String filename) throws IOException {
        BufferedReader reader = null;
        InputStream is = null;
        signs = new TreeMap<>();
        codepoints = new TreeMap<>();
        try {
            // input stream
            is = Gly2mdc2.class.getResourceAsStream(filename);
            reader= new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineArray = line.split("\t");
                String mdc = lineArray[0];
                String uni = lineArray[1];
                Sign sign = new Sign(mdc, uni);
                signs.put(mdc, sign);
                codepoints.put(uni, mdc);
            }
        }
        catch (Exception e) {
            
        }
        finally {
            reader.close();
            is.close();
        }
    }
    
    
    //read Thot sign list designations of signs from signtsl.txt (copied from Nederhof?) format: <sign id="A1" tslsign="82" />
    public static void readTSL(String filename) throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        InputStream is = null;
        TSLs = new TreeMap<>();
        try {
            is = Gly2mdc2.class.getResourceAsStream(filename);
            reader= new BufferedReader(new InputStreamReader(is));
            String line, mdc, tsl;
            while ((line = reader.readLine()) != null) {
                if (line.contains("sign id")) {
                    mdc = getMatch(line, "id=\"([^\"]*)\"");
                    tsl = getMatch(line, "tslsign=\"([^\"]*)\"");
                    if (signs.containsKey(mdc)) {
                        Sign sign = signs.get(mdc);
                        sign.setTsl(tsl);
                        if (!TSLs.containsKey(mdc)) {
                            TSLs.put(tsl, mdc);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
            is.close();
        }
    }
    
    
    //read mdc-translits that can be used in jSesh instead of gardiner codes
    public static void readMdc(String filename) throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        InputStream i = null;
        mdcTranslits = new TreeMap<>();
        
        String[] lineArray;
        try {
            i = Gly2mdc2.class.getResourceAsStream(filename);
            reader= new BufferedReader(new InputStreamReader(i));
            String line, mdc, translit;
            while ((line = reader.readLine()) != null) {
                lineArray = line.split("\t");
                translit = lineArray[0];
                mdc = lineArray[1];
                mdc = mdc.replaceAll("[a-z]$", getMatch(mdc, "[0-9]([a-z])$").toUpperCase());
                if (signs.containsKey(mdc)) {
                    Sign sign = signs.get(mdc);
                    sign.setTranslit(translit);

                    if (!mdcTranslits.containsKey(translit)) {
                        mdcTranslits.put(translit, mdc);
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
            i.close();
        }
    }
    
    private static Font readFont(String filename) throws IOException {
        Font customFont = null;
        InputStream i = null;
        try {
            //create the font to use. Specify the size!
            i = Gly2mdc2.class.getResourceAsStream(filename);
            customFont = Font.createFont(Font.TRUETYPE_FONT, i).deriveFont(12f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //register the font
            ge.registerFont(customFont);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(FontFormatException e) {
            e.printStackTrace();
        }
        finally {
            if (i != null) {
                i.close();
            }
        }
        return customFont;
    }
    
    //read in the possible insertion points for signs
    private static void readInsertions(String filename) throws IOException {
        BufferedReader reader = null;
        InputStream is = null;
        insertions = new TreeMap<>();
        TreeMap<String, String> poss;
        String[] lineArray;
        String line, codepoint, s, e, b, t, m, temp;
        try {
            is = Gly2mdc2.class.getResourceAsStream(filename);
            reader= new BufferedReader(new InputStreamReader(is));
            while ((line = reader.readLine()) != null) {
                lineArray = line.split("\t");
                codepoint = lineArray[0];
                b="";
                t="";
                m="";
                s="";
                e="";
                poss = new TreeMap<>();
                for (int i=1; i<lineArray.length; i++) {
                    temp = lineArray[i];
                    if (temp.equals("b")) {
                        b = "bottom";
                    }
                    else if (temp.equals("t")) {
                        t = "top";
                    }
                    else if (temp.endsWith("s")) {
                        if (temp.startsWith("b")) {
                            s = "bottomStart";
                        }
                        else {
                            s = "topStart";
                        }
                    }
                    else if (temp.endsWith("e")) {
                        if (temp.startsWith("b")) {
                            e = "bottomEnd";
                        }
                        else {
                            e = "topEnd";
                        }
                    }
                    else if (temp.equals("m")) {
                        m = "middle";
                    }
                }
                Sign sign = signs.get(codepoints.get(codepoint));
                sign.setInsertions(s, e, m, t, b);
                insertions.put(codepoint, sign.getMdc());
                signs.put(sign.getMdc(), sign);
            }
        }
        catch (Exception ex) {
            
        }
        finally {
            reader.close();
            is.close();
        }
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, FontFormatException {
        dirToOpen = "~/";
        currentFont = readFont("resources/Aegyptus.otf");
        readUnicode("resources/mdc2uni.txt");
        readMdc("resources/translit2mdc.txt");
        readTSL("resources/signtsl.txt");
        readInsertions("resources/myIns.txt");
        
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            Gly2mdc2 gly2mdc2 = new Gly2mdc2();
        });
        
    }
    
    //Rounded borders for 
    //FROM: https://stackoverflow.com/questions/423950/rounded-swing-jbutton-using-java
    private static class RoundedBorder implements Border {
        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }
}
