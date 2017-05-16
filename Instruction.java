import java.util.LinkedList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
public class Instruction
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
	AtomicInteger clock_cycle;
	String current_instruction;
	LinkedList<Integer> runningThreads;
	int ins_count;
	int local_clock_cycle;
	public Instruction(int ins_count, LinkedList<String> instruction, LinkedList<String> registers, HashMap<String, Integer> memory, HashMap<String, Integer> flag, Boolean[] hardware, HashMap<String,String> registerInUse, AtomicInteger program_counter, LinkedList<Integer> runningThreads, AtomicInteger clock_cycle)
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
		this.runningThreads = runningThreads;
		this.clock_cycle = clock_cycle;
		this.local_clock_cycle = clock_cycle.get();
	}
	public void updateRegisterInUse(HashMap<String, String> newRegisterInUse)
	{
		this.registerInUse = newRegisterInUse;
	}
	public synchronized void fetch()
	{
		// do fetch
		// while()
		// while(hardware[0] == true || (runningThreads.contains(ins_count - 1) == false && ins_count != 0))
		// {
		//
		// 	System.out.println("FETCH HARDWARE NOT AVAILABLE!");
		// 	try
		// 	{
		// 		local_clock_cycle = clock_cycle.get();
		// 		// local_clock_cycle += 1;
		// 		// //clock_cycle.getAndIncrement();
		// 		Thread.sleep(100);
		// 	}
		// 	catch (Exception e)
		// 	{
		// 		e.printStackTrace();
		// 	}
		// 	return;
		// }
		// acquire the hardware
		// hardware[0] = true;

		current_instruction = instruction.get(program_counter.getAndIncrement());
		// current_instruction = instruction.removeFirst();
		// System.out.println("DOING FETCH FOR INSTRUCTION " + ins_count + " AT CLOCK CYCLE " + clock_cycle.get());
		runningThreads.add(ins_count);
		// fetch the instruction
		// increase the program counter

		fetch = true;

		// try
		// {
		//
		// 	Thread.sleep(100);
		// }
		// catch (Exception e)
		// {
		// 	e.printStackTrace();
		// }

		// release the hardware
		hardware[0] = false;
		// local_clock_cycle += 1;
		//clock_cycle.getAndIncrement();


	}
	public synchronized int decode()
	{

		// while(hardware[1] == true)
		// {
		// 	this.stalls += 1;
		// 	local_clock_cycle += 1;
		// 	System.out.println("DECODE HARDWARE NOT AVAILABLE! STALLING");
		// 	try
		// 	{
		// 		Thread.sleep(100);
		// 	}
		// 	catch (Exception e)
		// 	{
		// 		e.printStackTrace();
		// 	}
		// 	return;
		// }

		//use hardware
		// hardware[1] = true;
		// System.out.println("DOING DECODE FOR INSTRUCTION " + ins_count + " AT CLOCK CYCLE " + clock_cycle.get());
		//error variable
		boolean halt = false;
		// do decode
		// if blocks for different instructions

		//for LOAD instructions
		System.out.println("CURRENT INS: " + current_instruction);
		if (current_instruction.equals(LOAD)){
			this.isLoad = true;

			// get registers to use
			this.dest = register.pop();
			registerInUse.replace(this.dest, DEST);
			System.out.println("DEST IS : " + this.dest + " AND NOW SET TO " + registerInUse.get(this.dest) + " IN THE registerInUse HashMap");

			//try-catch block for handling integer parse errors
			try{
				this.src = register.pop();
				this.immediate = Integer.parseInt(this.src);

				//if register is invalid, change halt variable to true
				if(!memoryBlock.containsKey(this.dest)){
					System.out.println("Instruction LOAD "+ this.dest +", "+ this.src +" is invalid. Reason: "+ this.dest + " is not a valid register.");
					halt = true;
				}
			}catch(NumberFormatException e){
				// e.printStackTrace();
				System.out.println("Instruction LOAD "+ this.dest +", "+ this.src +" is invalid. Reason: "+ this.src + "is not an immediate value.");
				halt = true;
			}

			System.out.println("SRC IS : " + this.immediate);

		}

		//for ADD instructions
		else if (current_instruction.equals(ADD)){
			this.isAdd = true;

			// get registers to use
			this.dest = register.pop();
			// System.out.println("(ADD) DEST IS: " + this.dest);
			this.src = register.pop();
			// System.out.println("(ADD) SRC IS: " + this.src);

			//check if registers are invalid
			if(checkInvalidRegisterDest() && checkInvalidRegisterSrc())
			{
				System.out.println("HALTING...");
				halt = true;
			}
			System.out.println("SRC IS " + this.src + " AND USED AS " + registerInUse.get(this.src));
			System.out.println("DEST IS " + this.dest + " AND USED AS " + registerInUse.get(this.dest));
			if (registerInUse.get(this.dest) == null && registerInUse.get(this.src) == null)
			{
				registerInUse.replace(this.dest, DEST);
				registerInUse.replace(this.src, SRC);
			}
			else if (registerInUse.get(this.dest) != null && registerInUse.get(this.dest).equals(DEST))
			{
				return 2;
			}
			else if (registerInUse.get(this.src) != null && registerInUse.get(this.src).equals(DEST))
			{
				return 1;
			}
			
			//else, assume valid and use registers
			// else{
			// 	if (registerInUse.get(this.dest)
			// 	{
			// 		System.out.println("DEPENDENCIES FOUND (RAW)! STALLING");
			// 		this.stalls += 1;
			// 		return;
			// 	}
			// 	// while(checkFlowDependency())
			// 	// {
			// 	// 	System.out.println("DEST REGISTER IS BEING USED AS DEST REGISTER! WAITING FOR IT TO FINISH");
			// 	// 	try
			// 	// 	{
			// 	// 		Thread.sleep(100);
			// 	// 		stalls += 1;
			// 	// 		local_clock_cycle += 1;
			// 	// 	}
			// 	// 	catch(Exception e)
			// 	// 	{
			// 	// 		e.printStackTrace();
			// 	// 	}
			// 	// }
			// }
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

		// try
		// {

		// 	Thread.sleep(100);
		// }
		// catch (Exception e)
		// {
		// 	e.printStackTrace();
		// }
		//make sure to remove hardware
		this.decode = true;
		// hardware[1] = false;
		// local_clock_cycle += 1;
		//if there was an error, stop
		if (halt) return -1;

		return 0;
		// System.out.println("DECODE is done!");

	}
	public synchronized void execute()
	{
		boolean halt = false;

		//if hardware is not available or if there is a flow dependency, stall execution
		// while(hardware[2] == true)
		// {
		// 	this.stalls += 1;
		// 	System.out.println("EXECUTE HARDWARE NOT AVAILABLE! STALLING");
		// 	try
		// 	{
		//
		// 		Thread.sleep(100);
		// 		local_clock_cycle += 1;
		// 	}
		// 	catch (Exception e)
		// 	{
		// 		e.printStackTrace();
		// 	}
		// 	return;
		// }
		// while (checkFlowDependency())
		// {
		// 	System.out.println("FLOW DEPENDECY FOUND! STALLING");
		// 	stalls += 1;
		// 	local_clock_cycle += 1;
		// 	try
		// 	{
		// 		Thread.sleep(100);
		// 	}
		// 	catch (Exception e)
		// 	{
		// 		e.printStackTrace();
		// 	}
		// }
		// while (checkFlowDependency()) {}
		// System.out.println("NO FLOW DEPENDENCIES FOUND!");
		// do execute and use registers
		// hardware[2] = true;
		// System.out.println("DOING EXECUTE FOR INSTRUCTION " + ins_count + " AT CLOCK CYCLE " + clock_cycle.get());
		// System.out.println("EXECUTING...");
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

			//raise flags for underflow or overflow
			if(checkForOverflow(this.result) || checkForUnderflow(this.result)) halt = true;

		}

		//for SUB instruction
		else if(isSub){
			System.out.println("EXECUTING SUB INSTRUCTION...");
			this.result = memoryBlock.get(dest) - memoryBlock.get(src);

			//raise flags for underflow or overflow
			if(checkForOverflow(this.result) || checkForUnderflow(this.result)) halt = true;

		}

		//for CMP instruction
		else if(isCmp){
			System.out.println("EXECUTING SUB INSTRUCTION...");
			this.result = memoryBlock.get(this.dest) - memoryBlock.get(this.src);

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

		// 	Thread.sleep(100);
		// }
		// catch (Exception e)
		// {
		// 	e.printStackTrace();
		// }
		// local_clock_cycle += 1;
			//make sure to deallocate hardware
		// hardware[2] = false;
		if (halt){
			System.out.println("HALTING...");
			return;
		}

	}
	public synchronized void mem_proc()
	{
		// do memory
		// while(hardware[3] == true)
		// {
		// 	this.stalls += 1;
		// 	System.out.println("MEMORY HARDWARE NOT AVAIABLE! STALLING!");
		// 	try
		// 	{
		//
		// 		Thread.sleep(100);
		// 		local_clock_cycle += 1;
		// 	}
		// 	catch (Exception e)
		// 	{
		// 		e.printStackTrace();
		// 	}
		// 	return;
		// }
		// hardware[3] = true;
		// System.out.println("DOING MEMORY FOR INSTRUCTION " + ins_count + " AT CLOCK CYCLE " + clock_cycle.get());
		local_clock_cycle += 1;
		this.mem = true;
		// try
		// {

		// 	Thread.sleep(100);
		// }
		// catch (Exception e)
		// {
		// 	e.printStackTrace();
		// }
		// hardware[3] = false;
	}

	public synchronized void writeBack()
	{
		// do write back
		// stallThread.currentThread()
		// while(hardware[4] == true)
		// {
		// 	this.stalls += 1;
		// 	System.out.println("WRITEBACK HARDWARE NOT AVAILABLE! STALLING!");
		// 	try
		// 	{
		//
		// 		Thread.sleep(100);
		// 		local_clock_cycle += 1;
		// 	}
		// 	catch (Exception e)
		// 	{
		// 		e.printStackTrace();
		// 	}
		// 	return;
		// }

		// hardware[4] = true;
		// System.out.println("DOING WRITEBACK FOR INSTRUCTION " + ins_count + " AT CLOCK CYCLE " + clock_cycle.get());
		//access registers
		// for(String s : register){
		// 	if(s.contains(registerInUse)){
		// 		this.register = this.result;
		// 	}
		// }
		memoryBlock.put(dest, result);
		this.wb = true;
		// try
		// {

		// 	Thread.sleep(100);
		// }
		// catch (Exception e)
		// {
		// 	e.printStackTrace();
		// }
		// local_clock_cycle += 1;
		//release the writeback hardware
		// hardware[4] = false;
		registerInUse.replace(this.dest, null);
		registerInUse.replace(this.src, null);
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
	// public synchronized boolean checkFlowDependency(){
	// 	if(registerInUse.get(this.src).equals(DEST)){
	// 		return true;
	// 	}
	// 	return false;
	// }
	public synchronized boolean checkFlowDependency(){
		if (registerInUse.get(this.src).equals(DEST) || registerInUse.get(this.dest).equals(DEST)){
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
