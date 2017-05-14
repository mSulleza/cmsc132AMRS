import java.util.LinkedList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
public class Instruction extends Thread
{

	private Thread t;
	// instructions will run as threads to emulate concurrency in pipelining
	// they will share the instruction and register stack and the memory hashmap


	// the will also share the array of boolean to stall the process
	// flags for instructions are as follows:
	// [IF ] - fetch
	// [ID ] - decode
	// [EX ] - execute
	// [MEM] - memory
	// [WB ] - write back

	//constants
	public static final String ZERO_FLAG = "ZF";
	public static final String OVERFLOW_FLAG = "OF";
	public static final String NEGATIVE_FLAG = "NF";
	public static final String ADD = "ADD";
	public static final String LOAD = "LOAD";
	public static final String CMP = "CMP";
	public static final String SUB = "SUB";
	public static final String DEST = "DEST";
	public static final String SRC = "SRC";

	// boolean for current stage
	boolean fetch = false;
	boolean decode = false;
	boolean execute = false;
	boolean mem = false;
	boolean wb = false;

	// immediate value for handling immediate input in LOAD instructions
	int immediate = 0;

	// assuming all instructions are I-types (Opcode <dest>, <src>)
	String dest;
	String src;

	// boolean flag if immediate value will be used as src
	boolean isImmediate;

	// stall counter for this instruction
	int stalls = 0;

	// holds result of computation
	int result = 0;

	// instruction flags
	boolean isLoad = false;
	boolean isAdd = false;
	boolean isSub = false;
	boolean isCmp = false;

	//instruction stop
	boolean instStop = false;

	LinkedList<String> instruction;
	LinkedList<String> register;
	HashMap<String, Integer> memoryBlock;
	HashMap<String, Integer> flag;
	Boolean[] hardware;
	HashMap<String,String> registerInUse;
	AtomicInteger program_counter;
	String current_instruction;
	LinkedList<Integer> runningTheads;
	int ins_count;
	public Instruction(int ins_count, LinkedList<String> instruction, LinkedList<String> registers, HashMap<String, Integer> memory, HashMap<String, Integer> flag, Boolean[] hardware, HashMap<String,String> registerInUse, AtomicInteger program_counter, LinkedList<Integer> runningTheads)
	{
		// constructor
		this.instruction = instruction;
		this.memoryBlock = memory;
		this.flag = flag;
		this.register = registers;
		this.hardware = hardware;
		this.program_counter = program_counter;
		this.registerInUse = registerInUse;
		this.ins_count = ins_count;
		this.runningTheads = runningTheads;
	}

	public synchronized void fetch()
	{
		// do fetch
		while(hardware[0] == true)
		{
			this.stalls += 1;
			System.out.println("FETCH HARDWARE NOT AVAILABLE! STALLING");
			try
			{
				System.out.println("SLEEPING FOR 100ms...");
				Thread.sleep(100);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		// acquire the hardware
		hardware[0] = true;
		System.out.println("FETCHING...");
		System.out.println("PROGRAM COUNTER: " + program_counter.get());
		current_instruction = instruction.get(program_counter.getAndIncrement());
		try
		{
			System.out.println("SLEEPING FOR 100ms...");
			Thread.sleep(100);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// fetch the instruction
		// increase the program counter

		fetch = true;

		// release the hardware

		hardware[0] = false;

	}
	public synchronized void decode()
	{

		while(hardware[1] == true)
		{
			this.stalls += 1;
			System.out.println("DECODE HARDWARE NOT AVAILABLE! STALLING");
			try
			{
				System.out.println("SLEEPING FOR 100ms...");
				Thread.sleep(100);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		//use hardware
		hardware[1] = true;
		//error variable
		boolean halt = false;
		System.out.println("DECODING...");
		// do decode
		// if blocks for different instructions

		//for LOAD instructions
		System.out.println("CURRENT INS: " + current_instruction);
		if (current_instruction.equals(LOAD)){
			this.isLoad = true;

			// get registers to use
			this.dest = register.pop();
			System.out.println("DEST IS : " + this.dest);

			//try-catch block for handling integer parse errors
			try{
				this.src = register.pop();
				this.immediate = Integer.parseInt(this.src);

				//if register is invalid, change halt variable to true
				if(!memoryBlock.containsKey(this.dest)){
					System.out.println("Instruction ADD "+ this.dest +", "+ this.src +"is invalid. Reason: "+ this.dest + "is not a valid register.");
					halt = true;
				}
			}catch(NumberFormatException e){
				// e.printStackTrace();
				System.out.println("Instruction LOAD "+ this.dest +", "+ this.src +"is invalid. Reason: "+ this.src + "is not an immediate value.");
				halt = true;
			}

		}

		//for ADD instructions
		else if (current_instruction.equals(ADD)){
			this.isAdd = true;

			// get registers to use
			this.dest = register.pop();
			System.out.println("(ADD) DEST IS: " + this.dest);
			this.src = register.pop();
			System.out.println("(ADD) SRC IS: " + this.src);

			//check if registers are invalid
			if(checkInvalidRegisterDest() && checkInvalidRegisterSrc())
			{
				System.out.println("HALTING...");
				halt = true;
			}

			//else, assume valid and use registers
			else{
				registerInUse.replace(this.dest, DEST);
				registerInUse.replace(this.src, SRC);
			}

		}

		//for SUB instructions
		else if (this.current_instruction.equals(SUB)){
			this.isSub = true;

			//get registers to use
			this.dest = register.pop();
			this.src = register.pop();

			//check if registers are invalid
			if(checkInvalidRegisterDest() && checkInvalidRegisterSrc()) halt = true;

			//else, assume valid and use registers
			else {
				registerInUse.replace(this.dest, DEST);
				registerInUse.replace(this.src, SRC);
			}
		}

		// for CMP instructions
		else if (this.current_instruction.equals(CMP)){
			this.isCmp = true;

			//get registers to use
			this.dest = register.pop();
			this.src = register.pop();

			//check if registers are invalid
			if(checkInvalidRegisterDest() && checkInvalidRegisterSrc()) halt = true;

			//else, assume valid and use registers
			else {
				registerInUse.replace(this.dest, DEST);
				registerInUse.replace(this.src, SRC);
			}
		}

		//for invalid instructions
		else{
			System.out.println("Instruction " + current_instruction + " " + this.dest + ", " + this.src + " is invalid. Reason: "+ src + "is not a valid operation.");
			halt = true;
		}

		try
		{
			System.out.println("SLEEPING FOR 100ms...");
			Thread.sleep(100);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//make sure to remove hardware
		this.decode = true;
		hardware[1] = false;

		//if there was an error, stop
		if (halt) return;
		System.out.println("FETCH is done!");

	}
	public synchronized void execute()
	{
		boolean halt = false;

		//if hardware is not available or if there is a flow dependency, stall execution
		while(hardware[2] == true)
		{
			this.stalls += 1;
			System.out.println("EXECUTE HARDWARE NOT AVAILABLE! STALLING");
			try
			{
				System.out.println("SLEEPING FOR 100ms...");
				Thread.sleep(100);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		while (checkFlowDependency()) {}
		System.out.println("NO FLOW DEPENDENCIES FOUND!");
		// do execute and use registers
		hardware[2] = true;
		System.out.println("EXECUTING...");
		//no need to conduct error checking since decode stage does most of that(?)

		//computation starts here
		//no writing to registers yet since that happens only during writeback stage

		//for LOAD instruction
		if(isLoad){
			System.out.println("EXECUTING LOAD INSTRUCTION...");
			this.result = this.immediate;
			System.out.println("LOADED: " + this.dest + ": " + this.result);
		}

		//for ADD instruction
		else if(isAdd){
			System.out.println("EXECUTING ADD INSTRUCTION...");
			this.result = memoryBlock.get(dest) + memoryBlock.get(src);
			System.out.println("Result is: " + this.result);
			//raise flags for underflow or overflow
			if(checkForOverflow(this.result) || checkForUnderflow(this.result)) halt = true;

		}

		//for SUB instruction
		else if(isSub){
			System.out.println("EXECUTING SUB INSTRUCTION...");
			this.result = memoryBlock.get(dest) - memoryBlock.get(src);
			System.out.println("Result is: " + this.result);
			//raise flags for underflow or overflow
			if(checkForOverflow(this.result) || checkForUnderflow(this.result)) halt = true;

		}

		//for CMP instruction
		else if(isCmp){
			System.out.println("EXECUTING SUB INSTRUCTION...");
			this.result = memoryBlock.get(this.dest) - memoryBlock.get(this.src);
			System.out.println("Result is: " + this.result);
			// raise flags
			if (this.result==0) flag.replace(ZERO_FLAG, 1);
			else if(this.result<0) flag.replace(NEGATIVE_FLAG, 1);



		}

		this.execute = true;

		//make sure to open registers again
		registerInUse.replace(dest, null);
		registerInUse.replace(src, null);

		// try
		// {
		// 	System.out.println("SLEEPING FOR 100ms...");
		// 	Thread.sleep(100);
		// }
		// catch (Exception e)
		// {
		// 	e.printStackTrace();
		// }

			//make sure to deallocate hardware
		hardware[2] = false;
		if (halt){
			System.out.println("HALTING...");
			return;
		}

	}
	public synchronized void mem_proc()
	{
		// do memory
		while(hardware[3] == true)
		{
			this.stalls += 1;
			System.out.println("MEMORY HARDWARE NOT AVAIABLE! STALLING!");
			try
			{
				System.out.println("SLEEPING FOR 100ms...");
				Thread.sleep(100);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		hardware[3] = true;
		this.mem = true;
		hardware[3] = false;
	}

	public synchronized void writeBack()
	{
		// do write back
		// stallThread.currentThread()
		while(hardware[4] == true)
		{
			this.stalls += 1;
			System.out.println("WRITEBACK HARDWARE NOT AVAILABLE! STALLING!");
			try
			{
				System.out.println("SLEEPING FOR 100ms...");
				Thread.sleep(100);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		hardware[4] = true;

		//access registers
		// for(String s : register){
		// 	if(s.contains(registerInUse)){
		// 		this.register = this.result;
		// 	}
		// }

		memoryBlock.put(dest, result);

		this.wb = true;
		//release the writeback hardware
		hardware[4] = false;
	}

	public synchronized void run()
	{
		// do the 5 stage cycle here
		//
		while(fetch == false || decode == false || execute == false || mem == false || wb == false)
		{
			if (fetch == false && decode == false && execute == false && mem == false && wb == false) fetch();
			else if (fetch == true && decode == false && execute == false && mem == false && wb == false) decode();
			else if (fetch == true && decode == true && execute == false && mem == false && wb == false) execute();
			else if (fetch == true && decode == true && execute == true && mem == false && wb == false) mem_proc();
			else if (fetch == true && decode == true && execute == true && mem == true && wb == false) writeBack();
		}

		System.out.println("TOTAL NUMBER OF STALLS: " + stalls);
	}

	public synchronized void start()
	{
		while(t == null)
		{
			System.out.println("Creating a new thread...");
			t = new Thread (this, Integer.toString(ins_count));
			if (ins_count == 0)
			{
				t.start();
				runningTheads.add(ins_count);
			}
			else
			{
				while (true)
				{
					Boolean found = false;
					for (Integer i : runningTheads)
					{
						System.out.println("Instruction: " + i);
						if (i == ins_count - 1) found = true;
					}

					if (found == true) break;
				}
				System.out.println("Previous instruction started! Starting next instruction...");
				t.start ();
				runningTheads.add(ins_count);
			}
		}
	}

	//checks if dest is an invalid register
	//return true if invalid, false otherwise
	public synchronized boolean checkInvalidRegisterDest(){
		if(!memoryBlock.containsKey(this.dest)){
			System.out.println("Instruction " + this.current_instruction +this.dest+", "+this.src +"is invalid. Reason: "+ this.dest+ "is not a valid register.");
			return true;
		}

		if(memoryBlock.get(this.dest)==null){
			System.out.println("Instruction " + this.current_instruction +this.dest+", "+this.src +"is invalid. Reason: "+ this.dest+ "is not yet initialized.");
			return true;
		}

		return false;
	}

	//checks if src is an invalid register
	//return true if invalid, false otherwise
	public synchronized boolean checkInvalidRegisterSrc(){
		if(!memoryBlock.containsKey(this.src)){
			System.out.println("Instruction " + this.current_instruction +this.dest+", "+this.src +"is invalid. Reason: "+ this.src+ "is not a valid register.");
			return true;
		}

		if(memoryBlock.get(this.src)==null){
			System.out.println("Instruction " + this.current_instruction +this.dest+", "+this.src +"is invalid. Reason: "+ this.src+ "is not yet initialized.");
			return true;
		}
		return false;
	}

	//checks availability of source operand by knowing
	//if the source operand is currently being used
	//as a destination operand by another instruction
	//return true if flow dependency exists, false otherwise
	public synchronized boolean checkFlowDependency(){
		if(registerInUse.containsKey(this.dest)){
			return true;
		}
		return false;
	}

	public synchronized boolean checkForOverflow(int num){
		if (num>99){
			System.out.println("Overflow detected at Instruction"+ this.current_instruction + " " + this.dest + ", " + this.src + ".");
			System.out.println("Terminating...");
			return true;
		}
		else return false;
	}

	public synchronized boolean checkForUnderflow(int num){
		if (num<-99) {
			System.out.println("Underflow detected at Instruction"+ this.current_instruction + " " + this.dest + ", " + this.src + ".");
			System.out.println("Terminating...");
			return true;
		}
		else return false;
	}
}
