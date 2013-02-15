package liujing.util;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;

public class CSVParser{

    /** reader */
    private BufferedReader input;

    private CSVLexer lexer;

    public CSVParser(Reader r)throws IOException{
        lexer = new CSVLexer(r);
    }



    protected static class CSVLexer{
        /** input */
        private BufferedReader input;
        /** current token */
        private String token;
        /** current char */
        private int chr;
        /**
        @param in reader
        */
        public CSVLexer(Reader in)throws IOException{
            input = new BufferedReader(in);
            chr = input.read();
        }

        /**
        @return token text
        */
        public String LT(){
            return token;
        }

        /**
        consume
        */
        public void consume(){
            parse();
        }

        private void parse(){
            if(chr == -1){
                token = null;
            }else if(chr == ','){
                token = "";
            }else if(chr == '"'){
                quote();
            }else{
                value();
            }
        }

        private void quote(){
        }

        private void value(){

        }
    }


}
