/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author hwikgren
 */
public class Sign {
    private String mdc;
    private String codepoint;
    private String translit;
    private String tsl;
    private String start;
    private String end;
    private String top;
    private String bottom;
    private String middle;
    private String unicode;
    private boolean insert;

    public Sign(String mdc, String uni) {
        this.mdc = mdc;
        this.codepoint = uni;
        unicode = "";
        makeUnicode();
        this.start = "";
        this.end = "";
        this.top = "";
        this.bottom = "";
        this.middle = "";
        this.insert = false;
    }

    public void setTranslit(String translit) {
        this.translit = translit;
    }

    public void setTsl(String tsl) {
        this.tsl = tsl;
    }

    public void setInsertions(String start, String end, String middle, String top, String bottom) {
        //System.out.println(this.mdc+"\t"+end);
        this.start = start;
        this.end = end;
        this.middle = middle;
        this.top = top;
        this.bottom = bottom;
        this.insert = true;
    }

    public String getMdc() {
        return mdc;
    }

    public String getUni() {
        return unicode;
    }

    public String getTsl() {
        return tsl;
    }
    

    public String getBegin() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getMiddle() {
        return middle;
    }

    public String getBottom() {
        return bottom;
    }

    public String getTop() {
        return top;
    }

    public String getCodepoint() {
        return codepoint;
    }

    public boolean getInsert() {
        return insert;
    }
    
    private void makeUnicode() {
        int code;
        String[] uniArray = codepoint.split(" \\+ ");
        for (String unity : uniArray) {
            code = Integer.parseInt(unity, 16);
            unicode += Character.toString(code);
        }
    }
    
}
