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
		BitString check = mIR.substring(13, 1);
		BitString destBS = mIR.substring(4, 3);
		BitString sourceBS = mIR.substring(7, 3);
		if (check.getValue() == 1) {
			BitString binaryNumber = mIR.substring(14, 5);
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

	public void excuteAnd() {
		System.out.println();
	}

	public void excuteLD() {
		System.out.println();
	}

	public void excuteBR() {
		System.out.println();
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

	/**
	 * This method will execute all the instructions starting at address 0 till HALT
	 * instruction is encountered.
	 */
	public void execute() {
		BitString opCodeStr;
		int opCode;

		while (true) {
			// Fetch the instruction
			mIR = mMemory[mPC.getValue()];
			mPC.addOne();

			// Decode the instruction's first 4 bits
			// to figure out the opcode
			opCodeStr = mIR.substring(0, 4);
			opCode = opCodeStr.getValue();

			// What instruction is this?
			if (opCode == 9) { // NOT
				executeNot();
				return; // TODO - Remove this once you add other instructions.
			} else if (opCode == 1) { // NOT
				executeAdd();
				return; // TODO - Remove this once you add other instructions.
			}
			// TODO - Others
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
