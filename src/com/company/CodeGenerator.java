package com.company;

public class CodeGenerator 
{
    PascalScanner scanner; // This was my way of informing CG about Constant Values detected by Scanner, you can do whatever you like
    
    // Define any variables needed for code generation
    
    public CodeGenerator(PascalScanner scanner)
    {
        this.scanner = scanner;
    }

    public void Generate(String sem)
    {
    	System.out.println(sem); // Just for debug
    	
            if (sem.equals("NoSem"))
            	return;
            /*
             * else if (sem.equals("SemanticRoutine1"))
             * {
             * 	...
             * }
             * else if (sem.equals("SemanticRoutine2"))
             * {
             * 	...
             * }
             */
    }
    
    public void FinishCode() // You may need this
    {

    }

    public void WriteOutput(String outputName)
    {
    	// Can be used to print the generated code to output
    	// I used this because in the process of compiling, I stored the generated code in a structure
    	// If you want, you can output a code line just when it is generated (strongly NOT recommended!!)
    }
}
