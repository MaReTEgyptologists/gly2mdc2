/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.marete.gly2mdc2;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
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
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
                Additionally makes a JSON-format text where the signs have been annotated with encoding, Unicode, and 
                Thot Sign List designations
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
    private static String dirToOpen;
    private static String prevFile;
    
    
    public Gly2mdc2() throws IOException {
        
        JFrame frame = new JFrame();
        frame.setTitle("Gly2mdc2");
        //frame.setIconImage(frameImage);
        frame.setLayout(new BorderLayout());
        frame.setSize(new Dimension(1100, 850));
        frame.getContentPane().setBackground(Color.WHITE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        /*URL resource = frame.getClass().getResource("/resources/logo.png");
        BufferedImage image = ImageIO.read(resource);*/
        frame.setIconImage(new ImageIcon("/Users/hwikgren/NetBeansProjects/Gly2mdc2/src/main/resources/logo.png").getImage());
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
        //toolbar.add(infoButton, codepoints);
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
        prevFile = "";
        //always open in home directory
        //dirToOpen=("~/");
        openDialog.setDirectory(dirToOpen);
        fileDialogButton.addActionListener((e) -> {
            
            openDialog.setVisible(true);
            fileToOpen[0] = openDialog.getDirectory()+openDialog.getFile();
            if (!fileToOpen[0].equals("nullnull")) {
                prevFile = fileToOpen[0];
                try {
                    processText(fileToOpen[0]);
                } catch (IOException ex) {
                    Logger.getLogger(Gly2mdc2.class.getName()).log(Level.SEVERE, null, ex);
                }
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
            if (!checkMdc.isSelected() && !modifiedMdc.isSelected() && !uniMdc.isSelected() && !json.isSelected()) {
                    JOptionPane.showMessageDialog(null, "Select file format(s) to save!");
                }
            else {
                saveDialog.setDirectory(prevFile.replaceFirst("/[^/]*$", ""));
                saveDialog.setFile(prevFile.replaceAll("[^/]*/", "").replaceFirst("\\.gly", ""));
                saveDialog.setVisible(true);
                String dir = saveDialog.getDirectory();
                String filename = saveDialog.getFile();
                if (filename != null && !filename.equals("nullnull")) {
                    String toFile;
                    if (checkMdc.isSelected()) {
                        toFile = mdcTextArea.getText();
                        saveFile(toFile, dir, filename, "mdc");
                    }
                    if (modifiedMdc.isSelected()) {
                        toFile = modifiedTextArea.getText();
                        saveFile(toFile, dir, filename, "pureMdc");
                    }
                    if (uniMdc.isSelected()) {
                        toFile = unicodeTextArea.getText();
                        saveFile(toFile, dir, filename, "unicode");
                    }
                    if (json.isSelected()) {
                        toFile = jsonTextArea.getText();
                        try {
                            saveToFile(toFile, dir, filename+".json");
                        } catch (Exception ex) {
                            Logger.getLogger(Gly2mdc2.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

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
                + "         &emsp 2. stripped Manuel de Codage encoding in Ramses Transliteration Corpus and TLA database dump style<br/>"
                + "         &emsp 3. unicode hieroglyphic characters.<br/>"
                + "  Additionally makes a JSON-format text where the signs have been annotated with encoding, Unicode, and Thot Sign List designations.\n"
                + "<br/>"
                + "  Select file with extension .gly to view. You can then choose which version(s) to save to file.<br/>");

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
    
    private static void saveFile(String toFile, String dir, String filename, String which) {
        try {
            saveToFile(toFile, dir, filename+"_"+which+".txt");
        } catch (Exception ex) {
            Logger.getLogger(Gly2mdc2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static String getGson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String jsonOutput = gson.toJson(model);
        return jsonOutput.replaceAll("\\\\\\\\", "\\\\");
    }
    
    public static String getMdc(String filename) throws IOException {
        String longString = readBytes(filename);
        String[] lines = longString.split("\n");
        String line, toFile = "";
        for (int i=0; i<lines.length; i++) {
            line = lines[i];
            if (!line.startsWith("++")) {
                line = line.replaceFirst("\\-\\!", "");
                line = line.replaceAll("_?\\-", " ");
                if (line.startsWith("|")) {
                    String lineNr = getMatch(line, "\\|([^-]*)\\-");
                    line = line.replaceFirst("\\|[^-]*\\-", "");
                    line = lineNr+" - "+line;
                }
                toFile += line+"\n";
            }
        }
        return toFile;
    }
    
    //read the text from the file that was selected
    public static void processText(String filename) throws UnsupportedEncodingException, IOException {
        origMdc = "";
        cleanedMdc = "";
        latestLine = "";
        unicodes = "";
        model = new Model();
        String longString = readBytes(filename);
        String[] lines = longString.split("\n");
        String line;
        boolean infosEnded = false;
        int count = 0;
        for (String line1 : lines) {
            
            line = line1;
            //ignore lines meant for the hieroglyphic text editor
            if (!line.startsWith("++")) {
                
                line = line.replaceFirst("(\\-?!!?)|(_$)", "");
                
                //if line starts with | it has the line number or designation in the beginning
                String lineNr = "";
                if (line.startsWith("|")) {
                    lineNr = getMatch(line, "(\\|[^-]*)\\-");
                    latestLine = lineNr;
                }

                //comments
                if (line.startsWith("+")) {
                    if (!infosEnded) {
                        line = cleanLine(line, false);
                    }
                    else {
                        ++count;
                        line = line.replaceFirst("^\\|[^-]*\\-", "");
                        line = cleanLine(line, true);
                        Line lineToAdd = new Line(count, lineNr);
                        Item item = new Item(String.valueOf(count), 1, "", "", "", "", "", "", line);
                        lineToAdd.setItem(item);
                        model.setLine(lineToAdd);

                    }
                    origMdc += line+"\n";
                    cleanedMdc += line+"\n";
                    unicodes += line+"\n";
                    continue;
                }
                else {
                    ++count;
                    line = line.replaceFirst("^\\|[^\\-]*\\-", "");
                    line = line.replaceAll("_*\\-", " ");
                    infosEnded = true;
                }
                line = line.replaceAll(" +", " ");
                
                //send to line to converted to "pure" encoding
                processMdc(line);
                String mdcLineNr = "";
                if (!lineNr.isEmpty()) {
                    mdcLineNr = lineNr+" - ";
                }
                origMdc += mdcLineNr+line.trim()+"\n";
                line = cleanLine(line, true);
                
                //send line to be converted to Unicode
                processUnicode(line, lineNr, count);    
            }
            else {
                if (line.contains("JSesh_page_direction")) {
                    model.setOrientation(getMatch(line, "direction ([^ ]*) ").toLowerCase());
                }
                else if (line.contains("JSesh_page_orientation")) {
                    model.setOrientation(getMatch(line, "orientation ([^ ]*) ").toLowerCase());
                }
            }
        }
    }
    
    
    //convert the encoding to "pure" encoding without control character and transliteration
    private static void processMdc(String line) {
        String tempLine = "", tempMdc;
        line = cleanLine(line, true);
        line = line.replaceAll("\\\\ ", " ");
        line = line.replaceAll("(\\{\\{[^\\}]*\\}\\})|(<[^ ]*)|([^ ]*>)|(\\|[0-9]+)", "");
        line = line.replaceAll("\\.\\.", "\t");
        line = line.replaceAll("[:*&^\\[\\]_'!<>\\.]", " ");
        line = line.replaceAll("([\\(\\)])|(\\$[rb])", "");
        line = line.replaceAll("#[^ ]*", " ");
        line = line.replaceAll("(^[hv]?/{1,2})|( [hv]?/{1,2})", " LACUNA");
        line = line.trim().replaceAll(" +", " ");
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
            mdc = mdc.replaceAll("\\\\R?[0-9l]+", "");
            if (mdcTranslits.containsKey(mdc)) {
                mdc = mdcTranslits.get(mdc);
            }
            if (!tempMdc.equals("")) {
                mdc = tempMdc;
            }
            tempLine += mdc+" ";
        }
        line = tempLine;

        line = line.replaceAll("[*^&:]", " ");
        line = line.replaceAll(" +", " ");

        cleanedMdc += line+"\n";
    }
    
    private static String cleanLine(String line, boolean infosEnded) {
        //if comment
        if (line.startsWith("+")) {
            line = line.replaceAll("\\+s", " ");
            line = line.replaceAll("\\+[libtchgrd]", " ");
            line = line.replaceAll(" +", " ");
            //put the line to model for the json
            //but only if in the beginning of the document
            if (model.getTextName().equals("")) {
                model.setTextName(line);
            }
            else if (!model.getTextName().equals("") && !infosEnded) {
                model.setInfo(line);
            }
            return line.trim();
        }
        //else
        line = line.replaceAll("(\\+[libtchgrd|][^\\+]*\\+s)", "");
        line = line.replaceAll("(\\+s)|(\\+\\+?)", " ");
        line = line.replaceAll(" +", " ");
        return line;
    }
    
    //convert the line to Unicode characters
    private static void processUnicode(String line, String lineNr, int count) {
        //change ** which indicates absolute positioning to *
        //it is almost impossible to know what the placing is
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

        //add the encoded sign groups to a stack
        for (int j=0; j<lineArray.length; j++) {
            mdc = lineArray[j];
            if (!mdc.contains("[") && !mdc.contains("]")) {
                while (mdc.contains("(") && !mdc.contains(")")) {
                    mdc  += "*"+lineArray[++j];
                }
            }
            signStack.add(mdc);
        }
        annotations = new TreeMap<>();

        //start from the last sign group
        while (!signStack.empty()) {
            String shaded;
            mdc = signStack.pop().toString();
            //separate different additions to signs/sign groups and add back to stack
            if (mdc.contains("[") && mdc.contains("]")) {
                //separate the brackets from the word
                mdc = mdc.replaceAll("\\[", " [");
                mdc = mdc.replaceAll("\\] \\{", "]{");
                mdc = mdc.replaceAll("\\[ ([\\[&\\{\"'\\(\\?])", "[$1").trim();
                mdc = mdc.replaceAll("([\\]&\\}\"'\\)\\?]) \\]", "$1]").trim();

                //handle shades, annotations and insertions that consern the bracketed region
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
                //separate the signs and add shade to all if necessary
                mdc = addSpaces(mdc, false);
                if (!endShade.equals("")) {
                    mdc = addShades(mdc, endShade).replaceAll(" +", " ");
                }
                //put signs back to stack
                signStack.addAll(Arrays.asList(mdc.split(" ")));
            }
            //get annotations such as absolute placement, rotation and size
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
            //in red zones each sign is marked
            else if (mdc.equals("$b")) {
               String red = "";
               mdc = signStack.pop().toString();
               while (!mdc.equals("$r")) {
                    red = mdc+"% "+red;
                    mdc = signStack.pop().toString();
               }
                signStack.addAll(Arrays.asList(red.split(" ")));
            }
            //when sign or sign group is followed by shading information #1234, 
            //put the shading for each sign according to its position in the group
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
                mdc = addSpaces(mdc, false);
                //the actual assignment of shading to each sign is done in addShades
                shaded = addShades(mdc, shade);
                signStack.addAll(Arrays.asList(shaded.split(" ")));

            }
            //handle insertion e.g. ligatures and signs placed inside/under etc. of each other
            else if ((mdc.contains("&") || mdc.contains("^")) && (!mdc.matches("\\[[&\\{\\[\"'\\?]") && !mdc.matches("[&\\}\\]\"'\\?]\\]"))) {
                mdc = getInsertions(mdc);
                signStack.addAll(Arrays.asList(mdc.split(" ")));
            }
            //clean signs and sign groups are ready to be added to the final line
            else {
                mdc = addSpaces(mdc, false);
                //reattach annotations to signs if any
                if (!annotations.isEmpty()) {
                    String[] array = mdc.split(" ");
                    String toAdd = "";
                    ArrayList<String> toRemove = new ArrayList<>();
                    for (String arr : array) {
                        if (annotations.containsKey(arr)) {
                            toAdd += arr+" "+annotations.get(arr)+" ";
                            toRemove.add(arr);
                        }
                        else {
                            toAdd += arr+" ";
                        }
                    }
                    for (String rem : toRemove) {
                        annotations.remove(rem);
                    }
                    mdc = toAdd;
                }
                //line in mdc encoding
                
                //get the unicodes for the sign(s)
                String value = getUnicodes(mdc);
                //System.out.println(value);
                String[] returnValue = value.split(";");
                thisChar = returnValue[0];
                if (!value.endsWith(";")) {
                    mdc = returnValue[1];
                }
                mdcLine = mdc+" "+mdcLine;
                //line with Unicode characters with spaces, for the JSON file
                jsonLine = thisChar+" "+jsonLine;
            }
        }
        jsonLine = jsonLine.replaceAll(" +", " ").trim();
        uniLine = jsonLine.replaceAll(" ", "");
        //add the tokens of the line to the JSON model
        if (jsonLine.isEmpty()) {
            Line lineToAdd = new Line(count, lineNr);
            Item item = new Item(String.valueOf(count), 1, "", "", "", "", "", "", "-");
            lineToAdd.setItem(item);
            model.setLine(lineToAdd);
        }
        else {
            addToModel(jsonLine, mdcLine, count, lineNr);
            String uniLineNr = "";
            if (!lineNr.isEmpty()) {
                uniLineNr = lineNr+" ";
            }
            unicodes += uniLineNr+uniLine+"\n";
        }
    }
        
    //if sign has modifiers (size, placing, rotation), store them in an array
    //they are re-added to the sign after Unicodes and pure mdc have been handled
    private static String getAnnotations(String mdc) {
        String divided = mdc.replaceAll("([:\\*&^\\(\\)])", " $1 ");
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
                annotations.put(arrArray[0], arr.replaceAll("^[^ ]* ", "").replaceAll(" ", ""));
                toAdd += arrArray[0];
                //System.out.println(toAdd);
            }
            else {
                toAdd += arr;
            }
        }
        
        return toAdd;
    }
    
    //add information for each sign of the line to the JSON model
    private static void addToModel(String uniLine, String mdcLine, int count, String lineNr) {
        //System.out.println(mdcLine);
        //System.out.println(uniLine);
        Line line;
        Item item;
        boolean red = false;
        mdcLine = mdcLine.replaceAll(" +", " ");
        if (lineNr.isEmpty()) {
            lineNr = "-";
        }

        line = new Line(count, lineNr);
        mdcLine = mdcLine.replaceFirst("^[^-]*- ", "");
        uniLine = uniLine.replaceFirst("^[^\\-]*-", "");  

        //ignore comment lines
        if (!mdcLine.startsWith("+")) {
            uniLine = uniLine.replaceAll(" +", " ");
            mdcLine = mdcLine.replaceAll("\\++[^\\+]\\+s", "");
            String[] mdcArray = mdcLine.split(" ");
            String[] uniArray = uniLine.split(" ");
            int j = 0, itemNr = 0;
            String mdc, uni, encoding, codepoint ="", tsl="";
            Sign sign;
            for (int i=0; i<mdcArray.length; i++) { 
                encoding = mdcArray[i];
                //placing, rotation and size
                if (encoding.startsWith("{{") || encoding.startsWith("€") || encoding.startsWith("\\")) {
                    item = line.getLast(1);
                    
                    if (encoding.startsWith("\\")) {
                        int howMany = 1;
                        while (item.isControlCharacter()) {
                            item = line.getLast(++howMany);
                        }
                        if (encoding.equals("\\")) {
                            item.setReversed();
                        }
                        else if (encoding.contains("R")) {
                            item.setRotation(encoding.replaceFirst("\\\\R", ""));
                        }
                        line.deleteLast();
                        line.setItem(item);
                        //continue;
                    }
                    else {
                        //absolute placing
                        if (encoding.startsWith("{{")) {
                            item.setPlacing(encoding);
                        }
                        //rotation and size
                        else {

                            encoding = encoding.replace("€", " ");
                            String[] encArray = encoding.split(" ");
                            for (String code : encArray) {
                                if (code.contains("R")) {
                                    if (mdcTranslits.containsKey(item.getMdc()+""+code)) {
                                        item.setMdc(mdcTranslits.get(item.getMdc()+""+code));
                                        item.setRotation(code);
                                        item.setUnicode(signs.get(item.getMdc()).getUni());
                                    }
                                    else {
                                        item.setRotation(code);
                                    }
                                    //j++;
                                }
                                else if (code.equals("l")) {
                                    item.setLarger();
                                }
                                else {
                                    item.setSize(code);
                                }
                            }

                        }
                        line.deleteLast();
                        line.setItem(item);
                        continue;
                    }
                }
                
                //sometimes the line division is not the same as in the original document
                //keep track of original line changes marked with |Nr
                if (encoding.matches("\\|[0-9]+")) {
                    if (line.getLineName().equals("-")) {
                        line.setLineName(encoding);
                    }
                    latestLine = encoding;
                    j++;
                }
                else {
                    itemNr++;
                    //signs in red zone have been marked with %
                    if (encoding.contains("%")) {
                        encoding = encoding.replaceAll("%", "");
                        red = true;
                    }
                    /*if (encoding.startsWith("\\")) {
                        Sign thisSign;
                        if (encoding.equals("\\")) {
                            thisSign = signs.get("<->");
                            item = new Item(String.valueOf(count), itemNr++, latestLine, "\\", "\\", thisSign.getUni(), "", thisSign.getCodepoint(), "");
                            item.setControlCharacter("Yes");
                            item.setReversed();
                            line.setItem(item);
                            //uni = uniArray[j++];
                            
                        }
                    }*/
                    if (mdcTranslits.containsKey(encoding)) {
                        mdc = mdcTranslits.get(encoding);
                    }
                    else if (!signs.containsKey(encoding)) {
                        //if encoding not found in the list, check without letter(s) at the end
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
                    if (lineNr.equals("-")) {
                        lineNr = String.valueOf(count);
                    }
                    uni = uniArray[j];
                    if (uni.matches("[a-zA-Z]+")) {
                        uni = "-";
                    }
                    codepoint = "-";
                    tsl = "";
                    /*Sign thisSign;
                    for (int y=90; y<=270; y+=90) {
                        thisSign = signs.get("\\R"+y);
                        if (uni.equals(thisSign.getUni())) {
                            item = new Item(String.valueOf(count), itemNr++, latestLine, thisSign.getMdc(), thisSign.getMdc(), uni, "", thisSign.getCodepoint(), "");
                            item.setControlCharacter("Yes");
                            item.setRotation(thisSign.getMdc());
                            line.setItem(item);
                            uni = uniArray[j++];
                            System.out.println(thisSign.getMdc()+"\t"+String.valueOf(count)+"\t"+itemNr);
                        }
                        
                    }
                    thisSign = signs.get("<->");
                    if (uni.equals(thisSign.getUni())) {
                        item = new Item(String.valueOf(count), itemNr++, latestLine, "\\", "\\", uni, "", thisSign.getCodepoint(), "");
                        item.setControlCharacter("Yes");
                        item.setReversed();
                        line.setItem(item);
                        uni = uniArray[j++];
                        System.out.println("\\");
                    }*/
                    //System.out.println(mdc+"\t"+uni);
                    if (signs.containsKey(mdc)) {
                        sign = signs.get(mdc);
                        codepoint = sign.getCodepoint();
                        tsl = sign.getTsl();
                        if (encoding.matches("\\\\")) {
                            encoding = sign.getMdc();
                            mdc = sign.getMdc();
                        }
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
                    
                    item = new Item(String.valueOf(count), itemNr, latestLine, encoding, mdc, uni, tsl, codepoint, "");
                    if (codepoint.startsWith("1343") || codepoint.startsWith("1344") || codepoint.startsWith("1345")) {
                        item.setControlCharacter();
                    }
                    if (i<mdcArray.length-1 && mdcArray[i+1].startsWith("#")) {
                        item.setShading("YES");
                    }
                    if (red || encoding.equals("o")) {
                        item.setColor("red");
                    }
                    line.setItem(item);
                    j++;
                }
            }
            model.setLine(line);
        }
    }
    
    //separate the encodings of a sign group
    private static String addSpaces(String mdc, boolean original) {
        if (!mdc.startsWith("|(")) {
            mdc = mdc.replaceAll("([:\\*&\\^\\)\\(])", " $1 ");
        }
        if (original) {
            mdc = mdc.replaceAll("\\\\", " €");
        }
        mdc = mdc.replaceAll(" +", " ");
        return mdc.trim();
    }
    
    //handle the shading which in Unicode is added after each sign separately
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
                //only works on groups with one colon and/or asterisk
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
                    if (thisIndex > mdc.indexOf(")") && mdc.contains(")")) {
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
        String toReturn = "";

        //ligatures in JSesh
        toReturn = getJseshLigature(mdc);
        if (!toReturn.equals("")) {
            return toReturn;
        }

        String devided = addSpaces(mdc, false);
        
        devided = devided.replaceAll("(\\[+)", " $1 ").trim();
        devided = devided.replaceAll("(\\]+)", " $1 ").trim();
        devided = devided.replaceAll(" +", " ");
        String[] mdcArray =  devided.split(" ");
        
        String thisMdc, pos ="", toAdd, mdc1 = "", mdc2, mdcForSign1, mdcForSign2;
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
        mdc2 = "";
        
        int withInsert = 0;
        for (int i=1; i<mdcArray.length; i+=2) {
            String toDo1 = insert[i-1];
            String toDo2 = insert[i+1];
            toAdd = toAdd.replaceAll(" +", " ");
            mdcForSign1 = "";
            mdcForSign2 = "";
            if (withInsert !=2) {
                mdc1 = mdcArray[i-1];
            }
            else {
                
            }
            mdcForSign1 = isTranslit(mdc1);
            if (mdcForSign1.isEmpty()) {
                mdcForSign1 = mdc1;
            }
            if (mdc1.equals("(")) {
                mdc1 = getGroupInsertion(mdc1, mdcArray, i-2);
                i+=mdc1.replaceAll(" +", " ").split(" ").length-1;
                toDo2 = insert[i+1];
            }
            withInsert = 0;
            mdc2 = "";
            //get previous sign
            
            sign1 = signs.get(mdcForSign1);
            //get the following sign
            if (i<mdcArray.length-1) {
                mdc2 = mdcArray[i+1];
                if (mdc2.equals("(")) {
                    mdc2 = getGroupInsertion(mdc2, mdcArray, i);
                }
                mdcForSign2 = isTranslit(mdc2);
                if (mdcForSign2.isEmpty()) {
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
            //find the position using the possible places for this sign, then mark the insertion in the group
            else if (thisMdc.equals("&") || thisMdc.equals("^")) {
                if (thisMdc.equals("^")) {
                    if (sign2 != null) {
                        pos = sign2.getBegin();
                        
                        if (pos.equals("")) {
                            pos = sign2.getTop();
                        }
                        else {
                            withInsert = 2;
                        }
                        if (pos.equals("")) {
                            pos = sign2.getBottom();
                        }
                        else {
                            withInsert = 2;
                        }
                    }       
                    if (pos.equals("") && sign1 != null) {        
                        pos = sign1.getEnd();
                        if (pos.equals("")) {
                            pos = sign1.getBottom();
                        }
                        if (pos.equals("")) {
                            pos = sign1.getTop();
                        }

                        if (pos.equals("")) {
                            pos = "*";
                        }
                    }
                    if (withInsert == 2) {
                        String temp = mdc1;
                        mdc1 = mdc2;
                        mdc2 = temp;
                        if (!toAdd.equals("") && toAdd.split(" ").length == i) {
                            String[] toAddArray = toAdd.split(" ");
                            toAdd = "";
                            for (int k=0; k<toAddArray.length-1; k++) {
                                toAdd += toAddArray[k]+" ";
                            }
                        }
                    }
                    if (!toAdd.equals("") && toAdd.split(" ").length == i && withInsert != 2) {
                            toAdd += " "+pos+" "+mdc2+" ";
                    }
                    else {
                        toAdd += mdc1+" "+pos+" "+mdc2+" ";
                        
                    }
                }
                else if (thisMdc.equals("&")) {
                    
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
                        if (pos.equals("")) {
                            pos = sign1.getBegin();
                        }
                        if (pos.equals("") && !annotations.isEmpty()) {
                            toAdd += mdc1+"##"+mdc2;
                            i++;
                            continue;
                        }
                        if (pos.equals("") && sign2 != null) {
                            pos = sign2.getBegin();
                            if (!pos.equals("")) {
                                withInsert = 2;
                                String[] toAddArray = toAdd.split(" ");
                                if (toAdd.length()>0 && toAddArray.length == i) {
                                    if (insert[i-2].equals("^")) {
                                        toAdd += pos+" "+mdc2;
                                    }
                                }
                                else {
                                    toAdd += mdc2+" "+pos+" "+mdc1+" ";
                                }
                               // i++;
                                continue;
                            }
                            
                        }
                        if (pos.equals("")) {
                            pos = sign1.getBegin();
                        }
                        if (withInsert == 2) {
                            String temp = mdc1;
                            mdc1 = mdc2;
                            mdc2 = temp;
                            

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
                        if (!pos.equals("")) {
                            withInsert = 2;
                        }
                        if (!toAdd.equals("") && toAdd.split(" ").length == i) {
                            toAdd += pos+" "+mdc2+" ";
                        }
                        else {
                            
                            toAdd += mdc2+" "+pos+" "+mdc1+" ";
                            withInsert = 2;
                            String temp = mdc1;
                            mdc1 = mdc2;
                            mdc2 = temp;
                            
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
            if (mdc2.endsWith(")")) {
                i+=mdc2.replaceAll(" +", " ").split(" ").length-1;
            }
        }
        return toAdd.replaceAll(" +", " ").trim();
    }
    
    private static String getGroupInsertion(String mdc1, String[] mdcArray, int i) {
        if (mdc1.equals("(")) {
            int count = 1;
            i++;
            while (count != 0) {
                String join = mdcArray[++i];
                mdc1 += join;
                if (join.equals("(")) {
                    count++;
                }
                else if (join.equals(")")) {
                    count--;
                }
                if (count == 0) {
                    if (mdc1.matches(".*[\\&\\^].*")) {
                        mdc1 = mdc1.replaceAll("\\)$", "");
                        mdc1 = "( "+getInsertions(mdc1.replaceAll("^\\(", ""))+" )";
                    }
                    else {
                        mdc1 = mdc1.replaceAll("([\\*&\\(\\):])", " $1 ").trim();
                    }
                }

            }

        }
        return mdc1;
    }
    
    private static String getJseshLigature(String mdc) {
        String[] mdcArray = mdc.split("&");
        String toReturn ="";
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
        else if (mdc.equals("D&(md*md*md)") || mdc.equals("I10&(md*md*md)") || mdc.equals("D&(S43*S43*S43)") || mdc.equals("I10&(S43*S43*S43)")) {
            toReturn = mdcArray[0]+" bottomStart "+mdcArray[1];
        }
        return toReturn;
    }
    
    //Check if the encoding is in the list of translit > encoding
    private static String isTranslit(String mdc) {
        if (mdcTranslits.containsKey(mdc)) {
            return mdcTranslits.get(mdc);
        }
        return mdc;
    }
    
    //get the Unicode sign for the given encoding
    //the conversion of codepoint to character is done in Sign.java
    private static String getUniForSign(String mdc) {
        if (signs.containsKey(mdc)) {
                Sign sign = signs.get(mdc);
                return sign.getUni();
            }
        return "";
    }
    
    //get the Unicodes for the sign or sign group
    private static String getUnicodes(String mdc) {
        String thisChar = "", uni, translit, newMdc = "";
        //remove parentheses
        mdc = mdc.replaceAll("(\\{\\{[^\\}]+\\}\\})|(%)", "");
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
            if (uni.equals("") && thisMdc.contains("€")) {
                String token = thisMdc.replaceAll("€", " €");
                //System.out.println(thisChar+"\t"+token);
                String[] array = token.split(" ");
                String uniAnno;
                for (int i=1; i<array.length; i++) {
                    String anno = array[i].replaceAll("€", "\\\\");
                    if (anno.matches("\\\\")) {
                        uni += " "+getUniForSign("<->");
                        newMdc += anno+" ";
                    }
                    if (anno.matches("\\\\R[0-9]+")) {
                        String uniRot = getUniForSign(anno);
                        if (!uni.equals("")) {
                            if (!uniRot.equals("")) {
                                uni += " "+uniRot;
                            }
                        }
                        else {
                            uni += " "+uniRot;
                        }
                        newMdc += anno+" ";
                    }
                    else {
                        thisMdc = "";
                        newMdc += array[i]+" ";
                    }
                }
            }
            else {
                newMdc += thisMdc+" ";
            }
            if (!uni.isEmpty()) {
                
                thisChar += " "+uni;
                //System.out.println(thisChar);
            }
            else {
                
                thisChar += " "+thisMdc;
                
            }
        }
        return thisChar+";"+newMdc.trim();
    }
    
    private static void saveToFile(String toFile, String dir, String filename) throws IOException {
        BufferedWriter writer = null;
        File file = new File(dir+""+filename);
        try {
            writer = new BufferedWriter(new FileWriter(file, false));
            writer.write(toFile);
        }
        catch (Exception e) {
            System.out.println("Error while creating writer: "+e.getMessage());
        }
        finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }
    
    //read the text from the file
    private static String readBytes(String filename) throws IOException {
        BufferedReader reader = null;
        String text = "";
        try {
            reader= new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                text+= line+"\n";
            }
        }
        catch (Exception e) {
            System.out.println("Trying to read text: "+e);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return text;
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
            System.out.println("Trying to read unicodes: "+e);
        }
        finally {
            if (is != null) {
                reader.close();
                is.close();
            }
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
            System.out.println("Trying to read TSL: "+e);
        }
        finally {
            if (is != null) {
                reader.close();
                is.close();
            }
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
            System.out.println("Trying to read translit2mdc: "+e);
        }
        finally {
            if (i != null) {
                reader.close();
                i.close();
            }
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
            System.out.println("Trying to read insertions: "+ex);
        }
        finally {
            reader.close();
            is.close();
        }
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, FontFormatException {
        dirToOpen = "~/";
        currentFont = readFont("/Aegyptus.otf");
        readUnicode("/mdc2uni.txt");
        readMdc("/translit2mdc.txt");
        readTSL("/signtsl.txt");
        readInsertions("/myIns.txt");
        //FlatLaf.setup();

// create UI here...
        try {
            //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel( new FlatLightLaf() );
            UIManager.put( "ScrollBar.width", 14 );
            UIManager.put( "TabbedPane.showTabSeparators", true );
            UIManager.put( "TabbedPane.selectedBackground", Color.white );
            UIManager.put( "TabbedPane.background", Color.white );
            UIManager.put( "TabbedPane.inactiveUnderlineColor", Color.white );
            UIManager.put( "TabbedPane.underlineColor", Color.white );
            UIManager.put( "TextPane.margin", new Insets( 105, 25, 25, 25 ) );
            UIManager.put( "Label.background", new Color( 0xe1e1e1 ) );
            UIManager.put( "TabbedPane.selectionFollowsFocus", true );
            UIManager.put( "TitlePane.borderColor", new Color( 0xe1e1e1) );
            UIManager.put( "TitlePane.showIcon", false );
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                Gly2mdc2 gly2mdc2 = new Gly2mdc2();
            } catch (IOException ex) {
                Logger.getLogger(Gly2mdc2.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
    }
    
    //Rounded borders for 
    //FROM: https://stackoverflow.com/questions/423950/rounded-swing-jbutton-using-java
    private static class RoundedBorder implements Border {
        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }
}
