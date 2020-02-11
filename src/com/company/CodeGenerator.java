package com.company;

import java.util.HashMap;
import java.util.Stack;

public class CodeGenerator
{
    PascalScanner scanner; // This was my way of informing CG about Constant Values detected by Scanner, you can do whatever you like
    Stack<Token> idStack = new Stack<Token>();
    HashMap<String, Integer> idType = new HashMap<>();
    int tempI = 0;
    String output = "";
    public CodeGenerator(PascalScanner scanner)
    {
        this.scanner = scanner;
    }

    public void Generate(String sem, Token currentToken)
    {
        if(!sem.equals("NoSem"))
    	    System.out.println(sem); // Just for debug
//        System.out.println(currentToken.toString());
        if (sem.equals("NoSem"))
            	return;
        else if (sem.equals("@push")){
            idStack.push(currentToken);
        } else if (sem.equals("@SDSCP")){
            idType.put(idStack.peek().value, currentToken.type);
            if (currentToken.type == 43)
                output += "%" + idStack.peek() + " = alloca i32, align 4\n";
            else if (currentToken.type == 44)
                output += "%" + idStack.peek() + " = alloca float, align 4\n";
            else if (currentToken.type == 46)
                output += "%" + idStack.peek() + " = alloca i8, align 1\n";
            else if (currentToken.type == 47)
                output += "%" + idStack.peek() + " = alloca i8, align 1\n";
        } else if (sem.equals("@assign")) {
            Token rightHandSide = idStack.pop();
            if (rightHandSide.constant) {
                int leftHandSideType = idType.get(idStack.peek().value);
                rightHandSide = convertConst(rightHandSide, leftHandSideType);
                if (leftHandSideType == 43)
                    output += "store i32 " + Integer.parseInt(rightHandSide.value) + ", i32* %" + idStack.peek() + ", align 4\n";
                else if (leftHandSideType == 44) {
                    System.out.println(rightHandSide.value);
                    long intBits = Double.doubleToLongBits((float) Float.parseFloat(rightHandSide.value));
                    String binary = Long.toHexString(intBits);
                    output += "store float " + "0x" + binary.toUpperCase() + ", float* %" + idStack.peek() + ", align 4\n";
                } else if (leftHandSideType == 46)
                    output += "store i8 " + rightHandSide.value + ", i8* %" + idStack.peek().value + ", align 1\n";
                else if (leftHandSideType == 47)
                    output += "store i8 " +  rightHandSide.value + ", i8* %" + idStack.peek().value + ", align 1\n";
            } else {
                if(!rightHandSide.value.matches("-?\\d+"))
                    Load(rightHandSide);
                convertRightID(idStack.peek(), rightHandSide);
                switch (idType.get(idStack.peek().value)){
                    case 43:
                        output += "store i32 %" + (tempI - 1) +", i32* %" + idStack.peek().value+ ", align 4\n";
                        break;
                    case 44:
                        output += "store float %" + (tempI - 1) +", float* %" + idStack.peek().value+ ", align 4\n";
                        break;
                    case 46:
                    case 47:
                        output += "store i8 %" + (tempI - 1) +", i8* %" + idStack.peek().value+ ", align 1\n";
                        break;
                }
                idStack.pop();
            }
        } else if (sem.equals("@pop")){
            idStack.pop();
        } else if(sem.equals("@int")){
            Token token = new Token(43, currentToken.value);
            token.constant = true;
            idStack.push(token);
        } else if(sem.equals("@float")){
            Token token = new Token(44, currentToken.value);
            token.constant = true;
            idStack.push(token);
        } else if(sem.equals("@true")){
            Token token = new Token(47, "1");
            token.constant = true;
            idStack.push(token);
        } else if(sem.equals("@false")){
            Token token = new Token(47, "0");
            token.constant = true;
            idStack.push(token);
        }else if(sem.equals("@char")){
            Token token = new Token(46, currentToken.value);
            token.constant = true;
            idStack.push(token);
        }
        else if (sem.equals("@mult")){
            Token token1 = idStack.pop();
            Token token2 = idStack.pop();
            int type1 = token1.type != 0 ? token1.type:idType.get(token1.value);
            int type2 = token2.type != 0 ? token2.type:idType.get(token2.value);
            int type  = bigger(type1, type2);
            if(type != type1){
                if(token1.type != 0){
                    token1 = convertConst(token1, type);
                }else{
                    if(!token1.value.matches("-?\\d+")){
                        Load(token1);
                        convertRightID(token2, token1);
                    }
                }
            }
            if (type == 44) {
                output += "%" + Integer.toString(tempI) + " = fmul float %" + Integer.toString(tempI - 1) + ", %" + Integer.toString(tempI -2) + "\n";
                tempI += 1;
            } else {
                output += "%" + Integer.toString(tempI) + " = mul nsw i32 %" + Integer.toString(tempI - 1) + ", %" + Integer.toString(tempI -2) + "\n";
                tempI += 1;
            }
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);
        }
    }
    public int bigger(int type1, int type2){
        if(type1 == 44 || type2 == 44){
            return 44;
        }else if(type2 == 43 || type2 == 43){
            return 43;
        } else
            return 46;
    }
    public Token convertConst(Token token, int type){
        if (token.type == type && type != 46) {
                return token;
        }
        else  if (type == 43){
            switch (token.type) {
                case 44:
                    return new Token(43, Integer.toString((int)Float.parseFloat(token.value)));
                case 46:
                    return new Token(43, Integer.toString((int) token.value.charAt(1)));
                case 47:
                    return new Token(43, Character.toString( token.value.charAt(0)));
            }
        } else if (type == 44){
            switch (token.type) {
                case 43:
                    return new Token(44, Float.toString((float) Integer.parseInt(token.value)));
                case 46:
                    return new Token(44, Float.toString((float) token.value.charAt(1)));
                case 47:
                    return new Token(44, Float.toString((float)  token.value.charAt(0) - 48));
            }
        } else if (type == 46){
            switch (token.type) {
                case 43:
                    return new Token(46, Integer.toString((char)(Integer.parseInt(token.value) % 255)));
                case 44:
                    return new Token(46, Integer.toString((int)(Float.parseFloat(token.value)) % 256));
                case 46:
                    return new Token(46, Integer.toString(token.value.charAt(1)));
                case 47:
                    return new Token(46, Integer.toString( token.value.charAt(0) - 48));
            }
        } else if (type == 47){
            switch (token.type) {
                case 43:
                    return new Token(47, Integer.toString((Integer.parseInt(token.value) != 0 ? 1:0)));
                case 44:
                    return new Token(47, Integer.toString((int)Float.parseFloat(token.value) != 0 ? 1:0));
                case 46:
                    return new Token(47, Integer.toString((int) token.value.charAt(1) != 0 ? 1:0));
            }
        }
        return token;
    }
    public int makeSame(Token token1, Token token2){
        if (idType.get(token1.value) == idType.get(token2.value)) {
            output += "%" + Integer.toString(tempI) + " = load " + getType(idType.get(token1.value))+", "+ getType(idType.get(token1.value)) + "* %"+ token1.value + ", align 4\n";
            tempI++;
            output += "%" + Integer.toString(tempI) + " = load " + getType(idType.get(token2.value))+", "+ getType(idType.get(token2.value)) + "* %"+ token2.value + ", align 4\n";
            tempI++;
            return idType.get(token1.value);
        }
        else if ((idType.get(token1.value) == 43 && idType.get(token2.value) == 44)){
            output += "%" + Integer.toString(tempI) + " = load " + getType(idType.get(token1.value))+", "+ getType(idType.get(token1.value)) + "* %"+ token1.value + ", align 4\n";
            tempI++;
            output += "%" + Integer.toString(tempI) + " = sitofp i32 %" + Integer.toString(tempI - 1) + "to float\n";
            tempI++;
            output += "%" + Integer.toString(tempI) + " = load " + getType(idType.get(token2.value))+", "+ getType(idType.get(token2.value)) + "* %"+ token2.value + ", align 4\n";
            tempI++;
            return 44;
        }
        return 0;
    }
    public void convertRightID(Token token, Token token1){
        int type = idType.get(token.value);
        if (idType.get(token1.value) == type) {
            return;
        }
        else  if (type == 43){
            switch (idType.get(token1.value)) {
                case 44:
                    output += "%" + tempI + " = fptosi float %" + ( tempI - 1) + "to i32\n";
                    tempI++;
                    return ;
                case 46:
                case 47:
                    output += "%" + tempI + " = sext i8 %" + ( tempI - 1) + "to i32\n";
                    tempI++;
                    return ;
            }
        } else if (type == 44){
            switch (idType.get(token1.value)) {
                case 43:
                    output += "%" + tempI + " = sitofp i32 %" + ( tempI - 1) + "to float\n";
                    tempI++;
                    return ;
                case 46:
                case 47:
                    output += "%" + tempI + " = load " + getType(idType.get(token1.value))+", "+ getType(idType.get(token1.value)) + "* %"+ token1.value + ", align 1\n";
                    tempI++;
                    output += "%" + tempI + " = sitofp i8 %" + ( tempI - 1) + "to float\n";
                    tempI++;
                    return ;
            }
        } else if (type == 46 || type == 47){
            switch (idType.get(token1.value)) {
                case 43:
                    output += "%" + tempI + " = trunc i32 %" + ( tempI - 1) + "to i8\n";
                    tempI++;
                    return ;
                case 44:
                    output += "%" + tempI + " = fptosi float %" + ( tempI - 1) + "to i8\n";
                    tempI++;
                    return ;
            }
        }
    }
    public void Load(Token token){
        int type = idType.get(token.value);
        if (type == 43){
            output += "%" + tempI + " = load " + getType(idType.get(token.value))+", "+ getType(idType.get(token.value)) + "* %"+ token.value + ", align 4\n";
            tempI++;
        } else if (type == 44){
            output += "%" + tempI + " = load " + getType(idType.get(token.value))+", "+ getType(idType.get(token.value)) + "* %"+ token.value + ", align 4\n";
            tempI++;
        } else if (type == 46 || type == 47){
            output += "%" + tempI + " = load " + getType(idType.get(token.value))+", "+ getType(idType.get(token.value)) + "* %"+ token.value + ", align 1\n";
            tempI++;
        }
    }
    public void FinishCode() // You may need this
    {

    }
    public String getType(int type){
        switch(type) {
            case 43:
                return "i32";
            case 44:
                return "float";
            case 46:
            case 47:
                return "i8";
            default:
                return "i0";
        }
    }
    public void WriteOutput(String outputName)
    {
        System.out.println(output);

    }
}
