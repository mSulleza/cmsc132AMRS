import java.util.LinkedList;
import java.util.HashMap;
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
	boolean memory = false;
	boolean writeBack = false;

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
	int program_counter;
	String current_instruction;
	public Instruction(LinkedList<String> instruction, LinkedList<String> registers, HashMap<String, Integer> memory, HashMap<String, Integer> flag, Boolean[] hardware, HashMap<String,String> registerInUse,int program_counter)
	{
		// constructor
		this.instruction = instruction;
		this.memoryBlock = memory;
		this.flag = flag;
		this.register = register;
		this.hardware = hardware;
		this.program_counter = program_counter;
		this.registerInUse = registerInUse;
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
		//use hardware
		hardware[1] = true;
		//error variable
		boolean halt = false;

		// do decode
		// if blocks for different instructions

		//for LOAD instructions
		if (current_instruction==LOAD){
			this.isLoad = true;

			// get registers to use
			this.dest = register.removeFirst();

			//try-catch block for handling integer parse errors
			try{
				this.src = register.removeFirst();	
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
		else if (current_instruction==ADD){
			this.isAdd = true;

			// get registers to use
			this.dest = register.removeFirst();
			this.src = register.removeFirst();

			//check if registers are invalid
			if(checkInvalidRegisterDest() && checkInvalidRegisterSrc()) halt = true;
			
			//else, assume valid and use registers
			else{
				registerInUse.replace(this.dest, DEST);
				registerInUse.replace(this.src, SRC);
			}
			
		}

		//for SUB instructions
		else if (this.current_instruction==SUB){
			this.isSub = true;

			//get registers to use
			this.dest = register.removeFirst();
			this.src = register.removeFirst();

			//check if registers are invalid
			if(checkInvalidRegisterDest() && checkInvalidRegisterSrc()) halt = true;
			
			//else, assume valid and use registers
			else {
				registerInUse.replace(this.dest, DEST);
				registerInUse.replace(this.src, SRC);
			}
		}

		// for CMP instructions
		else if (this.current_instruction==CMP){
			this.isCmp = true;

			//get registers to use
			this.dest = register.removeFirst();
			this.src = register.removeFirst();

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
			System.out.println("Instruction " + current_instruction + " " + this.dest + ", " + this.src + "is invalid. Reason: "+ src + "is not a valid operation.");
			halt = true;
		}

		//make sure to remove hardware
		this.program_counter ++;
		this.decode = true;
		hardware[1] = false;	

		//if there was an error, stop
		if (halt) this.stop();
		
	}
	public void execute()
	{
		boolean halt = false;

		//if hardware is not available or if there is a flow dependency, stall execution
		if(hardware[2]!=true || !checkFlowDependency()){

			// do execute and use registers
			hardware[2] = true;

			//no need to conduct error checking since decode stage does most of that(?)

			//computation starts here
			//no writing to registers yet since that happens only during writeback stage
			
			//for LOAD instruction
			if(isLoad){
				this.result = this.immediate;
				
			}

			//for ADD instruction
			else if(isAdd){
				this.result = memoryBlock.get(dest) + memoryBlock.get(src);

				//raise flags for underflow or overflow
				if(checkForOverflow(this.result) || checkForUnderflow(this.result)) halt = true;
				
			}

			//for SUB instruction
			else if(isSub){
				this.result = memoryBlock.get(dest) - memoryBlock.get(src);

				//raise flags for underflow or overflow
				if(checkForOverflow(this.result) || checkForUnderflow(this.result)) halt = true;
				
			}

			//for CMP instruction
			else if(isCmp){
				this.result = memoryBlock.get(this.dest) - memoryBlock.get(this.src);

				// raise flags
				if (this.result==0) flag.replace(ZERO_FLAG, 1);
				else if(this.result<0) flag.replace(NEGATIVE_FLAG, 1);


			}

			

			this.program_counter ++;
			this.execute = true;
			
			//make sure to open registers again
			registerInUse.replace(dest, null);
			registerInUse.replace(src, null);

			//make sure to deallocate hardware
			hardware[2] = false;
			if (halt){
				this.stop();
			}
		}

		//if stalled, increase stall count and do nothing.
		else
		{
			this.stalls++;
		}
		
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

	public void stop()
	{
		//stop thread here
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

	//checks availability of source operand by knowing
	//if the source operand is currently being used 
	//as a destination operand by another instruction
	//return true if flow dependency exists, false otherwise
	public boolean checkFlowDependency(){
		if(registerInUse.get(this.src)==DEST){
			return true;
		}
		return false;
	}

	public boolean checkForOverflow(int num){
		if (num>99){
			System.out.println("Overflow detected at Instruction"+ this.current_instruction + " " + this.dest + ", " + this.src + ".");
			System.out.println("Terminating...");
			return true;
		}
		else return false;
	}

	public boolean checkForUnderflow(int num){
		if (num<-99) {
			System.out.println("Underflow detected at Instruction"+ this.current_instruction + " " + this.dest + ", " + this.src + ".");
			System.out.println("Terminating...");
			return true;
		}
		else return false;
	}
}
