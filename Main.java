import java.io.*;
import java.util.*;

public class Main
{
	// list of instructions
	static LinkedList<String> instruction = new LinkedList<String>(); 
	// list of register
	static LinkedList<String> registers = new LinkedList<String>();
	// hash map of registers and actual values
	HashMap<String, Integer> memory = new HashMap<String, Integer>();

	Boolean[] hardware = {false, false, false, false, false};

	public static void parser(String line)
	{
		String[] temp = line.split(" ", 2); // splits the instruction from the register (2 ensures that it only gets the first occurence of the space character)
		instruction.add(temp[0]); // stores the instruction
		String[] reg = temp[1].replace(" ", "").split(","); // splits the registers separated by comma
		
		for (int i = 0; i < reg.length; i++)
		{
			registers.add(reg[i]);
		}
	}
	public static void loadFile(String path)
	{
		try
		{
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);

			String line;

			while ((line = br.readLine()) != null)
			{
				parser(line);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args)
	{
		loadFile("input.txt");
		// initial mapping of registers and their values

	}
}