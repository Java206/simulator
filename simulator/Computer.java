package simulator;
//Keagan James Anderson, Abrham Takele, Yavuzalp Turkoglu

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
			answer.setValue2sComp(mRegisters[sourceBS.getValue()].getValue2sComp()
					+ mRegisters[sourceBS2.getValue()].getValue2sComp());
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
			binaryNumber = binaryNumber.append(mIR.substring(11, 5));
			BitString answer = new BitString();
			char[] andChar = new char[16];
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
			char[] andChar = new char[16];
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
		mRegisters[destBS.getValue()] = mMemory[mPC.getValue() + mIR.substring(7, 9).getValue2sComp()];
	}

	public void executeBR() {
		BitString brN = mIR.substring(4, 1);
		BitString brZ = mIR.substring(5, 1);
		BitString brP = mIR.substring(6, 1);
		BitString currentN = mCC.substring(0, 1);
		BitString currentZ = mCC.substring(1, 1);
		BitString currentP = mCC.substring(2, 1);
		BitString offSet = mIR.substring(7, 9);

		if ((brN.getValue() == currentN.getValue() && brN.getValue() == 1)
				|| (brZ.getValue() == currentZ.getValue() && brZ.getValue() == 1)
				|| (brP.getValue() == currentP.getValue() && brP.getValue() == 1)) {
			mPC.setValue(mPC.getValue() + offSet.getValue2sComp() - 1);
		}
	}

	public void executeOUT() {
		BitString destBS = mIR.substring(4, 3);
		System.out.print((char) mRegisters[destBS.getValue()].substring(8, 8).getValue());
	}

	public void executeHalt() {
		System.out.println("Halting!");
	}

	public void setCC(int theValue) {
		if (theValue < 0) {
			mCC.setBits(new char[] { '1', '0', '0' });
		} else if (theValue == 0) {
			mCC.setBits(new char[] { '0', '1', '0' });
		} else {
			mCC.setBits(new char[] { '0', '0', '1' });
		}
	}

	public String getRegistry(int theLocation) {
		String value = "";
		for (char c : mRegisters[theLocation].getBits()) {
			value += c;
		}
		return value;
	}

	public String getCC() {
		return "" + mCC.getValue();
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
		Boolean flag = true;

		while (flag) {
			if (mPC.getValue() == 50) {
				return;
			}
			// Fetch the instruction
			mIR = mMemory[mPC.getValue()];
			mPC.addOne();

			// Decode the instruction's first 4 bits
			// to figure out the opcode
			opCodeStr = mIR.substring(0, 4);
			opCode = opCodeStr.getValue();
			trapStr = mIR.substring(8, 8);
			trapCode = trapStr.getValue();

			// 1111 0000 0010 0101 ;HALT 1+4+32
			// 1111 0000 0010 0001 ;OUT
			// 1+2+4+8 = 15

			// What instruction is this?
			if (opCode == 0 && mIR.substring(4, 3).getValue() != 0) { // NOT
				executeBR();
			} else if (opCode == 1) { // NOT
				executeAdd();
			} else if (opCode == 2) { // NOT
				executeLD();
			} else if (opCode == 5) { // NOT
				executeAnd();
			} else if (opCode == 9) { // NOT
				executeNot();
			} else if (opCode == 15 && trapCode == 37) {
				executeHalt();
				flag = false;
				return;
			} else if (opCode == 15 && trapCode == 33) {
				executeOUT();
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
