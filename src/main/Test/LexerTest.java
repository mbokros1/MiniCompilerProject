import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

  Lexer testLexer;
  Lexer.Token t;

  @org.junit.jupiter.api.Test
  void follow() {
    testLexer = new Lexer("==");
    t =  testLexer.follow('=', Lexer.TokenType.Op_equal,Lexer.TokenType.Op_assign,1,1);
    assertEquals(Lexer.TokenType.Op_equal, t.tokentype);
  }

  @org.junit.jupiter.api.Test
  void char_lit() {
    testLexer = new Lexer("'a'");
    t = testLexer.char_lit(1,1);
    assertEquals("97", t.value);
  }

  @org.junit.jupiter.api.Test
  void string_lit() {
    testLexer = new Lexer("\"xyz\"");
    t = testLexer.string_lit('\"',1,1);
    assertEquals("xyz", t.value);
  }

  @org.junit.jupiter.api.Test
  void div_or_comment() {
    testLexer = new Lexer("15 / 3");
    t = testLexer.div_or_comment(1,4);
    assertEquals(Lexer.TokenType.Op_divide, t.tokentype);
    testLexer = new Lexer("/*comment*/if");
    t = testLexer.div_or_comment(1,1);
    assertNotEquals(Lexer.TokenType.Op_divide, t.tokentype);
  }

  @org.junit.jupiter.api.Test
  void identifier_or_integer() {
    testLexer = new Lexer("123");
    t = testLexer.identifier_or_integer(1,1);
    assertEquals(Lexer.TokenType.Integer, t.tokentype);
    testLexer = new Lexer("123a");
    t = testLexer.identifier_or_integer(1,1);
    assertEquals(Lexer.TokenType.Identifier, t.tokentype);
    testLexer = new Lexer("if");
    t = testLexer.identifier_or_integer(1,1);
    assertEquals(Lexer.TokenType.Keyword_if, t.tokentype);
  }

  @org.junit.jupiter.api.Test
  void getToken() {
    testLexer = new Lexer("abc;(){}");
    t = testLexer.getToken();
    assertEquals("abc", t.value);
    assertEquals(Lexer.TokenType.Identifier, t.tokentype);
    t = testLexer.getToken();
    assertEquals(Lexer.TokenType.Semicolon, t.tokentype);
    t = testLexer.getToken();
    assertEquals(Lexer.TokenType.LeftParen, t.tokentype);
    t = testLexer.getToken();
    assertEquals(Lexer.TokenType.RightParen, t.tokentype);
    t = testLexer.getToken();
    assertEquals(Lexer.TokenType.LeftBrace, t.tokentype);
    t = testLexer.getToken();
    assertEquals(Lexer.TokenType.RightBrace, t.tokentype);
  }

  @org.junit.jupiter.api.Test
  void getNextChar() {
    testLexer = new Lexer("_bc");
    // testLexer private chr = this.charAt(0) = 'a'
    assertEquals('b', testLexer.getNextChar());
    assertEquals('c', testLexer.getNextChar());
  }

  @org.junit.jupiter.api.Test
  void printTokens() {
    testLexer = new Lexer("abc;(){}");
    String s = testLexer.printTokens();
    assertEquals("1                     0 Identifier            abc\n" +
            "1                     3 Semicolon           \n" +
            "1                     4 LeftParen           \n" +
            "1                     5 RightParen          \n" +
            "1                     6 LeftBrace           \n" +
            "1                     7 RightBrace          \n" +
            "1                     8 End_of_input        ", s);
  }

}