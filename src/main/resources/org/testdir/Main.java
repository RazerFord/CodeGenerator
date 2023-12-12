package org.testdir;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Dir dir = new Dir();

        dir.add("file2");
        dir.add("file3");
        dir.add("file4");

        dir.read();

        List<String> mmm = dir.read();
        mmm = dir.name;
    }
}
