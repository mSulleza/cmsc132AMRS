import java.util.LinkedList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
public class Instruction
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

	public static final int OVERFLOW_THRESHOLD = 99;
	public static final int UNDERFLOW_THRESHOLD = -99;

	public static final int ANTI_DEPENDENCE = 3;
	public static final int OUTPUT_DEPENDENCE = 2;
	public static final int FLOW_DEPENDENCE = 1;

	public static final int ERROR_HALT = -1;
	public static final int SUCCESS = 0;

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
	public void fetch()
	{

		// fetch the instruction
		// increase the program counter
		current_instruction = instruction.get(program_counter.getAndIncrement());

		runningThreads.add(ins_count);


		fetch = true;

		// release the hardware
		hardware[0] = false;



	}
	public int decode()
	{
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
			this.src = register.pop();


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
			else if (checkOutputDependency())
			{
				return OUTPUT_DEPENDENCE;
			}
			else if (checkFlowDependency())
			{
				return FLOW_DEPENDENCE;
			}
			else if (checkAntiDependency())
			{
				return ANTI_DEPENDENCE;
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
			System.out.println("SRC IS " + this.src + " AND USED AS " + registerInUse.get(this.src));
			System.out.println("DEST IS " + this.dest + " AND USED AS " + registerInUse.get(this.dest));
			if (checkRegisterDestFree() && checkRegisterSrcFree())
			{
				registerInUse.replace(this.dest, DEST);
				registerInUse.replace(this.src, SRC);
			}
			else if (checkOutputDependency())
			{
				return OUTPUT_DEPENDENCE;
			}
			else if (checkFlowDependency())
			{
				return FLOW_DEPENDENCE;
			}
			else if (checkAntiDependency())
			{
				return ANTI_DEPENDENCE;
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
			System.out.println("SRC IS " + this.src + " AND USED AS " + registerInUse.get(this.src));
			System.out.println("DEST IS " + this.dest + " AND USED AS " + registerInUse.get(this.dest));
			if (checkRegisterDestFree() && checkRegisterSrcFree())
			{
				registerInUse.replace(this.dest, DEST);
				registerInUse.replace(this.src, SRC);
			}
			else if (checkOutputDependency())
			{
				return OUTPUT_DEPENDENCE;
			}
			else if (checkFlowDependency())
			{
				return FLOW_DEPENDENCE;
			}
			else if (checkAntiDependency())
			{
				return ANTI_DEPENDENCE;
			}
				
		}

		//for invalid instructions
		else{
			System.out.println("Instruction " + current_instruction + " " + this.dest + ", " + this.src + " is invalid. Reason: "+ src + "is not a valid operation.");
			halt = true;
		}


		this.decode = true;

		if (halt) return ERROR_HALT;

		return SUCCESS;


	}
	public void execute()
	{
		boolean halt = false;

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
			System.out.println("EXECUTING CMP INSTRUCTION...");
			this.result = memoryBlock.get(this.dest) - memoryBlock.get(this.src);

			// raise flags
			if (this.result==0) flag.replace(ZERO_FLAG, 1);
			else if(this.result<0) flag.replace(NEGATIVE_FLAG, 1);



		}

		this.execute = true;

		if (halt){
			System.out.println("HALTING...");
			return;
		}

	}
	public void mem_proc()
	{

		local_clock_cycle += 1;
		this.mem = true;
	}

	public void writeBack()
	{

		memoryBlock.put(dest, result);
		this.wb = true;
		registerInUse.replace(this.dest, null);
		registerInUse.replace(this.src, null);
	}



	//checks if dest is an invalid register
	//return true if invalid, false otherwise
	public boolean checkInvalidRegisterDest(){
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
	public boolean checkInvalidRegisterSrc(){
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

	//RAW
	//checks availability of source operand by knowing
	//if the source operand is currently being used
	//as a destination operand by another instruction
	//return true if flow dependency exists, false otherwise

	public boolean checkFlowDependency(){
		if (registerInUse.get(this.src) != null && registerInUse.get(this.src).equals(DEST))
			return true;
		
		return false;
	}

	//WAW
	//checks availability of dest operand
	//if dest operand is marked as "dest"
	//then a preceding instruction is still in the process of writing to it
	//return true if output dependency exists, false otherwise
	public boolean checkOutputDependency(){
		if(registerInUse.get(this.dest) != null && registerInUse.get(this.dest).equals(DEST)) 
			return true;
		return false;
	}

	//WAR
	//checks if dest operand is being used as src operand by a preceding instruction
	//return true if antidependency exists, false otherwise
	public boolean checkAntiDependency(){
		if(registerInUse.get(this.dest) != null && registerInUse.get(this.dest).equals(SRC))
			return true;
		return false;
	}


	//functions that check whether the registers needed are free to use
	public boolean checkRegisterDestFree(){
		if (registerInUse.get(this.dest) == null) return true;
		return false;
	}

	public boolean checkRegisterSrcFree(){
		if (registerInUse.get(this.src) == null) return true;
		return false;
	}


	//functions that check for overflow/underflow


	public boolean checkForOverflow(int num){
		if (num>OVERFLOW_THRESHOLD){
			System.out.println("Overflow detected at Instruction"+ this.current_instruction + " " + this.dest + ", " + this.src + ".");
			System.out.println("Terminating...");
			return true;
		}
		else return false;
	}

	public boolean checkForUnderflow(int num){
		if (num<UNDERFLOW_THRESHOLD) {
			System.out.println("Underflow detected at Instruction"+ this.current_instruction + " " + this.dest + ", " + this.src + ".");
			System.out.println("Terminating...");
			return true;
		}
		else return false;
	}
}
