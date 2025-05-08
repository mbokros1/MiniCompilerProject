import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

  Lexer testLexer;

  @org.junit.jupiter.api.Test
  void follow() {
    testLexer = new Lexer("==");
    Lexer.Token t =  testLexer.follow('=', Lexer.TokenType.Op_equal,Lexer.TokenType.Op_assign,1,1);
    assertEquals(Lexer.TokenType.Op_equal, t.tokentype);
  }

  @org.junit.jupiter.api.Test
  void char_lit() {
  }

  @org.junit.jupiter.api.Test
  void string_lit() {
  }

  @org.junit.jupiter.api.Test
  void div_or_comment() {
  }

  @org.junit.jupiter.api.Test
  void identifier_or_integer() {
  }

  @org.junit.jupiter.api.Test
  void getToken() {
  }

  @org.junit.jupiter.api.Test
  void getNextChar() {
  }

  @org.junit.jupiter.api.Test
  void printTokens() {
  }

  @org.junit.jupiter.api.Test
  void outputToFile() {
  }
}