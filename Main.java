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
		for (int i = 1; i < 32; i++)
		{
			memory.put("R" + i, 0);
		}
		for (int i = 1; i < 32; i++)
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
		loadFile("input.txt");
		// initial mapping of registers and their values
		for (int i = 0; i < instruction.size(); i++)
		{
			System.out.println("INS: " + instruction.get(i));
		}
		initializeRegisters();
		int threads = 0;
		System.out.println("INSTRUCTION COUNT: " + number_of_instructions);
		LinkedList<Instruction> instruction_queue = new LinkedList<Instruction>();
		while (threads < number_of_instructions)
		{
			// new Thread(new Instruction(threads, instruction, registers, memory, flags, hardware, registerInUse, program_counter, runningThreads, clock_cycle)).start();
			// threads += 1;
			// System.out.println("Program Counter:" + program_counter);
			instruction_queue.add(new Instruction(threads, instruction, registers, memory, flags, hardware, registerInUse, program_counter, runningThreads, clock_cycle));
			threads += 1;


		}
		Collections.reverse(instruction_queue);
		while (!instructionFinished(instruction_queue))
		{
			for (Instruction i : instruction_queue)
			{
				delay();
				// i.updateClockCycle(clock_cycle.get());
				// i.updateRegisterInUse(registerInUse);
				for (int z = 1; z < 32; z++)
				{
					// System.out.println("REGISTER IN USE VALUES OF INSTRUCTION : " + instruction_queue.indexOf(i));
					// System.out.println("R" + z + " , " + i.registerInUse.get("R" + z));
					i.registerInUse.replace("R" + z, registerInUse.get("R" + z));
				}
				i.register = new LinkedList<String>(registers);
				i.memoryBlock = new HashMap<String, Integer>(memory);
				if (!i.fetch)
				{
					System.out.println("DOING FETCH FOR INSTRUCTION " + instruction_queue.indexOf(i) + " AT CLOCK CYCLE " + clock_cycle.get());
					hardware[0] = true;
					i.fetch();

					break;
				}
				else if (i.fetch && !i.decode)
				{
					hardware[0] = false;
					// if (registerInUse.get(i.src).equals("DEST"))
					// {
					// 	System.out.println("RAW! STALLING...");
					// 	i.stalls += 1;
					// 	continue;
					// }
					if (hardware[1] == true)
					{
						System.out.println("DECODE HARDWARE NOT AVAILABLE! STALLING...");
						i.stalls += 1;
						continue;
					}
					hardware[1] = true;
					System.out.println("DOING DECODE FOR INSTRUCTION " + instruction_queue.indexOf(i) + " AT CLOCK CYCLE " + clock_cycle.get());
					int value = i.decode();
					if (value == 1)
					{
						System.out.println("RAW DEPENDENCY FOUND! STALLING...");
						i.stalls += 1;
						hardware[1] = false;
						continue;
					}
					if (value == 2){
						System.out.println("WAW DEPENDENCY FOUND! STALLING...");
						i.stalls += 1;
						hardware[1] = false;
						continue;
					}
					else if (value == 0) registerInUse = new HashMap<String, String>(i.registerInUse);
					// for (int z = 1; z < 32; z++)
					// {
					// 	// System.out.println("REGISTER IN USE VALUES OF INSTRUCTION : " + instruction_queue.indexOf(i));
					// 	// System.out.println("R" + z + " , " + i.registerInUse.get("R" + z));
					// 	registerInUse.replace("R" + z, i.registerInUse.get("R" + z));
					// }
					registers = new LinkedList<String>(i.register);
					
					continue;
				}
				else if (i.fetch && i.decode && !i.execute)
				{
					hardware[1] = false;
					if (hardware[2] == true)
					{
						System.out.println("EXECUTE HARDWARE NOT AVAILABLE! STALLING...");
						i.stalls += 1;
						continue;
					}
					hardware[2] = true;
					System.out.println("DOING EXECUTE FOR INSTRUCTION " + instruction_queue.indexOf(i) + " AT CLOCK CYCLE " + clock_cycle.get());
					i.execute();
					// if (i.fetch == true && i.decode == true && i.execute == true && i.mem == true && i.wb == true)
					// {
					// 	hardware[4] = false;
					// 	continue;
					// }
					memory = new HashMap<String, Integer>(i.memoryBlock);
					continue;
				}
				else if (i.fetch && i.decode && i.execute && !i.mem)
				{
					hardware[2] = false;
					if (hardware[3] == true)
					{
						System.out.println("MEMORY HARDWARE NOT AVAILABLE! STALLING...");
						i.stalls += 1;
						continue;
					}
					hardware[3] = true;
					System.out.println("DOING MEMORY FOR INSTRUCTION " + instruction_queue.indexOf(i) + " AT CLOCK CYCLE " + clock_cycle.get());
					i.mem_proc();
					continue;
				}
				else if (i.fetch && i.decode && i.execute && i.mem && !i.wb)
				{
					hardware[3] = false;
					if (hardware[4] == true)
					{
						System.out.println("WRITEBACK HARDWARE NOT AVAILABLE! STALLING...");
						i.stalls += 1;
						continue;
					}
					hardware[4] = true;
					System.out.println("DOING WRITEBACK FOR INSTRUCTION " + instruction_queue.indexOf(i) + " AT CLOCK CYCLE " + clock_cycle.get());
					i.writeBack();
					registerInUse = new HashMap<String, String>(i.registerInUse);
					hardware[4] = false;
					continue;
				}
				// else if (i.fetch && i.decode && i.execute && i.mem && i.wb){
				// 	registerInUse = new HashMap<String, String>(i.registerInUse);
				// 	continue;
				// }
				
			}
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
}
