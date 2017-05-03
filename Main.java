import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
public class Main
{
	// list of instructions
	static LinkedList<String> instruction = new LinkedList<String>();
	// list of register
	static LinkedList<String> registers = new LinkedList<String>();
	// hash map of registers and actual values
	static HashMap<String, Integer> memory = new HashMap<String, Integer>();
	// hash map of flags and values
	static HashMap<String, Integer> flags = new HashMap<String, Integer>();
	// shared boolean for hardware availability
	static Boolean[] hardware = {false, false, false, false, false};

	// shared string hashmap for register availability
	// keys are registers, values are either
	//"dest" (if used as destination),
	//"src" (if used as source),
	//null if not being used
	static HashMap<String, String> registerInUse = new HashMap<String, String>();
	// clock cycle
	static int clock_cycle = 0;
	// program counter
	static AtomicInteger program_counter = new AtomicInteger(0);
	static int number_of_instructions = 0;
	public static void parser(String line)
	{
		String[] temp = line.split(" ", 2); // splits the instruction from the register (2 ensures that it only gets the first occurence of the space character)
		instruction.add(temp[0]); // stores the instruction
		String[] reg = temp[1].replace(" ", "").split(","); // splits the registers separated by comma

		for (int i = 0; i < reg.length; i++)
		{
			System.out.println("ADDING REGISTER: " + reg[i]);
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
				number_of_instructions += 1;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}
	public static void initializeRegisters()
	{
		for (int i = 0; i < 32; i++)
		{
			memory.put("R" + i, 0);
		}
	}
	public static void main(String[] args)
	{
		loadFile("input.txt");
		// initial mapping of registers and their values
		for (int i = 0; i < instruction.size(); i++)
		{
			System.out.println("INS: " + instruction.get(i));
		}
		initializeRegisters();
		int threads = 0;
		System.out.println("INSTRUCTION COUNT: " + number_of_instructions);
		while (threads < number_of_instructions)
		{
			try
			{
				Instruction ins = new Instruction(threads, instruction, registers, memory, flags, hardware, registerInUse, program_counter);
				ins.start();
				ins.join();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			threads += 1;
		}

	}
}
