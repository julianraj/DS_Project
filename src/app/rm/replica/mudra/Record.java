/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.rm.replica.mudra;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class Record implements Serializable {
    public String id;
    public String bookedBy;

    public Record() {
    }

    public Record(String id, String bookedBy) {
        this.id = id;
        this.bookedBy = bookedBy;
    }

    public boolean patternMatcher(String s, String compile) {
        Pattern p = Pattern.compile(compile);
        Matcher m = p.matcher(s);
        return m.matches();
    }
}