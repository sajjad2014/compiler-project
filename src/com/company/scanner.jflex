package com.company;
import java.util.*;

%%
%standalone
%class PascalScanner
%public
%line
%column

%function next_token
%type Token
%eofval{
    if(bracketCount == 0)
        return new Token(TokenConst.EOF, "-1");
    else
        Report.error("Odd number of Brackets", - 1);
%eofval}
%eofclose
%{
    int bracketCount = 0;
%}
%{
    public int getLine() { return yyline + 1; }
%}
%eof{
%eof}


Whitespace = [ \n\t\f\r\v]+
SingleLineComment = "--".*
MultiLineComment = [<][-][-]([^-]|[-][^-]|[-][-][^>])*[-][-][>]
ID = [a-zA-Z][_a-zA-Z0-9]*
ICD = [0-9]*[\.][0-9]*
ICC = [0-9]+
HEX = [0][x][a-fA-F0-9]+
StringConst = [\"][^\"]*[\"]
CharConst = ['][^'][']
%%

<YYINITIAL>{
	{Whitespace} {}
	"write" {
        return new Token(TokenConst.WRITE, yytext());
    }
    "read" {
        return new Token(TokenConst.READ, yytext());
    }
    "main" {
        return new Token(TokenConst.MAIN, yytext());
    }
    "strlen" {
        return new Token(TokenConst.STRLEN, yytext());
    }
    "array" {
        return new Token(TokenConst.ARRAY, yytext());
    }	
    "boolean" {
        return new Token(TokenConst.BOOLEAN, yytext());
    }
    "begin" {
        return new Token(TokenConst.BEGIN, yytext());
    }
    "character" {
        return new Token(TokenConst.CHAR, yytext());
    }
    "do" {
        return new Token(TokenConst.DO, yytext());
    }
    "else" {
        return new Token(TokenConst.ELSE, yytext());
    }
    "end" {
        return new Token(TokenConst.END, yytext());
    }
    "false" {
        return new Token(TokenConst.FALSE, yytext());
    }
    "function" {
        return new Token(TokenConst.FUNCTION, yytext());
    }
    "procedure" {
        return new Token(TokenConst.PROCEDURE, yytext());
    }
    "if" {
        return new Token(TokenConst.IF, yytext());
    }
    "integer" {
        return new Token(TokenConst.INTEGER, yytext());
    }
    "long" {
        return new Token(TokenConst.LONG, yytext());
    }
    "of" {
        return new Token(TokenConst.OF, yytext());
    }
    "real" {
        return new Token(TokenConst.REAL, yytext());
    }
    "return" {
        return new Token(TokenConst.RETURN, yytext());
    }
    "string" {
        return new Token(TokenConst.STRING, yytext());
    }
    "true" {
        return new Token(TokenConst.TRUE, yytext());
    }
    "while" {
        return new Token(TokenConst.WHILE, yytext());
    }
    "then" {
        return new Token(TokenConst.THEN, yytext());
    }
    "and" {
        return new Token(TokenConst.AND, yytext());
    }
    "or" {
        return new Token(TokenConst.OR, yytext());
    }
    {SingleLineComment} {}
    "//".* {}
    {MultiLineComment}  {}
    {ID} {
        return new Token(TokenConst.ID, yytext());
    }
    {ICD} {
        return new Token(TokenConst.ICD, yytext());
    }
    {ICC} {
        return new Token(TokenConst.ICC, yytext());
    }
    {HEX} {
        return new Token(TokenConst.HEX, Integer.toString(Integer.parseInt(yytext().substring(2), 16)));
    }
    {StringConst} {
        return new Token(TokenConst.StringConst, yytext());
    }
    {CharConst} {
        return new Token(TokenConst.CharConst, yytext());
    }
    ":=" {
        return new Token(TokenConst.ASSIGN, yytext());
    }
    ":" { 
        return new Token(TokenConst.COLON, yytext());
    }
    "," {
        return new Token(TokenConst.COMMA, yytext());
    }
    "(" {
        return new Token(TokenConst.LPARENTHESIS, yytext());
    }
    ")" {
        return new Token(TokenConst.RPARENTHESIS, yytext());
    }
    ";" {
        return new Token(TokenConst.SEMIC, yytext());
    }
    "+" {
        return new Token(TokenConst.ADD, yytext());
    }
    "=" {
        return new Token(TokenConst.EQU, yytext());
    }
    ">=" {
        return new Token(TokenConst.GEQ, yytext());
    }
    ">" {
        return new Token(TokenConst.GTH, yytext());
    }
    "<" {
        return new Token(TokenConst.LTH, yytext());
    }
    "<=" {
        return new Token(TokenConst.LEQ, yytext());
    }
    "*" {
        return new Token(TokenConst.MUl, yytext());
    }
    "<>" {
        return new Token(TokenConst.NEQ, yytext());
    }
    "-" {
        return new Token(TokenConst.SUB, yytext());
    }
    "~" {
        return new Token(TokenConst.NOT, yytext());
    }
    "/" {
        return new Token(TokenConst.DIV, yytext());
    }
    "&" {
        return new Token(TokenConst.UAND, yytext());
    }
    "^" {
        return new Token(TokenConst.XOR, yytext());
    }
    "|" {
        return new Token(TokenConst.UOR, yytext());
    }
    "[" {
        return new Token(TokenConst.LBRAKET, yytext());
    }
    "]" {
        return new Token(TokenConst.RBRAKET, yytext());
    }
    "%" {
        return new Token(TokenConst.MOD, yytext());
    }
}
