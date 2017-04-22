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
	int immediate;

	// assuming all instructions are I-types (Opcode <dest>, <src>)
	String dest;
	String src;

	// boolean flag if immediate value will be used as src
	boolean isImmediate;

	// stall counter for this instruction
	int stalls;

	// instruction flags
	boolean isLoad;
	boolean isAdd;
	boolean isSub;
	boolean isCmp;

	//instruction stop
	boolean instStop;

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
		hardware[1] = true;
			// do decode

		if (current_instruction==LOAD){
			this.isLoad = true;
			this.dest = register.removeFirst();
			try{
				this.src = register.removeFirst();	
				this.immediate = Integer.parseInt(this.src);
				if(!memoryBlock.containsKey(dest)){
					System.out.println("Instruction ADD "+dest+", "+src +"is invalid. Reason: "+ dest+ "is not a valid register.");
					this.stop();
				}
			}catch(NumberFormatException e){
				// e.printStackTrace();
				System.out.println("Instruction LOAD "+dest+", "+src +"is invalid. Reason: "+ src+ "is not an immediate value.");
				this.stop();
			}
			
		}

		else if (current_instruction==ADD){
			this.isAdd = true;
			this.dest = register.removeFirst();
			this.src = register.removeFirst();
			if(checkValidRegisterDest(ADD) && checkValidRegisterSrc(ADD)) this.stop();
			
		}

		else if (this.current_instruction==SUB){
			this.isSub = true;
			this.dest = register.removeFirst();
			this.src = register.removeFirst();
			if(checkValidRegisterDest(SUB) && checkValidRegisterSrc(SUB)) this.stop();
		}

		else if (this.current_instruction==CMP){
			this.isCmp = true;
			this.dest = register.removeFirst();
			this.src = register.removeFirst();
			if(checkValidRegisterDest(CMP) && checkValidRegisterSrc(CMP)) this.stop();
		}
		else{
			System.out.println("Instruction " + current_instruction + " " + dest + ", " + src + "is invalid. Reason: "+ src + "is not a valid operation.");
			this.stop();
		}


		this.program_counter ++;
		this.decode = true;
		hardware[1] = false;
	
		
	}
	public void execute()
	{
		//if hardware is not available or if there is a flow dependency, stall execution
		if(hardware[2]!=true || !checkFlowDependency()){
			// do execute and use registers
			hardware[2] = true;
			registerInUse.replace(dest, DEST);
			registerInUse.replace(src, SRC);

			if(isLoad){
				memoryBlock.replace(this.dest, this.immediate);
			}

			else if(isAdd){
				int sum = memoryBlock.get(dest) + memoryBlock.get(src);
				if(checkForOverflow(sum) || checkForUnderflow(sum)) this.stop();
				else memoryBlock.replace(dest, sum);
			}
			else if(isSub){
				int difference = memoryBlock.get(dest) - memoryBlock.get(src);
				if(checkForOverflow(difference) || checkForUnderflow(difference)) this.stop();
				else memoryBlock.replace(dest, difference);
			}
			else if(isCmp){
				int difference = memoryBlock.get(dest) - memoryBlock.get(src);
				if (difference==0) flag.replace(ZERO_FLAG, 1);
				else if(difference<0) flag.replace(NEGATIVE_FLAG, 1);


			}

			this.program_counter ++;
			this.execute = true;
			registerInUse.replace(dest, null);
			registerInUse.replace(src, null);
			hardware[2] = false;
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

	//checks if dest is a valid register
	public boolean checkValidRegisterDest(String instruction){
		if(!memoryBlock.containsKey(this.dest)){
			System.out.println("Instruction " + instruction +this.dest+", "+this.src +"is invalid. Reason: "+ this.dest+ "is not a valid register.");
			return true;
		}

		if(memoryBlock.get(this.dest)==null){
			System.out.println("Instruction " + instruction +this.dest+", "+this.src +"is invalid. Reason: "+ this.dest+ "is not yet initialized.");
			return true;
		}

		return false;
	}

	//checks if src is a valid register
	public boolean checkValidRegisterSrc(String instruction){
		if(!memoryBlock.containsKey(this.src)){
			System.out.println("Instruction " + instruction +this.dest+", "+this.src +"is invalid. Reason: "+ this.src+ "is not a valid register.");
			return true;
		}

		if(memoryBlock.get(this.src)==null){
			System.out.println("Instruction " + instruction +this.dest+", "+this.src +"is invalid. Reason: "+ this.src+ "is not yet initialized.");
			return true;
		}
		return false;
	}

	//checks availability of source operand by knowing
	//if the source operand is currently being used 
	//as a destination operand by another instruction
	public boolean checkFlowDependency(){
		if(registerInUse.get(this.src)==DEST){
			return true;
		}
		return false;
	}

	public boolean checkForOverflow(int num){
		if (num>99){
			System.out.println("Overflow detected at Instruction"+ current_instruction + " " + dest + ", " + src + ".");
			System.out.println("Terminating...");
			return true;
		}
		else return false;
	}

	public boolean checkForUnderflow(int num){
		if (num<-99) {
			System.out.println("Underflow detected at Instruction"+ current_instruction + " " + dest + ", " + src + ".");
			System.out.println("Terminating...");
			return true;
		}
		else return false;
	}
}