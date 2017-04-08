import java.util.*;
public class Instruction implements Runnable
{

	// instructions will run as threads to emulate concurrency in pipelining
	// they will share the instruction and register stack and the memory hashmap
	

	// the will also share the array of boolean to stall the process
	// flags for instructions are as follows:
	// [IF ] - fetch
	// [ID ] - decode
	// [EX ] - execute
	// [MEM] - memory
	// [WB ] - write back

	// boolean for current stage
	boolean fetch = false;
	boolean decode = false;
	boolean execute = false;
	boolean memory = false;
	boolean writeBack = false;

	LinkedList<String> instruction;
	HashMap<String, Integer> memory;
	Boolean[] hardware;
	int program_counter;
	String current_instruction;
	public Instruction(LinkedList<String> instruction; HashMap<String, Integer> memory, Boolean[] hardware, int program_counter)
	{
		// constructor
		this.instruction = instruction;
		this.memory = memory;
		this.hardware = hardware;
		this.program_counter = program_counter;
	}

	public void fetch()
	{
		// do fetch

		// acquire the hardware
		hardware[0] = true;

		current_instruction = instruction.get(program_counter);
		// fetch the instruction
		// increase the program counter
		program_counter += 1;

		fetch = true;

		// release the hardware

		hardware[0] = false;

	}
	public void decode()
	{
		// do decode
	}
	public void execute()
	{
		// do execute
	}
	public void memory()
	{
		// do memory
	}
	public void writeBack()
	{
		// do write back
	}

	public void run()
	{
		// do the 5 stage cycle here
		//
	}
}