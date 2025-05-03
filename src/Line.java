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

public class Line {
    private int lineNr;
    private String lineName;
    ArrayList<Item> items;

    public Line(int number, String lineName) {
        this.lineNr = number;
        this.lineName = lineName;
        this.items = new ArrayList<>();
    }

    
    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItem(Item item) {
        this.items.add(item);
    }
    
    public void setLineName(String name) {
        this.lineName = name;
    }
    
    public String getLineName() {
        return this.lineName;
    }
    
    public Item getLast(int howMany) {
        return this.items.get(items.size()-howMany);
    }
    
    public void deleteLast() {
        this.items.remove(items.size()-1);
    }
    
    public String getBeginning() {
        String beginning = "";
        for (Item item : items) {
            beginning += item.getMdc()+" ";
        }
        return beginning.trim();
    }
}
