/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.util.ArrayList;

/**
 *
 * @author hwikgren
 */
public class Model {
    private String textName, modelSource, creator, organization, fundedBy, repository;
    private ArrayList<String> infos;
    private ArrayList<Item> items;
    

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
        this.items = new ArrayList<>();
    }

    public void setTextName(String textName) {
        this.textName = textName;
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

    public void setItem(Item item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    public String getTextName() {
        return this.textName;
    }

    public String getCreator() {
        return this.creator;
    }
    
    public Item getLast() {
        return this.items.get(items.size()-1);
    }
    
    public void deleteLast() {
        this.items.remove(items.size()-1);
    }
}
