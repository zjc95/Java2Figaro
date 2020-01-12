package Trans.demo;

import Trans.StaticUtils.StaticParser;

public class demo {

    public static void main(String[] args) {
        String srcFile = System.getProperty("user.dir") + "\\resources\\test.java";
        StaticParser.Analyze(srcFile);
    }




}
