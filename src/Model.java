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
public class Model {
    private String textName, modelSource, creator, organization, fundedBy, repository;
    private ArrayList<String> orientation;
    private ArrayList<String> infos;
    private ArrayList<Line> lines;
    

    public Model() {
        this.textName = "";
        /*this.modelSource = "";
        this.creator = "";
        this.organization = "";
        this.fundedBy = "";
        this.repository = "";*/
        //this.items = new ArrayList<>();
    }

    
    public Model(String name) {
        this.textName = name;
        this.modelSource = "";
        this.creator = "";
        this.organization = "";
        this.fundedBy = "";
        this.repository = "";
        this.lines = new ArrayList<>();
    }

    public void setTextName(String textName) {
        this.textName = textName;
    }
    
    public void setOrientation(String orientation) {
        if (this.orientation == null) {
            this.orientation = new ArrayList<>();
        }
        this.orientation.add(orientation);
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setInfo(String info) {
        if (this.infos == null) {
            this.infos = new ArrayList<>();
        }
        infos.add(info);
    }

    public void setLine(Line line) {
        if (this.lines == null) {
            this.lines = new ArrayList<>();
        }
        this.lines.add(line);
    }

    public String getTextName() {
        return this.textName;
    }

    public String getCreator() {
        return this.creator;
    }
    
    public Line getLast() {
        return this.lines.get(lines.size()-1);
    }
    
    public void deleteLast() {
        this.lines.remove(lines.size()-1);
    }
}
