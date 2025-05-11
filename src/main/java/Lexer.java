import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Lexer {
  Map<String, TokenType> keywords = new HashMap<>();
  private int line;
  private int pos;
  private int position;
  private char chr;
  private String s;

  /**
   * Constructor for lexer.
   * @param source is the input.
   */
  Lexer(String source) {
    this.line = 1;
    this.pos = 0;
    this.position = 0;
    this.s = source;
    this.chr = this.s.charAt(0);
    this.keywords.put("if", TokenType.Keyword_if);
    this.keywords.put("else", TokenType.Keyword_else);
    this.keywords.put("print", TokenType.Keyword_print);
    this.keywords.put("putc", TokenType.Keyword_putc);
    this.keywords.put("while", TokenType.Keyword_while);// These are all of the Keyword tokentypes, can check against
    // this Hashmap to see if a string matches any of them

  }


  /**
   * Function to flag an error in the input file, showing the line and position at which it occurred
   * as well as a pre-defined message passed in as a parameter.
   * @param line is the line where the error occurred.
   * @param pos is the specific position on the line where the error occurred.
   * @param msg is the message notifying what the error was.
   */
  static void error(int line, int pos, String msg) {
    if (line > 0 && pos > 0) {
      System.out.printf("%s in line %d, pos %d\n", msg, line, pos);
    } else {
      System.out.println(msg);
    }
    System.exit(1);
  }

  /**
   * A function to write the output into a new file, which will be a .lex file.
   * @param result is the output to be written into the file.
   * @param filename is a parameter I added so that the output file would not be hardcoded,
   *                 and could be switched as additional input files were looped through.
   */
  static void outputToFile(String result, String filename) {
    try {
      FileWriter myWriter = new FileWriter("src/main/resources/" +
              filename.substring(0,filename.lastIndexOf('.')) + "/" + filename);
      // If we don't include the path above it puts the output in the project directory but outside of src
      myWriter.write(result);
      myWriter.close();
      System.out.println("Successfully wrote to the file.");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {

    if (1 == 1) {
      File folder = new File("src/main/resources/");
      processDirectory(folder);// Moved the processing logic to the processDirectory function.
      // This way we can process folders and files recursively.
      // A possible alternative would be to have one input folder and only search through files in that location.
    } else {
      error(-1, -1, "No args");
    }
  }

  /**
   *
   * @param fName
   */
  static void processDirectory(File fName){
    File[] listOfFiles = fName.listFiles();

    for(File file : listOfFiles) {
      if(file.isDirectory()) {
        processDirectory(file);
      }
      else{
        try {// Processing logic from main()
          String fileName = file.getName().toLowerCase();
          if(!(fileName.endsWith(".lex") || fileName.endsWith(".par"))) {
            Scanner s = new Scanner(file);
            String source = " ";
            String result = " ";
            while (s.hasNext()) {
              source += s.nextLine() + "\n";
            }
            Lexer l = new Lexer(source);
            result = l.printTokens();
            String newFname = fileName.substring(0,fileName.lastIndexOf('.')) + ".lex";
            // Removes previous file extension and adds ".lex" instead to use for output file.
            outputToFile(result, newFname);
          }
      } catch (FileNotFoundException e) {
        error(-1, -1, "Exception: " + e.getMessage());
      }

      }
    }
  }

  /**
   * A function that checks whether the next character is as expected or not.
   * Useful for when one character can lead to two outcomes, such as '=' is assign and '==' is
   * to check equality.
   * @param expect is what the next character is being checked against.
   * @param ifyes is the TokenType that returns if the next character is as expected.
   * @param ifno is the TokenType that returns if the next character is not as expected.
   * @param line is the line of the location that resulted in this function being called.
   * @param pos is the position within the line at which this function was called.
   * @return a TokenType, either the ifyes or ifno.
   */
  Token follow(char expect, TokenType ifyes, TokenType ifno, int line, int pos) {
    if (getNextChar() == expect) {
      getNextChar();
      // All of the ifyes cases are 2 char combinations, all of the ifno cases are 1 char.
      // This way the getNextChar() in the if statement suffices for the ifno cases
      // and the ifyes cases get the extra getNextChar() to avoid an processing the 2nd char separatley.
      return new Token(ifyes, "", line, pos);
    }
    if (ifno == TokenType.End_of_input) {
      error(line, pos, String.format("follow: unrecognized character: (%d) '%c'", (int) this.chr, this.chr));
    }
    return new Token(ifno, "", line, pos);
  }

  /**
   * Function to handle character literals, indicated by an opening '.
   * @param line is the line in which this function was called.
   * @param pos is the position of the character where this function was called.
   * @return is a token with the unicode value of the char enclosed within two 's.
   */
  Token char_lit(int line, int pos) { // handle character literals
    char c = getNextChar(); // skip opening quote
    int n = (int) c;
    // code here
    if(c == '\\'){// Handles the case of escape characters, only using '/n' in the input files.
      // Otherwise would have else if(getNextChar() == 't'), etc., or a switch statement.
      if(getNextChar() == 'n'){
        c = '\n';
        n = (int) c;
      }
    }
    getNextChar(); // skip closing quote
    return new Token(TokenType.Integer, "" + n, line, pos);
  }

  /**
   * Function to handle String literals, indicated by an introductory ".
   * @param start is the first character in the String.
   * @param line is the line at which this function was called.
   * @param pos is the position of the character that resulted in this function being called.
   * @return is a token with the string as its value.
   */
  Token string_lit(char start, int line, int pos) { // handle string literals
    String result = "";
    // code here
    while(start != '\n' && start != '\u0000'){// Making sure that the String is in a valid format.
      start = getNextChar();
      if(start == '"' || start == '\''){// Detect the end of a string.
        return new Token(TokenType.String, result, line, pos);
      }
      result += start;// Add getNextChar() to String.
    }
    return new Token(TokenType.String, result, line, pos);
  }

  /**
   * A function that can tell whether a / indicates division or a comment.
   * @param line is the line at which this function was called.
   * @param pos is the position of the character that resulted in this function being called.
   * @return either getToken() if the / is a comment, skipping the comment, or a token of the
   * TokenType Op_divide.
   */
  Token div_or_comment(int line, int pos) { // handle division or comments
    // code here
    char next = getNextChar(); // peek ahead after '/'

    if (next == '/') {// Single-line comment, skip until newline or end of input
      while (this.chr != '\n' && this.chr != '\u0000') {
        getNextChar();
      } // Line ends, get the next token.
      return getToken();
    } else if (next == '*') { // Multi-line comment: skip until */
      char prev;
      while (true) {
        prev = this.chr;
        char curr = getNextChar();
        if (prev == '*' && curr == '/') {
          getNextChar(); // move past closing '/'
          break;
        }
      } // Done skipping comment, get next token
      return getToken();
    } else { // Not a comment, is actually a division operator
      return new Token(TokenType.Op_divide, "/", line, pos);
    }
  }

  /**
   * A function to tell whether the characters which follow are an identifier or integer (or keyword).
   * @param line is the line at which this function was called.
   * @param pos is the position at which this function was called.
   * @return a token that is either an integer, identifier, or keyword.
   */
  Token identifier_or_integer(int line, int pos) { // handle identifiers and integers
    boolean is_number = true;
    String text = "";
    // code here
    char c = this.chr;
    while(Character.isDigit(c) || Character.isLetter(c) || c == '_'){// valid parts of identifiers
      if(!Character.isDigit(c)){// If any of the characters are not a digit, it cannot be an integer.
        is_number = false;
      }
      text = text + c;
      c = getNextChar();
    }
    if(is_number){
      return new Token(TokenType.Integer, text, line, pos);
    }
    if(keywords.containsKey(text)){// Check against HashMap if text matches any of the possible Keywords.
      return new Token(keywords.get(text), text, line, pos);
    }
    return new Token(TokenType.Identifier, text, line, pos);
    // If not a number and not a Keyword, then it must be an identifier.
  }

  /**
   * Iterates through the text of the input file determining which parts correspond to which tokens.
   * @return various types of tokens depending on the input. See other functions in this file.
   */
  Token getToken() {
    int line, pos;
    while (Character.isWhitespace(this.chr)) {
      getNextChar();
    }
    line = this.line;
    pos = this.pos;

    // switch statement on character for all forms of tokens with return to follow....
    // one example left for you

    switch (this.chr) {
      case '\u0000':
        return new Token(TokenType.End_of_input, "", this.line, this.pos);
      // remaining case statements
      case '*':
        Token t = new Token(TokenType.Op_multiply, "*", this.line, this.pos);
        getNextChar();
        return t;
      case '/':
        return div_or_comment(this.line, this.pos);
      case '%':
        t = new Token(TokenType.Op_mod, "%", this.line, this.pos);
        getNextChar();
        return t;
      case '+':
        t = new Token(TokenType.Op_add, "+", this.line, this.pos);
        getNextChar();
        return t;
      case '-':
        t = new Token(TokenType.Op_subtract,"-",this.line,this.pos);
        getNextChar();
        return t;
      case '!':
      return follow('=', TokenType.Op_notequal, TokenType.Op_not, this.line, this.pos);
      case '<':
        return follow('=', TokenType.Op_lessequal,TokenType.Op_less, this.line, this.pos);
      case '>':
        return follow('=', TokenType.Op_greaterequal,TokenType.Op_greater, this.line, this.pos);
      case '=':
        return follow('=', TokenType.Op_equal, TokenType.Op_assign, this.line, this.pos);
      case '&':
        return follow('&', TokenType.Op_and, TokenType.End_of_input, this.line, this.pos);
      case '|':
        return follow('|', TokenType.Op_or, TokenType.End_of_input, this.line, this.pos);
      case '(':
        t = new Token(TokenType.LeftParen, "(", this.line, this.pos);
        getNextChar();
        // follow function does this, got stuck in an infinite loop before I remembered getNextChar() here.
        return t;
      case ')':
        t = new Token(TokenType.RightParen, ")", this.line, this.pos);
        getNextChar();
        return t;
      case '{':
        t = new Token(TokenType.LeftBrace, "{", this.line, this.pos);
        getNextChar();
        return t;
      case '}':
        t = new Token(TokenType.RightBrace, "}", this.line, this.pos);
        getNextChar();
        return t;
      case ';':
        t = new Token(TokenType.Semicolon, ";", this.line, this.pos);
        getNextChar();
        return t;
      case ',':
        t = new Token(TokenType.Comma, ",", this.line, this.pos);
        getNextChar();
        return t;
      case '"':
        t = string_lit('"', this.line, this.pos);
        getNextChar();
        return t;
      case '\'':
        return char_lit(this.line, this.pos);
      default:
        return identifier_or_integer(line, pos);
    }
  }

  /**
   * Finds the next non-whitespace character in the input file and returns it.
   * @return the next non-whitespace character.
   */
  char getNextChar() {
    this.pos++;
    this.position++;
    if (this.position >= this.s.length()) {
      this.chr = '\u0000';
      return this.chr;
    }
    this.chr = this.s.charAt(this.position);
    if (this.chr == '\n') {
      this.line++;
      this.pos = 0;
    }
    return this.chr;
  }

  /**
   * Prints the line, position, and tokentype to the console.
   * @return the output printed to the console as a String.
   */
  String printTokens() {
    Token t;
    StringBuilder sb = new StringBuilder();
    while ((t = getToken()).tokentype != TokenType.End_of_input) {
      sb.append(t);
      sb.append("\n");
      System.out.println(t);
    }
    sb.append(t);
    System.out.println(t);
    return sb.toString();
  }

  /**
   * Enumeration of the different possible types of tokens.
   */
  static enum TokenType {
    End_of_input, Op_multiply, Op_divide, Op_mod, Op_add, Op_subtract,
    Op_negate, Op_not, Op_less, Op_lessequal, Op_greater, Op_greaterequal,
    Op_equal, Op_notequal, Op_assign, Op_and, Op_or, Keyword_if,
    Keyword_else, Keyword_while, Keyword_print, Keyword_putc, LeftParen, RightParen,
    LeftBrace, RightBrace, Semicolon, Comma, Identifier, Integer, String
  }

  /**
   *
   */
  static class Token {
    public TokenType tokentype;
    public String value;
    public int line;
    public int pos;

    /**
     * Constructor for token.
     * @param token is the TokenType.
     * @param value is usually the set of characters that correspond to the token, but if this is from
     *              a char literal then it is the corresponding unicode.
     * @param line the line at which this token occurs.
     * @param pos the position of the beginning of this token in the line.
     */
    Token(TokenType token, String value, int line, int pos) {
      this.tokentype = token;
      this.value = value;
      this.line = line;
      this.pos = pos;
    }

    /**
     * @return the information about a token in the specific format for the .lex files.
     */
    @Override
    public String toString() {
      String result = String.format("%d  %20d %-20s", this.line, this.pos, this.tokentype);
      switch (this.tokentype) {
        case Integer:
          result += String.format("  %s", value);
          break;
        case Identifier:
          result += String.format("  %s", value);
          break;
        case String:
          result += String.format(" \"%s\"", value);
          break;
      }
      return result;
    }
  }
}