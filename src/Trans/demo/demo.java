package Trans.demo;
public class demo {

    public static void main(String[] args) {
        String srcFile = System.getProperty("user.dir") + "\\resources\\test.java";
        JavaParser.Analyze(srcFile);
    }




}
