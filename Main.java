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
	public static HashMap<String, String> registerInUse = new HashMap<String, String>();
	// clock cycle
	public static AtomicInteger clock_cycle = new AtomicInteger(1);
	// program counter
	static AtomicInteger program_counter = new AtomicInteger(0);
	static int number_of_instructions = 0;
	static LinkedList<Integer> runningThreads = new LinkedList<Integer>();
	public static void parser(String line)
	{
		String[] temp = line.split(" ", 2); // splits the instruction from the register (2 ensures that it only gets the first occurence of the space character)
		instruction.add(temp[0]); // stores the instruction
		String[] reg = temp[1].replace(" ", "").split(","); // splits the registers separated by comma

		for (int i = 0; i < reg.length; i++)
		{
			// System.out.println("ADDING REGISTER: " + reg[i]);
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
		for (int i = 1; i <= 32; i++)
		{
			memory.put("R" + i, 0);
		}
		for (int i = 1; i <= 32; i++)
		{
			registerInUse.put("R" + i, null);
		}
	}
	public static boolean instructionFinished(LinkedList<Instruction> instruction_queue)
	{
		boolean finished = true;
		for (Instruction i : instruction_queue)
		{
			if (i.fetch == false || i.decode == false || i.execute == false || i.mem == false || i.wb == false)
			{
				finished = false;
			}
		}
		return finished;
	}
	public static void main(String[] args)
	{
		//if there is no argument entered, terminate and require user to enter a filename as argument
		if(args.length==0){
			System.out.println("1 argument is required.");
			System.out.println("Usage: java Main <filename>");
			System.exit(-1);
		}


		loadFile(args[0]);
		// initial mapping of registers and their values
		// for (int i = 0; i < instruction.size(); i++)
		// {
		// 	System.out.println("INS: " + instruction.get(i));
		// }
		initializeRegisters();
		int threads = 0;
		System.out.println("INSTRUCTION COUNT: " + number_of_instructions);
		LinkedList<Instruction> instruction_queue = new LinkedList<Instruction>();
		while (threads < number_of_instructions)
		{
			instruction_queue.add(new Instruction(threads, instruction, registers, memory, flags, hardware, registerInUse, program_counter, runningThreads, clock_cycle));
			threads += 1;


		}

		while (!instructionFinished(instruction_queue))
		{
			for (Instruction i : instruction_queue)
			{
				delay();

				// update the instruction's registerInUse map
				for (int z = 1; z < 32; z++)
				{
					i.registerInUse.replace("R" + z, registerInUse.get("R" + z));
				}
				// update the instruction's register
				i.register = new LinkedList<String>(registers);
				// update the instruction's memory
				i.memoryBlock = new HashMap<String, Integer>(memory);
				if (i.fetch && i.decode && i.execute && i.mem && i.wb)
				{
					// hardware[4] = false;
					i.registerInUse.replace(i.dest, null);
					i.registerInUse.replace(i.src, null);
					registerInUse = new HashMap<String, String>(i.registerInUse);
					continue;
				}
				if (!i.fetch)
				{
					// System.out.println("DOING FETCH FOR INSTRUCTION " + instruction_queue.indexOf(i) + " AT CLOCK CYCLE " + clock_cycle.get());
					hardware[0] = true;
					i.fetch();

					break;
				}
				else if (i.fetch && !i.decode)
				{
					hardware[0] = false;
					if (hardware[1] == true)
					{
						System.out.println("DECODE HARDWARE NOT AVAILABLE! STALLING...");
						System.out.println("\t @Instruction: " + i.getInstructionString());
						i.stalls += 1;
						continue;
					}
					hardware[1] = true;
					// System.out.println("DOING DECODE FOR INSTRUCTION " + instruction_queue.indexOf(i) + " AT CLOCK CYCLE " + clock_cycle.get());
					int value = i.decode();
					if (value == Instruction.FLOW_DEPENDENCE)
					{
						System.out.println("RAW DEPENDENCY FOUND! STALLING...");
						System.out.println("\t @Instruction: " + i.getInstructionString());
						i.stalls += 1;
						hardware[1] = false;
						continue;
					}
					else if (value == Instruction.OUTPUT_DEPENDENCE){
						System.out.println("WAW DEPENDENCY FOUND! STALLING...");
						System.out.println("\t @Instruction: " + i.getInstructionString());
						i.stalls += 1;
						hardware[1] = false;
						continue;
					}
					else if (value == Instruction.ANTI_DEPENDENCE){
						System.out.println("WAR DEPENDENCY FOUND! STALLING...");
						System.out.println("\t @Instruction: " + i.getInstructionString());
						i.stalls += 1;
						hardware[1] = false;
						continue;
					}
					else if (value == Instruction.ERROR_HALT){
						System.out.println("An error in instruction decode has occurred. Program execution is aborted.");
						System.out.println("\t @Instruction: " + i.getInstructionString());
						System.exit(-1);
					}
					else if (value == Instruction.SUCCESS) registerInUse = new HashMap<String, String>(i.registerInUse);

					registers = new LinkedList<String>(i.register);

					continue;
				}
				else if (i.fetch && i.decode && !i.execute)
				{
					hardware[1] = false;
					if (hardware[2] == true)
					{
						System.out.println("EXECUTE HARDWARE NOT AVAILABLE! STALLING...");
						System.out.println("\t @Instruction: " + i.getInstructionString());
						i.stalls += 1;
						continue;
					}
					hardware[2] = true;
					// System.out.println("DOING EXECUTE FOR INSTRUCTION " + instruction_queue.indexOf(i) + " AT CLOCK CYCLE " + clock_cycle.get());
					i.execute();

					
					continue;
				}
				else if (i.fetch && i.decode && i.execute && !i.mem)
				{
					hardware[2] = false;
					if (hardware[3] == true)
					{
						System.out.println("MEMORY HARDWARE NOT AVAILABLE! STALLING...");
						System.out.println("\t @Instruction: " + i.getInstructionString());
						i.stalls += 1;
						continue;
					}
					hardware[3] = true;
					// System.out.println("DOING MEMORY FOR INSTRUCTION " + instruction_queue.indexOf(i) + " AT CLOCK CYCLE " + clock_cycle.get());
					i.mem_proc();
					continue;
				}
				else if (i.fetch && i.decode && i.execute && i.mem && !i.wb)
				{
					hardware[3] = false;
					if (hardware[4] == true)
					{
						System.out.println("WRITEBACK HARDWARE NOT AVAILABLE! STALLING...");
						System.out.println("\t @Instruction: " + i.getInstructionString());
						i.stalls += 1;
						continue;
					}
					hardware[4] = true;
					// System.out.println("DOING WRITEBACK FOR INSTRUCTION " + instruction_queue.indexOf(i) + " AT CLOCK CYCLE " + clock_cycle.get());
					i.writeBack();

					registerInUse = new HashMap<String, String>(i.registerInUse);
					memory = new HashMap<String, Integer>(i.memoryBlock);

					hardware[4] = false;
					continue;
				}
			}
			registerValues();
			registerStatus();
			clock_cycle.getAndIncrement();
		}
		
		
	}

	public static void delay(){
			try
			{
				Thread.sleep(100);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	}

	public static void registerValues(){
		System.out.println("====================================================");
		System.out.println("As of clock cycle " + clock_cycle.get());
		System.out.println("Register Values");
		for (int i = 1; i<32; i+=4){
			System.out.print("R"+ i + ": " + memory.get("R"+i));
			System.out.print("\tR"+ (i+1) + ": " + memory.get("R"+(i+1)));
			System.out.print("\tR"+ (i+2) + ": " + memory.get("R"+(i+2)));
			System.out.print("\tR"+ (i+3) + ": " + memory.get("R"+(i+3)));
			System.out.println();
		}
		System.out.println("====================================================");
	}
	
	public static void registerStatus(){
		System.out.println("====================================================");
		System.out.println("As of clock cycle " + clock_cycle.get());
		System.out.println("Register Status");
		for (int i = 1; i<32; i+=4){
			System.out.print("R"+ i + ": " + registerInUse.get("R"+i));
			System.out.print("\tR"+ (i+1) + ": " + registerInUse.get("R"+(i+1)));
			System.out.print("\tR"+ (i+2) + ": " + registerInUse.get("R"+(i+2)));
			System.out.print("\tR"+ (i+3) + ": " + registerInUse.get("R"+(i+3)));
			System.out.println();
		}
		System.out.println("====================================================");
	}

}
