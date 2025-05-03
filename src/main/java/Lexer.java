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
    this.keywords.put("while", TokenType.Keyword_while);

  }

  static void error(int line, int pos, String msg) {
    if (line > 0 && pos > 0) {
      System.out.printf("%s in line %d, pos %d\n", msg, line, pos);
    } else {
      System.out.println(msg);
    }
    System.exit(1);
  }

  static void outputToFile(String result, String filename) {
    try {
      FileWriter myWriter = new FileWriter(filename);
      myWriter.write(result);
      myWriter.close();
      System.out.println("Successfully wrote to the file.");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    String[] files = {"prime", "fizzbuzz", "99bottles"};

    if (1 == 1) {
      try {
          for(int i = 0; i < files.length; i++) {
              File f = new File("src/main/resources/" + files[i] + "/" + files[i] + ".c");
              Scanner s = new Scanner(f);
              String source = " ";
              String result = " ";
              while (s.hasNext()) {
                  source += s.nextLine() + "\n";
              }
              Lexer l = new Lexer(source);
              result = l.printTokens();
              outputToFile(result, "src/main/resources/" + files[i] + "/" + files[i] + ".lex");
          }
      } catch (FileNotFoundException e) {
        error(-1, -1, "Exception: " + e.getMessage());
      }
    } else {
      error(-1, -1, "No args");
    }
  }

  Token follow(char expect, TokenType ifyes, TokenType ifno, int line, int pos) {
    if (getNextChar() == expect) {
      getNextChar();
      return new Token(ifyes, "", line, pos);
    }
    if (ifno == TokenType.End_of_input) {
      error(line, pos, String.format("follow: unrecognized character: (%d) '%c'", (int) this.chr, this.chr));
    }
    return new Token(ifno, "", line, pos);
  }

  Token char_lit(int line, int pos) { // handle character literals
    char c = getNextChar(); // skip opening quote
    int n = (int) c;
    // code here
    if(c == '\\'){
      if(getNextChar() == 'n'){
        c = '\n';
        n = (int) c;
      }
    }
    getNextChar(); // skip closing quote
    return new Token(TokenType.Integer, "" + n, line, pos);
  }

  Token string_lit(char start, int line, int pos) { // handle string literals
    String result = "";
    // code here
    while(start != '\n' && start != '\u0000'){
      start = getNextChar();
      if(start == '"' || start == '\''){
        return new Token(TokenType.String, result, line, pos);
      }
      result += getNextChar();
      pos++;
    }
    return new Token(TokenType.String, result, line, pos);
  }

  Token div_or_comment(int line, int pos) { // handle division or comments
    // code here
    return getToken();
  }

  Token identifier_or_integer(int line, int pos) { // handle identifiers and integers
    boolean is_number = true;
    String text = "";
    // code here
    return new Token(TokenType.Identifier, text, line, pos);
  }

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
        return new Token(TokenType.Op_multiply, "*", this.line, this.pos);
      case '/':
        div_or_comment(this.line, this.pos);
      case '%':
        return new Token(TokenType.Op_mod, "%", this.line, this.pos);
      case '+':
        return new Token(TokenType.Op_add, "+", this.line, this.pos);
      case '-':
        return new Token(TokenType.Op_subtract,"-",this.line,this.pos);
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
        return new Token(TokenType.LeftParen, "(", this.line, this.pos);
      case ')':
        return new Token(TokenType.RightParen, ")", this.line, this.pos);
      case '{':
        return new Token(TokenType.LeftBrace, "{", this.line, this.pos);
      case '}':
        return new Token(TokenType.RightBrace, "}", this.line, this.pos);
      case ';':
        return new Token(TokenType.Semicolon, ";", this.line, this.pos);
      case ',':
        return new Token(TokenType.Comma, ",", this.line, this.pos);
      default:
        return identifier_or_integer(line, pos);
    }
  }

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

  static enum TokenType {
    End_of_input, Op_multiply, Op_divide, Op_mod, Op_add, Op_subtract,
    Op_negate, Op_not, Op_less, Op_lessequal, Op_greater, Op_greaterequal,
    Op_equal, Op_notequal, Op_assign, Op_and, Op_or, Keyword_if,
    Keyword_else, Keyword_while, Keyword_print, Keyword_putc, LeftParen, RightParen,
    LeftBrace, RightBrace, Semicolon, Comma, Identifier, Integer, String
  }

  static class Token {
    public TokenType tokentype;
    public String value;
    public int line;
    public int pos;

    Token(TokenType token, String value, int line, int pos) {
      this.tokentype = token;
      this.value = value;
      this.line = line;
      this.pos = pos;
    }

    @Override
    public String toString() {
      String result = String.format("%5d  %5d %-15s", this.line, this.pos, this.tokentype);
      switch (this.tokentype) {
        case Integer:
          result += String.format("  %4s", value);
          break;
        case Identifier:
          result += String.format(" %s", value);
          break;
        case String:
          result += String.format(" \"%s\"", value);
          break;
      }
      return result;
    }
  }
}