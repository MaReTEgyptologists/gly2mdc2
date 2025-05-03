/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.marete.gly2mdc2;

import java.util.ArrayList;

/**
 *
 * @author hwikgren
 */
public class Item {
    
    //private ArrayList<Interpretation> interpretations;
    //private int line;
    private String line;
    private int itemNr;
    private String originalLine;
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
    private Boolean larger;
    private Boolean reversed;
    private Boolean controlCharacter;
    private String comment;
    private String color;
    
    public Item(String lineName, int nr, String origLine, String encoding, String mdc, String uni, String tsl, String codepoint, String comment) {
        //this.line = line;
        this.line = lineName;
        this.itemNr = nr;
        if (comment.isEmpty()) {
            this.encoding = encoding;
            if (!mdc.matches("[:\\*&^\\(\\)\\[\\]']")) {
                this.mdc = mdc;
            }
            if (!origLine.isEmpty()) {
                this.originalLine = origLine;
            }
            this.unicode = uni;
            this.codepoint = codepoint;
            //System.out.println(codepoint);
            if (tsl != null && !tsl.equals("")) {
                this.tsl = "https://thotsignlist.org/mysign?id="+tsl;
            }
            if (!codepoint.matches("134[345].")) {
                this.shading = "NO";
            }
        }
        else {
            if (comment.equals("-")) {
                comment = "";
            }
            this.comment = comment;
        }
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

    /*public void setInterpret(Interpretation interpret) {
        this.interpretations.add(interpret);
    }*/

    public void setUnicode(String unicode) {
        this.unicode = unicode;
    }

    public void setShading(String shading) {
        if (!this.codepoint.matches("134[345].")) {
                this.shading = shading;
            }
        //this.shading = shading;
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
    
    public void setLarger() {
        this.larger = true;
    }
    
    public void setReversed() {
        this.larger = true;
    }

    public void setControlCharacter() {
        this.controlCharacter = true;
    }
    
    public void setColor(String red) {
        this.color = red;
    }

    public String getMdc() {
        return mdc;
    }

    public String getShading() {
        return shading;
    }
    
    public Boolean isControlCharacter() {
        if (this.controlCharacter != null) {
            return true;
        }
        else {
            return false;
        }
    }
    
    
}
