import java.io.*;
import java.util.*;

public class Assembler {

    static Map<String, String> comp = new HashMap<>();
    static Map<String, String> dest = new HashMap<>();
    static Map<String, String> jump = new HashMap<>();
    static Map<String, Integer> symbolTable = new HashMap<>();

    static {
        comp.put("0","0101010"); comp.put("1","0111111"); comp.put("-1","0111010");
        comp.put("D","0001100"); comp.put("A","0110000"); comp.put("!D","0001101");
        comp.put("!A","0110001"); comp.put("-D","0001111"); comp.put("-A","0110011");
        comp.put("D+1","0011111"); comp.put("A+1","0110111"); comp.put("D-1","0001110");
        comp.put("A-1","0110010"); comp.put("D+A","0000010"); comp.put("D-A","0010011");
        comp.put("A-D","0000111"); comp.put("D&A","0000000"); comp.put("D|A","0010101");
        comp.put("M","1110000"); comp.put("!M","1110001"); comp.put("-M","1110011");
        comp.put("M+1","1110111"); comp.put("M-1","1110010"); comp.put("D+M","1000010");
        comp.put("D-M","1010011"); comp.put("M-D","1000111"); comp.put("D&M","1000000");
        comp.put("D|M","1010101");

        dest.put("", "000"); dest.put("M","001"); dest.put("D","010");
        dest.put("MD","011"); dest.put("A","100"); dest.put("AM","101");
        dest.put("AD","110"); dest.put("AMD","111");

        jump.put("", "000"); jump.put("JGT","001"); jump.put("JEQ","010");
        jump.put("JGE","011"); jump.put("JLT","100"); jump.put("JNE","101");
        jump.put("JLE","110"); jump.put("JMP","111");

        symbolTable.put("SP",0); symbolTable.put("LCL",1); symbolTable.put("ARG",2);
        symbolTable.put("THIS",3); symbolTable.put("THAT",4);
        symbolTable.put("SCREEN",16384); symbolTable.put("KBD",24576);

        for(int i=0;i<16;i++) symbolTable.put("R"+i,i);
    }

    static String toBinary(int n){
        return String.format("%16s", Integer.toBinaryString(n)).replace(' ', '0');
    }

    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader("fulltest.asm"));
        List<String> lines = new ArrayList<>();

        String line;

        // Remove comments and blank lines
        while((line = br.readLine()) != null){
            line = line.split("//")[0].trim();
            if(!line.isEmpty()) lines.add(line);
        }

        // PASS 1: Handle labels
        int rom = 0;
        List<String> instructions = new ArrayList<>();

        for(String l : lines){
            if(l.startsWith("(")){
                String label = l.substring(1, l.length()-1);
                symbolTable.put(label, rom);
            } else {
                instructions.add(l);
                rom++;
            }
        }

        // PASS 2: Translation
        int ram = 16;
        PrintWriter out = new PrintWriter("output.hack");

        for(String l : instructions){

            if(l.startsWith("@")){
                String sym = l.substring(1);
                int val;

                if(sym.matches("\\d+")){
                    val = Integer.parseInt(sym);
                } else {
                    if(!symbolTable.containsKey(sym)){
                        symbolTable.put(sym, ram++);
                    }
                    val = symbolTable.get(sym);
                }

                out.println(toBinary(val));
            }
            else {
                String d="", c="", j="";

                if(l.contains("=")){
                    String[] parts = l.split("=");
                    d = parts[0];
                    l = parts[1];
                }

                if(l.contains(";")){
                    String[] parts = l.split(";");
                    c = parts[0];
                    j = parts[1];
                } else {
                    c = l;
                }

                out.println("111" + comp.get(c) + dest.get(d) + jump.get(j));
            }
        }

        out.close();
        System.out.println("Conversion completed! Output written to output.hack");
    }
}
