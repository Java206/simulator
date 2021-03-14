package simulator;

/**
 * Computer class comprises of memory, registers, cc and can execute the
 * instructions based on PC and IR
 * 
 * @author mmuppa
 *
 */
public class Computer {

	private final static int MAX_MEMORY = 50;
	private final static int MAX_REGISTERS = 8;

	private BitString mRegisters[];
	private BitString mMemory[];
	private BitString mPC;
	private BitString mIR;
	private BitString mCC;

	/**
	 * Initializes all the memory to 0, registers to 0 to 7 PC, IR to 16 bit 0s and
	 * CC to 000 Represents the initial state
	 */
	public Computer() {
		mPC = new BitString();
		mPC.setValue(0);
		mIR = new BitString();
		mIR.setValue(0);
		mCC = new BitString();
		mCC.setBits(new char[] { '0', '0', '0' });
		mRegisters = new BitString[MAX_REGISTERS];
		for (int i = 0; i < MAX_REGISTERS; i++) {
			mRegisters[i] = new BitString();
			mRegisters[i].setValue(i);
		}

		mMemory = new BitString[MAX_MEMORY];
		for (int i = 0; i < MAX_MEMORY; i++) {
			mMemory[i] = new BitString();
			mMemory[i].setValue(0);
		}
	}

	/**
	 * Loads a 16 bit word into memory at the given address.
	 * 
	 * @param address memory address
	 * @param word    data or instruction or address to be loaded into memory
	 */
	public void loadWord(int address, BitString word) {
		if (address < 0 || address >= MAX_MEMORY) {
			throw new IllegalArgumentException("Invalid address");
		}
		mMemory[address] = word;
	}

	/**
	 * Performs not operation by using the data from the register based on bits[7:9]
	 * and inverting and storing in the register based on bits[4:6]
	 */
	public void executeNot() {
		BitString destBS = mIR.substring(4, 3);
		BitString sourceBS = mIR.substring(7, 3);
		mRegisters[destBS.getValue()] = mRegisters[sourceBS.getValue()].copy();
		mRegisters[destBS.getValue()].invert();
		setCC(mRegisters[destBS.getValue()].getValue2sComp());

	}

	public void executeAdd() {
		BitString check = mIR.substring(10, 1);
		BitString destBS = mIR.substring(4, 3);
		BitString sourceBS = mIR.substring(7, 3);
		if (check.getValue() == 1) {
			BitString binaryNumber = mIR.substring(11, 5);
			BitString answer = new BitString();
			answer.setValue2sComp(mRegisters[sourceBS.getValue()].getValue() + binaryNumber.getValue2sComp());
			mRegisters[destBS.getValue()] = answer;
		} else if (check.getValue() == 0) {
			BitString sourceBS2 = mIR.substring(13, 3);
			BitString answer = new BitString();
			answer.setValue2sComp(
					mRegisters[sourceBS.getValue()].getValue() + mRegisters[sourceBS2.getValue()].getValue());
			mRegisters[destBS.getValue()] = answer;
		}
		setCC(mRegisters[destBS.getValue()].getValue2sComp());
	}

	public void executeAnd() {
		BitString check = mIR.substring(10, 1);
		BitString destBS = mIR.substring(4, 3);
		BitString sourceBS = mIR.substring(7, 3);
		if (check.getValue() == 1) {
			BitString binaryNumber = new BitString();
			char extension = (char) mIR.substring(11, 1).getValue();
			binaryNumber.setBits(new char[] { extension, extension, extension, extension, extension, extension,
					extension, extension, extension, extension, extension });
			binaryNumber.append(mIR.substring(11, 5));

			BitString answer = new BitString();
			char[] andChar = new char[] {};
			for (int i = 0; i < 16; i++) {
				andChar[i] = mRegisters[sourceBS.getValue()].getBits()[i] == '1' && binaryNumber.getBits()[i] == '1'
						? '1'
						: '0';
			}
			answer.setBits(andChar);
			mRegisters[destBS.getValue()] = answer;
		} else if (check.getValue() == 0) {
			BitString sourceBS2 = mIR.substring(13, 3);
			BitString answer = new BitString();
			char[] andChar = new char[] {};
			for (int i = 0; i < 16; i++) {
				andChar[i] = mRegisters[sourceBS.getValue()].getBits()[i] == '1'
						&& mRegisters[sourceBS2.getValue()].getBits()[i] == '1' ? '1' : '0';
			}
			answer.setBits(andChar);
			mRegisters[destBS.getValue()] = answer;
		}

		setCC(mRegisters[destBS.getValue()].getValue2sComp());
	}

	public void executeLD() {
		BitString destBS = mIR.substring(4, 3);
		mRegisters[destBS.getValue()] = mMemory[mPC.getValue() + mIR.substring(11, 5).getValue2sComp()];
	}

	public void executeBR() {
		BitString destNZP = mIR.substring(4, 3);
		BitString offSet = mIR.substring(7, 9);
		if (mCC.getValue() == destNZP.getValue()) {
			mPC.setValue(mPC.getValue() + offSet.getValue2sComp());
		}
	}

	/*
	 * if(PE == 1 && (CPL > IOPL || VM == 1)) { //Protected mode with CPL > IOPL or
	 * virtual-8086 mode if(!IOPermission()) Exception(GP); //Any I/O Permission Bit
	 * for I/O port being accessed = 1 else Destination = Source; //Writes to
	 * selected I/O port } //Real Mode or Protected Mode with CPL <= IOPL else
	 * Destination = Source; //Writes to selected I/O port
	 */

	/*
	 * Copies the value from the second operand (source operand) to the I/O port
	 * specified with the destination operand (first operand).
	 */
	public void executeOUT() {
		BitString destBS = mIR.substring(4, 3);
		BitString sourceBS = mIR.substring(7, 3);
		BitString copyOfValue = (mRegisters[destBS.getValue()] = mRegisters[sourceBS.getValue()].copy());
		System.out.println(copyOfValue);
	}

	public void executeHalt() {
		System.exit(1);
	}

	public void setCC(int theValue) {
		if (theValue < 0) {
			mCC.setBits(new char[] { '1', '0', '0' });
		} else if (theValue < 0) {
			mCC.setBits(new char[] { '0', '1', '0' });
		} else {
			mCC.setBits(new char[] { '0', '0', '1' });
		}
	}

	public String getMemory(int theLocation) {
		String value = "";
		for (char c :mRegisters[theLocation].getBits()){
			value += c;
		}
		return value ;
	}

	/**
	 * This method will execute all the instructions starting at address 0 till HALT
	 * instruction is encountered.
	 */
	public void execute() {
		BitString opCodeStr;
		int opCode;
		BitString trapStr;
		int trapCode;

		while (true) {
			// Fetch the instruction
			mIR = mMemory[mPC.getValue()];
			mPC.addOne();

			// Decode the instruction's first 4 bits
			// to figure out the opcode
			opCodeStr = mIR.substring(0, 4);
			opCode = opCodeStr.getValue();
			trapStr = mIR.substring(7, 8);
			trapCode = trapStr.getValue();

			// 1111 0000 0010 0101 ;HALT 1+4+32
			// 1111 0000 0010 0001 ;OUT
			// 1+2+4+8 = 15

			// What instruction is this?
			if (opCode == 0) { // NOT
				executeBR();
				return;
			} else if (opCode == 1) { // NOT
				executeAdd();
				return;
			} else if (opCode == 2) { // NOT
				executeLD();
				return;
			} else if (opCode == 5) { // NOT
				executeAnd();
				return;
			} else if (opCode == 9) { // NOT
				executeNot();
				return;
			} else if (opCode == 15 && trapCode == 37) {
				executeHalt();
				return;
			} else if (opCode == 15 && trapCode == 32) {
				executeOUT();
				return;
			}
		}
	}

	/**
	 * Displays the computer's state
	 */
	public void display() {
		System.out.print("\nPC ");
		mPC.display(true);
		System.out.print("   ");

		System.out.print("IR ");
		mPC.display(true);
		System.out.print("   ");

		System.out.print("CC ");
		mCC.display(true);
		System.out.println("   ");

		for (int i = 0; i < MAX_REGISTERS; i++) {
			System.out.printf("R%d ", i);
			mRegisters[i].display(true);
			if (i % 3 == 2) {
				System.out.println();
			} else {
				System.out.print("   ");
			}
		}
		System.out.println();

		for (int i = 0; i < MAX_MEMORY; i++) {
			System.out.printf("%3d ", i);
			mMemory[i].display(true);
			if (i % 3 == 2) {
				System.out.println();
			} else {
				System.out.print("   ");
			}
		}
		System.out.println();

	}
}
