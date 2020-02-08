package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Program
{
	// Address of PGen output table.
	public static final String stPath = "table.npt";
	
	public static String inputPath = "";
	public static String outputPath = "";
	public static void main(String[] args) throws FileNotFoundException {

        if ( args.length != 2)
        {
            System.err.println("Wrong parameters passed.");
            System.err.println("Use the following format:");
            System.err.println("java Program inputfilename.L outputfilename.Lm");
           return;
        }
        else
        {
	        inputPath = args[0];
	        outputPath = args[1];
        }

	String[] symbols = null;
	PTBlock[][] parseTable = null;

        if (!FileExists(stPath) || !FileExists(inputPath))
        {
        	System.out.println("File not found: " + stPath + " or " + inputPath);
            return;
        }

        try
        {
    		int rowSize, colSize;
    		String[] tmpArr;
    		PTBlock block;
    		
    		try
    		{
    			java.io.FileInputStream fis = new java.io.FileInputStream(new java.io.File(stPath));
    			java.util.Scanner sc = new java.util.Scanner(fis);
    			
    			tmpArr = sc.nextLine().trim().split(" ");
    			rowSize = Integer.parseInt(tmpArr[0]);
    			colSize = Integer.parseInt(tmpArr[1]);
    	
    			String SL = sc.nextLine();
    			// This is the line creates an array of symbols depending on the parse table read.
    			symbols = SL.trim().split(" ");
    	
    			parseTable = new PTBlock[rowSize][colSize];
    			for (int i = 0; sc.hasNext(); i++)
    			{
    	
    				if (!sc.hasNext())
    					throw new Exception("Ivalid .npt file");
    	
    				tmpArr = sc.nextLine().trim().split(" ");
    	
    				//PGen generates some unused rows!
    				if (tmpArr.length == 1)
    				{
    					System.out.println("Anomally in .npt file, skipping one line");
    					continue;
    				}
    	
    				if (tmpArr.length != colSize * 3)
    					throw new Exception("Ivalid line in .npt file");
    					for (int j = 0; j < colSize; j++)
    				{
    					block = new PTBlock();
    					block.setAct(Integer.parseInt((tmpArr[j * 3])));
    					block.setIndex(Integer.parseInt(tmpArr[j * 3 + 1]));
    					block.setSem(tmpArr[j * 3 + 2]);
    					parseTable[i][j] = block;
    				}
    	
    			}
    		}
    		catch (Exception e)
    		{
    			e.printStackTrace();
    		}

        }
        catch (Exception ex)
        {
            System.out.println("Compile Error -> " + ex.getMessage());
            return;
        }
		BufferedReader br = new BufferedReader(new FileReader("in"));
		Parser parser = new Parser(br, symbols, parseTable);

        try
        {
            parser.Parse();
        }
        catch (Exception ex)
        {
            System.out.println("Compile Error -> " + ex.getMessage());
        }
        parser.WriteOutput(outputPath);
	}

	static boolean FileExists(String path)
	{
		java.io.File f = new java.io.File(path);
		boolean b = f.exists();
		if (!b)
			System.out.println("ERROR: File not found: {0}" + path);

		return b;
	}

	// You don't need know about the details of this method
	static void LoadPT(String stPath, String[] symbols, PTBlock[][] parseTable)
	{
		int rowSize, colSize;
		String[] tmpArr;
		PTBlock block;
		
		try
		{
			java.io.FileInputStream fis = new java.io.FileInputStream(new java.io.File(stPath));
			java.util.Scanner sc = new java.util.Scanner(fis);
			
			tmpArr = sc.nextLine().trim().split(" ");
			rowSize = Integer.parseInt(tmpArr[0]);
			colSize = Integer.parseInt(tmpArr[1]);
	
			symbols = sc.nextLine().trim().split(" ");
	
			parseTable = new PTBlock[rowSize][colSize];
			for (int i = 0; i < rowSize; i++)
			{
	
				if (!sc.hasNext())
					throw new Exception("Ivalid .npt file");
	
				tmpArr = sc.nextLine().trim().split(" ");
	
				//PGen generates some unused rows!
				if (tmpArr.length == 1)
				{
					System.out.println("Anomally in .npt file, skipping one line");
					continue;
				}
	
				if (tmpArr.length != colSize * 3)
					throw new Exception("Ivalid line in .npt file");
					for (int j = 0; j < colSize; j++)
				{
					block = new PTBlock();
					block.setAct(Integer.parseInt((tmpArr[j * 3])));
					block.setIndex(Integer.parseInt(tmpArr[j * 3 + 1]));
					block.setSem(tmpArr[j * 3 + 2]);
					parseTable[i][j] = block;
				}
	
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}
