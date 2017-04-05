import java.util.*;
public class Instruction implements Runnable
{

	String instruction;
	Integer register1, register2;
	// instructions will run as threads to emulate concurrency in pipelining
	// they will share the instruction and register stack and the memory hashmap
	

	// the will also share the array of boolean to stall the process
	// flags for instructions are as follows:
	// [IF ] - fetch
	// [ID ] - decode
	// [EX ] - execute
	// [MEM] - memory
	// [WB ] - write back
	public Instruction( String instruction, LinkedList<String> registers, HashMap<String, Integer> memory, Boolean[] hardware)
	{
		this.instruction = instruction;
		this.register1 = register1;
		this.register2 = register2;
	}

	public void fetch()
	{
		// do fetch
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
	}
}