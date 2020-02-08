package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;


public class Parser
{
    PascalScanner scanner;
    CodeGenerator cg;
    PTBlock[][] parseTable;
    Stack<Integer> parseStack = new Stack<Integer>();
    String[] symbols;

    public Parser(BufferedReader inputFile, String[] symbols, PTBlock[][] parseTable)
    {
        try
        {
            this.parseTable = parseTable;
            this.symbols = symbols;

            scanner = new PascalScanner(inputFile);
            cg = new CodeGenerator(scanner);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public int LineNumber()
    {
        return scanner.getLine(); // Or any other name you used in your Scanner
    }

    public void Parse()
    {
        try
        {
            int tokenId = nextTokenID();
            int curNode = 0;
            boolean notAccepted = true;
            while (notAccepted)
            {
                if (tokenId == -1)
                    return;
                String token = symbols[tokenId];
                PTBlock ptb = parseTable[curNode][tokenId];
                switch (ptb.getAct())
                {
                    case PTBlock.ActionType.Error:
                    {
                        throw new Exception(String.format("Compile Error (" + token + ") at line " + scanner.getLine() + " @ " + curNode));
                    }
                    case PTBlock.ActionType.Shift:
                    {
                        cg.Generate(ptb.getSem());
                        tokenId = nextTokenID();
                        curNode = ptb.getIndex();
                    }
                    break;

                    case PTBlock.ActionType.PushGoto:
                    {
                        parseStack.push(curNode);
                        curNode = ptb.getIndex();
                    }
                    break;

                    case PTBlock.ActionType.Reduce:
                    {
                        if (parseStack.size() == 0)
                        {
                            throw new Exception(String.format("Compile Error (" + token + ") at line " + scanner.getLine() + " @ " + curNode));
                        }

                        curNode = parseStack.pop();
                        ptb = parseTable[curNode][ptb.getIndex()];
                        cg.Generate(ptb.getSem());
                        curNode = ptb.getIndex();
                    }
                    break;

                    case PTBlock.ActionType.Accept:
                    {
                        notAccepted = false;
                    }
                    break;

                }
            }
            cg.FinishCode();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    int nextTokenID()
    {
        Token t = new Token(-1, -1);
        try
        {
            t = scanner.next_token();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        int i;
        if (t.type == 48)
            return -1;
        for (i = 0; i < symbols.length; i++)
            if (symbols[i].equals(t.type.toString()))
                return i;
        (new Exception("Undefined token: " + t)).printStackTrace();
        return 0;
    }

    public void WriteOutput(String outputFile) // You can change this function, if you think it is not comfortable
    {
        cg.WriteOutput(outputFile);
    }
}
