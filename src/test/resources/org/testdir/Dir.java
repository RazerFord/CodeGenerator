package org.testdir;

import java.util.ArrayList;
import java.util.List;

public class Dir {
    public List<String> name = new ArrayList<>();
    public int i = 10;

    public void add(String file) {
        name.add(file);
    }

    public List<String> read() {
        return name;
    }
}
