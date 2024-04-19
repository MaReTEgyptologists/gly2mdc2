/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import java.util.ArrayList;

/**
 *
 * @author hwikgren
 */
public class Item {
    
    //private ArrayList<Interpretation> interpretations;
    private String line;
    private int itemNr;
    private String encoding;
    private String mdc;
    private String unicode;
    private String codepoint;
    private String tsl;
    private String shading;
    private String placing;
    private String insertion;
    private String size;
    private String rotation;
    private String controlCharacter;
    
    public Item(String line, int nr, String encoding, String mdc, String uni, String tsl, String codepoint) {
        this.line = line;
        this.itemNr = nr;
        this.encoding = encoding;
        if (!mdc.matches("[:\\*&^\\(\\)\\[\\]']")) {
            this.mdc = mdc;
        }
        this.unicode = uni;
        this.codepoint = codepoint;
        //System.out.println(codepoint);
        if (tsl != null && !tsl.equals("")) {
            this.tsl = "https://thotsignlist.org/mysign?id="+tsl;
        }
        this.shading = "NO";
    }
    
    

    /*public void setInterpret(Interpretation interpret) {
        this.interpretations.add(interpret);
    }*/

    public void setUnicode(String unicode) {
        this.unicode = unicode;
    }

    public void setShading(String shading) {
        this.shading = shading;
    }

    public void setMdc(String mdc) {
        this.mdc = mdc;
    }

    public void setPlacing(String placing) {
        this.placing = placing;
    }

    public void setInsertion(String insertion) {
        this.insertion = insertion;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    public void setControlCharacter(String controlCharacter) {
        this.controlCharacter = controlCharacter;
    }

    public String getMdc() {
        return mdc;
    }

    public String getShading() {
        return shading;
    }
    
    
    
    
}
