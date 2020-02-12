package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class CodeGenerator {
    PascalScanner scanner; // This was my way of informing CG about Constant Values detected by Scanner, you can do whatever you like
    Stack<Token> idStack = new Stack<Token>();
    Stack<String> stringStack = new Stack<String>();
    HashMap<String, Integer> idType = new HashMap<>();
    HashMap<String, PascalArray> arrayHashMap = new HashMap<>();
    PascalArray currentArray;
    int tempI = 0;
    int stringCount =0;
    int arrayTempI = 0;
    boolean inMain = false;
    int bracketCount = 0;
    String output = "";

    public CodeGenerator(PascalScanner scanner) {
        this.scanner = scanner;
        output += "@.formatStringDigit = private constant [3 x i8] c\"%d\\00\" \n";
        output += "@.formatStringFloat = private constant [3 x i8] c\"%f\\00\" \n";
        output += "@.formatStringChar = private constant [3 x i8] c\"%c\\00\" \n";
        output += "@.formatString = private constant [3 x i8] c\"%s\\00\" \n";
    }

    public void Generate(String sem, Token currentToken) throws Exception {
//        if(!sem.equals("NoSem"))
//    	    System.out.println(sem); // Just for debug
//        System.out.println(currentToken.toString());
        if (sem.equals("NoSem"))
            return;
        else if (sem.equals("@push")) {
            idStack.push(currentToken);
        } else if (sem.equals("@ADCP")) {
            currentArray = new PascalArray(idStack.peek().value, -1);
        } else if (sem.equals("@ub")) {
            currentArray.dimensions.add(Integer.parseInt(currentToken.value));
        } else if (sem.equals("@CASDCP")) {
            currentArray.type = currentToken.type;
            currentArray.createStrings();
            arrayHashMap.put(currentArray.id, currentArray);
            String dimensions = "";
            for (int i = 0; i < currentArray.dimensions.size(); i++)
                dimensions += "[" + currentArray.dimensions.get(i) + " x ";
            dimensions += getType(currentArray.type);
            for (int i = 0; i < currentArray.dimensions.size(); i++)
                dimensions += "]";
            output += "%" + idStack.peek() + " = alloca " + dimensions + ", align 16\n";
            idStack.pop();
        } else if (sem.equals("@SDSCP")) {
            idType.put(idStack.peek().value, currentToken.type);
            if (currentToken.type == 43)
                output += "%" + idStack.peek() + " = alloca i32, align 4\n";
            else if (currentToken.type == 44)
                output += "%" + idStack.peek() + " = alloca float, align 4\n";
            else if (currentToken.type == 45)
                output += "%" + idStack.peek() + " = alloca [50 x i8], align 16\n";
            else if (currentToken.type == 46)
                output += "%" + idStack.peek() + " = alloca i8, align 1\n";
            else if (currentToken.type == 47)
                output += "%" + idStack.peek() + " = alloca i8, align 1\n";
        } else if (sem.equals("@assign")) {
            Token rightHandSide = idStack.pop();
            if (rightHandSide.constant) {
                int leftHandSideType = idType.get(idStack.peek().value);
                if (rightHandSide.type != 45) {
                    rightHandSide = convertConst(rightHandSide, leftHandSideType);
                    if (leftHandSideType == 43)
                        output += "store i32 " + Integer.parseInt(rightHandSide.value) + ", i32* %" + idStack.peek() + ", align 4\n";
                    else if (leftHandSideType == 44) {
                        long intBits = Double.doubleToLongBits((float) Float.parseFloat(rightHandSide.value));
                        String binary = Long.toHexString(intBits);
                        output += "store float " + "0x" + binary.toUpperCase() + ", float* %" + idStack.peek() + ", align 4\n";
                    } else if (leftHandSideType == 46)
                        output += "store i8 " + rightHandSide.value + ", i8* %" + idStack.peek().value + ", align 1\n";
                    else if (leftHandSideType == 47)
                        output += "store i8 " + rightHandSide.value + ", i8* %" + idStack.peek().value + ", align 1\n";
                }else {
                    output += "%."+ arrayTempI + " = getelementptr inbounds [50 x i8], [50 x i8]* %" + idStack.peek().value + ", i32 0, i32 0\n";
                    arrayTempI += 1;
                    int size = rightHandSide.value.length() + 1;
                    output += "%" + tempI + " = call i8* @strcpy(i8* %."+ (arrayTempI - 1) + ", i8* getelementptr inbounds ([" + size + " x i8], [" + size + " x i8]* " + stringStack.pop() + ", i32 0, i32 0))\n";
                    tempI += 1;
                }
            } else {
                if (idType.get(idStack.peek().value) != 45) {
                    if (!rightHandSide.value.matches("\\d+"))
                        Load(rightHandSide);
                    convertRightID(idType.get(idStack.peek().value), rightHandSide);
                    switch (idType.get(idStack.peek().value)) {
                        case 43:
                            output += "store i32 %" + (tempI - 1) + ", i32* %" + idStack.peek().value + ", align 4\n";
                            break;
                        case 44:
                            output += "store float %" + (tempI - 1) + ", float* %" + idStack.peek().value + ", align 4\n";
                            break;
                        case 46:
                        case 47:
                            output += "store i8 %" + (tempI - 1) + ", i8* %" + idStack.peek().value + ", align 1\n";
                            break;
                    }
                }else {
                    output += "%."+ arrayTempI + " = getelementptr inbounds [50 x i8], [50 x i8]* %" + idStack.peek().value + ", i32 0, i32 0\n";
                    arrayTempI += 1;
                    output += "%."+ arrayTempI + " = getelementptr inbounds [50 x i8], [50 x i8]* %" + rightHandSide.value + ", i32 0, i32 0\n";
                    arrayTempI += 1;
                    output += "%" + tempI + " = call i8* @strcpy(i8* %."+ (arrayTempI - 2) + ", i8* %."+ (arrayTempI - 1) + ")\n";
                    tempI += 1;
                }
                idStack.pop();
            }
        } else if (sem.equals("@pop")) {
            idStack.pop();
        } else if (sem.equals("@int")) {
            Token token = new Token(43, currentToken.value);
            token.constant = true;
            idStack.push(token);
        } else if (sem.equals("@float")) {
            Token token = new Token(44, currentToken.value);
            token.constant = true;
            idStack.push(token);
        } else if (sem.equals("@true")) {
            Token token = new Token(47, "1");
            token.constant = true;
            idStack.push(token);
        } else if (sem.equals("@false")) {
            Token token = new Token(47, "0");
            token.constant = true;
            idStack.push(token);
        } else if (sem.equals("@char")) {
            Token token = new Token(46, currentToken.value);
            token.constant = true;
            idStack.push(token);
        } else if (sem.equals("@string")) {
            String string = currentToken.value.substring(1, currentToken.value.length() - 1);
            Token token = new Token(45, currentToken.value.substring(1, currentToken.value.length() - 1));
//            for (int i = string.length() + 1; i < 50; i++) {
//                string += "\\00";
//            }
            output = "@.string" + stringCount + " = private constant ["+ (string.length() + 1) + " x i8] c\""+ string + "\\00\" \n" + output;
            stringStack.push("@.string" + stringCount);
            stringCount += 1;
            token.constant = true;
            idStack.push(token);
        } else if (sem.equals("@array")) {
            ArrayList<Token> indexes = new ArrayList<>();
            while (!arrayHashMap.containsKey(idStack.peek().value))
                indexes.add(idStack.pop());
            PascalArray pascalArray = arrayHashMap.get(idStack.pop().value);
            String name = pascalArray.id;
            for (int i = pascalArray.dimensions.size() - 1; i >= 0; i--) {
                Token token1 = indexes.get(i);
                int type = 43;
                String in1;
                if (token1.type != 0) {
                    token1 = convertConst(token1, type);
                    in1 = token1.value;
                } else {
                    if (!token1.value.matches("-?\\d+")) {
                        Load(token1);
                        convertRightID(type, token1);
                        in1 = "%" + (tempI - 1);
                    } else {
                        if (convertRightID(type, token1) == 0)
                            in1 = "%" + (tempI - 1);
                        else
                            in1 = "%" + token1.value;
                    }
                }
                output += "%." + arrayTempI + " = getelementptr inbounds " + pascalArray.strings.get(i) + ", " + pascalArray.strings.get(i) + "* %" + name +", i32 0, i32 " + in1 + "\n";
                name = "." + arrayTempI;
                arrayTempI += 1;
            }
            idStack.push(new Token(0, name));
            idType.put(name, pascalArray.type);
        } else if (sem.equals("@mult")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);

            if (type == 44) {
                output += "%" + (tempI) + " = fmul float " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = mul nsw i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = mul nsw i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = mul nsw i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
                convertToBool(type, Integer.toString(tempI - 1));
            }
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);
        } else if (sem.equals("@div")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);

            if (type == 44) {
                output += "%" + (tempI) + " = fdiv float " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = sdiv i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = sdiv i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = sdiv i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
                convertToBool(type, Integer.toString(tempI - 1));
            }
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);
        } else if (sem.equals("@add")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                output += "%" + (tempI) + " = fadd float " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = add nsw i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = add nsw i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = add nsw i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
                convertToBool(type, Integer.toString(tempI - 1));
            }
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);
        } else if (sem.equals("@sub")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);

            if (type == 44) {
                output += "%" + (tempI) + " = fsub float " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = sub nsw i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = sub nsw i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = sub nsw i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
                convertToBool(type, Integer.toString(tempI - 1));
            }
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);
        } else if (sem.equals("@uand")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                throw new Exception(String.format("Compile Error (" + currentToken + ") at line " + scanner.getLine() + " @ "));
            } else if (type == 43) {
                output += "%" + (tempI) + " = and i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = and i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = and i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
                convertToBool(type, Integer.toString(tempI - 1));
            }
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);
        } else if (sem.equals("@uor")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                throw new Exception(String.format("Compile Error (" + currentToken + ") at line " + scanner.getLine() + " @ "));
            } else if (type == 43) {
                output += "%" + (tempI) + " = or i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = or i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = or i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
                convertToBool(type, Integer.toString(tempI - 1));
            }
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);
        } else if (sem.equals("@xor")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                throw new Exception(String.format("Compile Error (" + currentToken + ") at line " + scanner.getLine() + " @ "));
            } else if (type == 43) {
                output += "%" + (tempI) + " = xor i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = xor i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = xor i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
                convertToBool(type, Integer.toString(tempI - 1));
            }
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);
        } else if (sem.equals("@mod")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                throw new Exception(String.format("Compile Error (" + currentToken + ") at line " + scanner.getLine() + " @ "));
            } else if (type == 43) {
                output += "%" + (tempI) + " = srem i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = srem i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = srem i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
                convertToBool(type, Integer.toString(tempI - 1));
            }
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);
        } else if (sem.equals("@neg")) {
            Token token1 = idStack.pop();
            int type = token1.type != 0 ? token1.type : idType.get(token1.value);
            String in1 = "";
            if (token1.type != 0) {
                if (type != 44)
                    in1 = token1.value;
                else {
                    long intBits = Double.doubleToLongBits((float) Float.parseFloat(token1.value));
                    in1 = "0x" + Long.toHexString(intBits).toUpperCase();
                }
            } else {
                if (!token1.value.matches("-?\\d+")) {
                    Load(token1);
                    in1 = "%" + (tempI - 1);
                } else {
                    in1 = "%" + token1.value;
                }
            }
            if (type == 44) {
                output += "%" + (tempI) + " = fsub float 0.000000e+00, " + in1 + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = sub nsw i32  0, " + in1 + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = sub nsw i8 0, " + in1 + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = sub nsw i8 0, " + in1 + "\n";
                tempI += 1;
                convertToBool(type, Integer.toString(tempI - 1));
            }
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);
        } else if (sem.equals("@finishwrite")) {
            Token token1 = idStack.pop();
            int type;
            int flag = 0;
            String in1 = "";
            if (token1.type != 0) {
                type = token1.type;
                if(type != 45) {
                    if (type != 44)
                        in1 = token1.value;
                    else {
                        long intBits = Double.doubleToLongBits((float) Float.parseFloat(token1.value));
                        in1 = "0x" + Long.toHexString(intBits).toUpperCase();
                    }
                }else {
                    flag = 1;
                }
            } else {
                if (!token1.value.matches("-?\\d+")) {
                    type = idType.get(token1.value);
                    if (type != 45) {
                        Load(token1);
                        in1 = "%" + (tempI - 1);
                    }else {
                        in1 = token1.value;
                    }
                } else {
                    type = idType.get(token1.value);
                    if(type != 45)
                        in1 = "%" + token1.value;
                }
            }
            switch (type) {
                case 43:
                    output += "%" + tempI + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.formatStringDigit , i32 0, i32 0), i32 " + in1 + ")\n";
                    tempI++;
                    break;
                case 44:
                    output += "%" + tempI + " = fpext float " + in1 + " to double\n";
                    tempI += 1;
                    output += "%" + tempI + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.formatStringFloat , i32 0, i32 0), double %" + (tempI - 1) + ")\n";
                    tempI++;
                    break;
                case 46:
                    output += "%" + tempI + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.formatStringChar , i32 0, i32 0), i8 " + in1 + ")\n";
                    tempI++;
                    break;
                case 47:
                    output += "%" + tempI + " = call i32 (i8*, ...)  @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.formatStringDigit , i32 0, i32 0), i8 %" + (tempI - 1) + ")\n";
                    tempI++;
                    break;
                case 45:
                    if(flag == 1){
                        output += "%" + tempI + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds (["+(token1.value.length() + 1)+" x i8], ["+(token1.value.length() + 1)+" x i8]* "+ stringStack.pop() + ", i32 0, i32 0))\n";
                        tempI += 1;
                    }else {
                        output += "%."+ arrayTempI + " = getelementptr inbounds [50 x i8], [50 x i8]* %" + in1 + ", i32 0, i32 0\n";
                        arrayTempI += 1;
                        output += "%" + tempI + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds (["+(3)+" x i8], ["+(3)+" x i8]*  @.formatString, i32 0, i32 0), i8* %."+ (arrayTempI - 1) + ")\n";
                        tempI += 1;
                    }
            }
        } else if (sem.equals("@startblock")) {
            output += "{\n";
            if (inMain) {
                bracketCount += 1;
                if (bracketCount == 1) {
                    output += "entry:\n";
                }
            }
        } else if (sem.equals("@finishblock")) {
            if (inMain) {
                if (bracketCount == 1) {
                    output += "ret i32 0\n";
                }
                bracketCount -= 1;
            }
            output += "}\n";

        } else if (sem.equals("@maindef")) {
            output += "define i32 @main()\n";
            inMain = true;
        } else if (sem.equals("@finishread")) {
            Token token1 = idStack.pop();
            int type;
            String in1;
            type = idType.get(token1.value);
            in1 = "%" + token1.value;
            switch (type) {
                case 43:
                    output += "%" + tempI + " = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.formatStringDigit, i32 0, i32 0), i32* " + in1 + ")\n";
                    tempI += 1;
                    break;
                case 44:
                    output += "%" + tempI + " = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.formatStringFloat , i32 0, i32 0), float* " + in1 + ")\n";
                    tempI += 1;
                    break;
                case 46:
                    output += "%" + tempI + " = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.formatStringChar, i32 0, i32 0), i8* " + in1 + ")\n";
                    tempI += 1;
                    break;
                case 47:
                    output += "%" + tempI + " = call i32 (i8*, ...)  @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.formatStringDigit , i32 0, i32 0), i8* " + in1 + ")\n";
                    tempI += 1;
                    Load(token1);
                    convertToBool(47, Integer.toString(tempI - 1));
                    output += "store i8 %" + (tempI - 1) + ", i8* " + in1 + ", align 1\n";
                    break;
                case 45:
                    output += "%."+ arrayTempI + " = getelementptr inbounds [50 x i8], [50 x i8]* " + in1 + ", i32 0, i32 0\n";
                    arrayTempI += 1;
                    output += "%" + tempI + " = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds (["+(3)+" x i8], ["+(3)+" x i8]*  @.formatString, i32 0, i32 0), i8* %."+ (arrayTempI - 1) + ")\n";
                    tempI += 1;
                    break;
            }
        } else if (sem.equals("@lth")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                output += "%" + (tempI) + " = icmp slt i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = fcmp olt float " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = icmp slt i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = icmp slt i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            }
            output += "%" + (tempI) + " = zext i1 %" + (tempI - 1) + " to i8\n";
            tempI += 1;
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, 47);
        } else if (sem.equals("@leq")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                output += "%" + (tempI) + " = icmp sle i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = fcmp ole float " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = icmp sle i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = icmp sle i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            }
            output += "%" + (tempI) + " = zext i1 %" + (tempI - 1) + " to i8\n";
            tempI += 1;
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, 47);
        } else if (sem.equals("@gth")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                output += "%" + (tempI) + " = icmp sgt i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = fcmp ogt float " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = icmp sgt i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = icmp sgt i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            }
            output += "%" + (tempI) + " = zext i1 %" + (tempI - 1) + " to i8\n";
            tempI += 1;
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, 47);
        } else if (sem.equals("@geq")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                output += "%" + (tempI) + " = icmp sge i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = fcmp oge float " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = icmp sge i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = icmp sge i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            }
            output += "%" + (tempI) + " = zext i1 %" + (tempI - 1) + " to i8\n";
            tempI += 1;
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, 47);
        } else if (sem.equals("@eq")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                output += "%" + (tempI) + " = icmp eq i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = fcmp oeq float " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = icmp eq i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = icmp eq i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            }
            output += "%" + (tempI) + " = zext i1 %" + (tempI - 1) + " to i8\n";
            tempI += 1;
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, 47);
        } else if (sem.equals("@neq")) {
            String[] res = getOperationsSides();
            int type = Integer.parseInt(res[2]);
            if (type == 44) {
                output += "%" + (tempI) + " = icmp ne i32 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 43) {
                output += "%" + (tempI) + " = fcmp une float " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else if (type == 46) {
                output += "%" + (tempI) + " = icmp ne i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            } else {
                output += "%" + (tempI) + " = icmp ne i8 " + res[1] + ", " + res[0] + "\n";
                tempI += 1;
            }
            output += "%" + (tempI) + " = zext i1 %" + (tempI - 1) + " to i8\n";
            tempI += 1;
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, 47);
        } else if (sem.equals("@or")) {
            Token token1 = idStack.pop();
            Token token2 = idStack.pop();
            int type = 47;
            String in1 = "", in2 = "";
            if (token1.type != 0) {
                token1 = convertConst(token1, type);
                in1 = token1.value;
            } else {
                if (!token1.value.matches("-?\\d+")) {
                    Load(token1);
                    convertRightID(type, token1);
                    in1 = "%" + (tempI - 1);
                } else {
                    if (convertRightID(type, token1) == 0)
                        in1 = "%" + (tempI - 1);
                    else
                        in1 = "%" + token1.value;
                }
            }
            if (token2.type != 0) {
                token2 = convertConst(token2, type);
                in2 = token2.value;
            } else {
                if (!token2.value.matches("-?\\d+")) {
                    Load(token2);
                    convertRightID(type, token2);
                    in2 = "%" + (tempI - 1);
                } else {
                    if (convertRightID(type, token2) == 0)
                        in2 = "%" + (tempI - 1);
                    else
                        in2 = "%" + token2.value;
                }
            }
            output += "%" + (tempI) + " = or i8 " + in2 + ", " + in1 + "\n";
            tempI += 1;
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, 47);
        } else if (sem.equals("@and")) {
            Token token1 = idStack.pop();
            Token token2 = idStack.pop();
            int type = 47;
            String in1 = "", in2 = "";
            if (token1.type != 0) {
                token1 = convertConst(token1, type);
                in1 = token1.value;
            } else {
                if (!token1.value.matches("-?\\d+")) {
                    Load(token1);
                    convertRightID(type, token1);
                    in1 = "%" + (tempI - 1);
                } else {
                    if (convertRightID(type, token1) == 0)
                        in1 = "%" + (tempI - 1);
                    else
                        in1 = "%" + token1.value;
                }
            }
            if (token2.type != 0) {
                token2 = convertConst(token2, type);
                in2 = token2.value;
            } else {
                if (!token2.value.matches("-?\\d+")) {
                    Load(token2);
                    convertRightID(type, token2);
                    in2 = "%" + (tempI - 1);
                } else {
                    if (convertRightID(type, token2) == 0)
                        in2 = "%" + (tempI - 1);
                    else
                        in2 = "%" + token2.value;
                }
            }
            output += "%" + (tempI) + " = and i8 " + in2 + ", " + in1 + "\n";
            tempI += 1;
            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, 47);
        } else if (sem.equals("@not")) {
            Token token1 = idStack.pop();
            int type = 47;
            String in1 = "";
            if (token1.type != 0) {
                token1 = convertConst(token1, type);
                in1 = token1.value;
            } else {
                if (!token1.value.matches("-?\\d+")) {
                    Load(token1);
                    convertRightID(type, token1);
                    in1 = "%" + (tempI - 1);
                } else {
                    if (convertRightID(type, token1) == 0)
                        in1 = "%" + (tempI - 1);
                    else
                        in1 = "%" + token1.value;
                }
            }
            output += "%" + (tempI) + " = xor i8 1, " + in1 + "\n";
            tempI += 1;
            convertToBool(type, Integer.toString(tempI - 1));

            Token token = new Token(0, Integer.toString(tempI - 1));
            idStack.push(token);
            idType.put(token.value, type);

        }
    }

    public int bigger(int type1, int type2) {
        if (type1 == 44 || type2 == 44) {
            return 44;
        } else if (type1 == 43 || type2 == 43) {
            return 43;
        } else if (type1 == 46 || type2 == 46) {
            return 46;
        } else
            return 47;

    }

    public Token convertConst(Token token, int type) {
        if (token.type == type && type != 46) {
            return token;
        } else if (type == 43) {
            switch (token.type) {
                case 44:
                    return new Token(43, Integer.toString((int) Float.parseFloat(token.value)));
                case 46:
                    return new Token(43, Integer.toString((int) token.value.charAt(1)));
                case 47:
                    return new Token(43, Character.toString(token.value.charAt(0)));
            }
        } else if (type == 44) {
            switch (token.type) {
                case 43:
                    return new Token(44, Float.toString((float) Integer.parseInt(token.value)));
                case 46:
                    return new Token(44, Float.toString((float) token.value.charAt(1)));
                case 47:
                    return new Token(44, Float.toString((float) token.value.charAt(0) - 48));
            }
        } else if (type == 46) {
            switch (token.type) {
                case 43:
                    return new Token(46, Integer.toString((char) (Integer.parseInt(token.value) % 255)));
                case 44:
                    return new Token(46, Integer.toString((int) (Float.parseFloat(token.value)) % 256));
                case 46:
                    return new Token(46, Integer.toString(token.value.charAt(1)));
                case 47:
                    return new Token(46, Integer.toString(token.value.charAt(0) - 48));
            }
        } else if (type == 47) {
            switch (token.type) {
                case 43:
                    return new Token(47, Integer.toString((Integer.parseInt(token.value) != 0 ? 1 : 0)));
                case 44:
                    return new Token(47, Integer.toString((int) Float.parseFloat(token.value) != 0 ? 1 : 0));
                case 46:
                    return new Token(47, Integer.toString((int) token.value.charAt(1) != 0 ? 1 : 0));
            }
        }
        return token;
    }

    public void convertToBool(int type, String value) {
        if (!value.matches("-?\\d+")) {
            value = Integer.toString(tempI - 1);
        }
        if (type == 43) {
            output += "%" + tempI + " = icmp ne i32 %" + value + ", 0\n";
            tempI++;
            output += "%" + (tempI) + " = zext i1 %" + (tempI - 1) + " to i8\n";
            tempI++;
        } else if (type == 44) {
            output += "%" + tempI + " = fcmp une float %" + value + ", 0.000000e+00\n";
            tempI++;
            output += "%" + (tempI) + " = zext i1 %" + (tempI - 1) + " to i8\n";
            tempI++;
        } else if (type == 46 || type == 47) {
            output += "%" + tempI + " = icmp ne i8 %" + value + ", 0\n";
            tempI++;
            output += "%" + (tempI) + " = zext i1 %" + (tempI - 1) + " to i8\n";
            tempI++;
        }
    }

    public int convertRightID(int type, Token token1) {
        if (idType.get(token1.value) == type) {
            return 1;
        } else if (type == 43) {
            switch (idType.get(token1.value)) {
                case 44:
                    output += "%" + tempI + " = fptosi float %" + (tempI - 1) + " to i32\n";
                    tempI++;
                    return 0;
                case 46:
                case 47:
                    output += "%" + tempI + " = sext i8 %" + (tempI - 1) + " to i32\n";
                    tempI++;
                    return 0;
            }
        } else if (type == 44) {
            switch (idType.get(token1.value)) {
                case 43:
                    output += "%" + tempI + " = sitofp i32 %" + (tempI - 1) + " to float\n";
                    tempI++;
                    return 0;
                case 46:
                case 47:
                    output += "%" + tempI + " = sitofp i8 %" + (tempI - 1) + " to float\n";
                    tempI++;
                    return 0;
            }
        } else if (type == 46) {
            switch (idType.get(token1.value)) {
                case 43:
                    output += "%" + tempI + " = trunc i32 %" + (tempI - 1) + " to i8\n";
                    tempI++;
                    return 0;
                case 44:
                    output += "%" + tempI + " = fptosi float %" + (tempI - 1) + " to i8\n";
                    tempI++;
                    return 0;
            }
        } else if (type == 47) {
            convertToBool(idType.get(token1.value), token1.value);
            return 0;
        }
        return 1;
    }

    public void Load(Token token) {
        int type = idType.get(token.value);
        if (type == 43) {
            output += "%" + tempI + " = load " + getType(idType.get(token.value)) + ", " + getType(idType.get(token.value)) + "* %" + token.value + ", align 4\n";
            tempI++;
        } else if (type == 44) {
            output += "%" + tempI + " = load " + getType(idType.get(token.value)) + ", " + getType(idType.get(token.value)) + "* %" + token.value + ", align 4\n";
            tempI++;
        } else if (type == 46 || type == 47) {
            output += "%" + tempI + " = load " + getType(idType.get(token.value)) + ", " + getType(idType.get(token.value)) + "* %" + token.value + ", align 1\n";
            tempI++;
        }
    }

    public void FinishCode() // You may need this
    {

    }

    public String getType(int type) {
        switch (type) {
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

    public String[] getOperationsSides() {
        Token token1 = idStack.pop();
        Token token2 = idStack.pop();
        int type1 = token1.type != 0 ? token1.type : idType.get(token1.value);
        int type2 = token2.type != 0 ? token2.type : idType.get(token2.value);
        int type = bigger(type1, type2);
        String in1 = "", in2 = "";
        if (token1.type != 0) {
            token1 = convertConst(token1, type);
            if (type != 44)
                in1 = token1.value;
            else {
                long intBits = Double.doubleToLongBits((float) Float.parseFloat(token1.value));
                in1 = "0x" + Long.toHexString(intBits).toUpperCase();
            }
        } else {
            if (!token1.value.matches("-?\\d+")) {
                Load(token1);
                convertRightID(type, token1);
                in1 = "%" + (tempI - 1);
            } else {
                if (convertRightID(type, token1) == 0)
                    in1 = "%" + (tempI - 1);
                else
                    in1 = "%" + token1.value;
            }
        }
        if (token2.type != 0) {
            token2 = convertConst(token2, type);
            if (type != 44)
                in2 = token2.value;
            else {
                long intBits = Double.doubleToLongBits((float) Float.parseFloat(token2.value));
                in2 = "0x" + Long.toHexString(intBits).toUpperCase();
            }
        } else {
            if (!token2.value.matches("-?\\d+")) {
                Load(token2);
                convertRightID(type, token2);
                in2 = "%" + (tempI - 1);
            } else {
                if (convertRightID(type, token2) == 0)
                    in2 = "%" + (tempI - 1);
                else
                    in2 = "%" + token2.value;
            }
        }
        String[] res = new String[3];
        res[0] = in1;
        res[1] = in2;
        res[2] = Integer.toString(type);
        return res;
    }

    public void WriteOutput(String outputName) {
        output += "declare i8* @strcpy(i8*, i8*)\n";
        output += "declare i32 @printf(i8*, ...)\n";
        output += "declare i32 @scanf(i8*, ...)\n";
        System.out.println(output);

    }
}
