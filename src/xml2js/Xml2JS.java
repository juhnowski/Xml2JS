package xml2js;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ilya Juhnowski
 */
public class Xml2JS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length==0){
            System.out.println("Usage: java -jar Xml2JS.jar <dict_name>.xml");
        }
        boolean isBodySplitterCheck = true;
        String bodySplitter = "";
        
        File argFileName = new File(args[0]);
        String filenameWithExtension = argFileName.getName();
        
        String[] ar = filenameWithExtension.split("\\.");
        String name = ar[0];
        String tableName = name.toUpperCase();
        File f = new File(name + ".html");
        try {
            f.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Xml2JS.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (
                FileInputStream inputStream = new FileInputStream(name+".xml");
                Scanner sc = new Scanner(inputStream, "UTF-8");
                OutputStream fos = new FileOutputStream(name + ".html");
                OutputStreamWriter osr = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
                BufferedWriter bw = new BufferedWriter(osr);) {
            StringBuilder sb = new StringBuilder();
            String class_wrapper
                    = "<!DOCTYPE HTML><html><head><script type=\"text/javascript\">\n"
                    + "         var db = openDatabase('mydb', '1.0', 'Test DB', 2 * 1024 * 1024);\n"
                    + "         var msg;\n"
                    + "         db.transaction(function (tx) {tx.executeSql('DROP TABLE "+tableName+"');});"
                    + "         db.transaction(function (tx) {\n"
                    + "            tx.executeSql('CREATE TABLE IF NOT EXISTS "+tableName+" (key unique, val)');});";
            sb.append(class_wrapper);

            int i = 0;
            while (sc.hasNextLine()) {

                String line = sc.nextLine();

                if (line.contains("<H1>")) {
                    i++;
                    String[] sarray = line.split("</key2></h><body>");
                    String[] warray = sarray[0].split("</key1><key2>");
                    
                    String word = warray[1].replaceAll("'", "&acute;");;
                    if (word.length() > 0) {
                        /**
                         * Following fixed ap90.xml where splitter is just <body>
                         */
                        if (isBodySplitterCheck){
                            if (line.contains("<body>.")) {
                                bodySplitter = "<body>.";
                            } else {
                                bodySplitter = "<body";
                            }
                        }
                        String s2 = "";
                        try {
                            String[] s0 = line.split(bodySplitter);
                            
                            if(bodySplitter == "<body") {
                                String tmp = s0[1].substring(s0[1].indexOf(">")+1);
                                s0[1] = tmp;
                            }
                            String[] s1 = s0[1].split("</body>");
                            s2 = s1[0].replaceAll("'", "&acute;");
                        } catch (ArrayIndexOutOfBoundsException e){
                            System.out.println("line with error: " + line + "\n");
                        }        
                            String s3 = s2.replaceAll("<s>", "<b>");
                            String body = s3.replaceAll("</s>", "</b>");
                
                        sb.append("db.transaction(function (tx) {\n");
                        sb.append("tx.executeSql('INSERT INTO "+tableName+" (key, val) VALUES (\""+word+"\", \""+body+"\")');});");
                        bw.append(sb.toString());
                        bw.newLine();
                        bw.flush();
                        sb = new StringBuilder();
                    }

                }
            }
            sb.append("\n"
                    + "         db.transaction(function (tx) {\n"
                    + "            tx.executeSql('SELECT * FROM "+tableName+"', [], function (tx, results) {\n"
                    + "               var len = results.rows.length, i;\n"
                    + "               msg = \"<p>Found rows: \" + len + \"</p>\";\n"
                    + "               document.querySelector('#status').innerHTML +=  msg;\n"
                    + "\n"
                    + "               for (i = 0; i < len; i++){\n"
                    + "                  msg = \"<p><b>\" + results.rows.item(i).key + \"</b></p><br>\" + results.rows.item(i).val + \"<br>\";\n"
                    + "                  document.querySelector('#status').innerHTML +=  msg;\n"
                    + "               }\n"
                    + "            }, null);\n"
                    + "         });\n"
                    + "      </script>\n"
                    + "   </head>\n"
                    + "   <body>\n"
                    + "      <div id=\"status\" name=\"status\">Status Message</div>\n"
                    + "   </body>\n"
                    + "</html>");
            bw.append(sb.toString());
            bw.newLine();
            osr.flush();
            System.out.println("Has been added: " + i + " words.");

            if (sc.ioException() != null) {
                throw sc.ioException();
            }

        } catch (IOException ex) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("Please check that "+name+".xml exist");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Logger.getLogger(Xml2JS.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
