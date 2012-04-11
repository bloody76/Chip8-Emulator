import java.util.Random;

public class Chip
{
	
	private char[] mem = new char[4096];
	private short pc = 0x200;
	private short opcode = 0;
	private short I = 0;
	private char[] register = new char[16];
	
	private char delayTimer = 0;
	private char soundTimer = 0;
	private char graphics[] = new char[64 * 32];
	
	private short stack[] = new short[16];
	private short sp = 0;
	private char chip8_fontset[] =
	{ 
	  0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
	  0x20, 0x60, 0x20, 0x20, 0x70, // 1
	  0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
	  0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
	  0x90, 0x90, 0xF0, 0x10, 0x10, // 4
	  0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
	  0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
	  0xF0, 0x10, 0x20, 0x40, 0x40, // 7
	  0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
	  0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
	  0xF0, 0x90, 0xF0, 0x90, 0x90, // A
	  0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
	  0xF0, 0x80, 0x80, 0x80, 0xF0, // C
	  0xE0, 0x90, 0x90, 0x90, 0xE0, // D
	  0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
	  0xF0, 0x80, 0xF0, 0x80, 0x80  // F
	};
	private char[] keys = new char[16];
	
	public void initializeChip()
	{
		for (int i = 0; i < 4096; i++)
			mem[i] = 0;
		pc = 0x200;
		opcode = 0;
		I = 0;
		for (int i = 0; i < 16; i++)
			register[i] = 0;
		delayTimer = 0;
		soundTimer = 0;
		for (int i = 0; i < 64 * 32; i++)
			graphics[i] = 0;
		for (int i = 0; i < 16; i++)
			stack[i] = 0;
		sp = 0;
		for (int i = 0; i < 16; i++)
			keys[i] = 0;
		
		for(int i = 0; i < 80; ++i)
			mem[i] = chip8_fontset[i];
		
	}
	
	public void cycle()
	{
		//Fetch
		opcode = (short) (mem[pc] << 8 | mem[pc + 1]);
		
		//Decode
		switch (opcode & 0xF000)
		{
		case 0x0000:
			switch (opcode & 0xFF00)
			{
			case 0x0000:
				if (opcode == 0x00E0) //Clear Screen.
					for (int i = 0; i < 64 * 32; i++)
						graphics[i] = 0;
				if (opcode == 0x00EE) //Return from subroutine.
					pc = stack[--sp];
				break;
			default:
				break;
			}
			pc += 2;
			break;
		case 0x1000: // Jump to to NNN from 0x1NNN.
			pc = (short) (opcode & 0x0FFF);
			pc += 2;
			break;
		case 0x2000: // Call subroutines at address NNN from 0x2NNN.
			stack[sp++] = (short) pc;
			pc = (short) (opcode & 0x0FFF);
			break;
		case 0x3000:
			if (register[opcode & 0x0F00 >> 8] == (opcode & 0x00FF))
				pc += 2;
			pc += 2;
			break;
		case 0x4000:
			if (register[opcode & 0x0F00 >> 8] != (opcode & 0x00FF))
				pc += 2;
			pc += 2;
			break;
		case 0x5000:
			if (register[opcode & 0x0F00 >> 8] == register[opcode & 0x00F0 >> 4])
				pc += 2;
			pc += 2;
			break;
		case 0x6000:
			register[opcode & 0x0F00 >> 8] = (char) (opcode & 0x00FF);
			pc += 2;
			break;
		case 0x7000:
			register[opcode & 0x0F00 >> 8] += (char) (opcode & 0x00FF);
			pc += 2;
			break;
		case 0x8000:
			switch (opcode & 0x000F)
			{
			case 0x0: // 0x8XY0: Sets VX to the value of VY.
				register[opcode & 0x0F00 >> 8] = register[opcode & 0x00F0 >> 4];
				break;
			case 0x1: //8XY1: Sets VX to VX or VY.
				register[opcode & 0x0F00 >> 8] |= register[opcode & 0x00F0 >> 4];
				break;
			case 0x2: //8XY2: Sets VX to VX and VY.
				register[opcode & 0x0F00 >> 8] &= register[opcode & 0x00F0 >> 4];
				break;
			case 0x3: //8XY3: Sets VX to VX xor VY.
				register[opcode & 0x0F00 >> 8] ^= register[opcode & 0x00F0 >> 4];
				break;
			case 0x4:
				if(register[(opcode & 0x00F0) >> 4] > (0xFF - register[(opcode & 0x0F00) >> 8]))
				    register[0xF] = 1; //carry
				  else
					  register[0xF] = 0;
				register[(opcode & 0x0F00) >> 8] += register[(opcode & 0x00F0) >> 4];
				break;
			case 0x5:
				if(register[(opcode & 0x00F0) >> 4] > register[(opcode & 0x0F00) >> 8]) 
					register[0xF] = 0; // there is a borrow
				else 
					register[0xF] = 1;					
				register[(opcode & 0x0F00) >> 8] -= register[(opcode & 0x00F0) >> 4];
				break;
			case 0x6:
				register[0xF] = (char) (register[opcode & 0x0F00 >> 8] & 0x1);
				register[opcode & 0x0F00 >> 8] >>= 1;
				break;
			case 0x7:
				if(register[(opcode & 0x0F00) >> 8] > register[(opcode & 0x00F0) >> 4]) 
					register[0xF] = 0; // there is a borrow
				else 
					register[0xF] = 1;					
				register[(opcode & 0x00F0) >> 4] -= register[(opcode & 0x0F00) >> 8];
				break;
			case 0xE:
				register[0xF] = (char) (register[(opcode & 0x0F00) >> 8] & 0x8000);
				register[(opcode & 0x0F00) >> 8] <<= 1;
				break;
			}
			pc += 2;
			break;
		case 0x9000:
			if (register[(opcode & 0x0F00) >> 8] != register[(opcode & 0x00F0) >> 4])
				pc += 2;
			pc += 2;
			break;
		case 0xA000:
			I = (short) (opcode & 0x0FFF);
			pc += 2;
			break;
		case 0xB000:
			pc = (short) ((opcode & 0x0FFF) + register[0x0]);
			break;
		case 0xC000:
			Random r = new Random();
			register[(opcode & 0x0F00) >> 8] = (char) (r.nextInt() & (opcode & 0x00FF));
			pc += 2;
			break;
		case 0xD000:
			short x = (short) register[(opcode & 0x0F00) >> 8];
			short y = (short) register[(opcode & 0x00F0) >> 4];
			short height = (short) (opcode & 0x000F);
			short pixel;

			register[0xF] = 0;
			for (int yline = 0; yline < height; yline++)
			{
				pixel = (short) mem[I + yline];
				for(int xline = 0; xline < 8; xline++)
				{
					if((pixel & (0x80 >> xline)) != 0)
					{
						if(graphics[(x + xline + ((y + yline) * 64))] == 1)
						{
							register[0xF] = 1;                                    
						}
						graphics[x + xline + ((y + yline) * 64)] ^= 1;
					}
				}
			}		
			pc += 2;
			break;
		case 0xE000:
			switch (opcode & 0x00FF)
			{
			case 0x009E:
				if (keys[register[(opcode & 0x0F00) >> 8]] == 1)
					pc += 2;
				pc += 2;
				break;
			case 0x00A1:
				if (keys[register[(opcode & 0x0F00) >> 8]] == 0)
					pc += 2;
				pc += 2;
				break;
			}
		case 0xF000:
			switch (opcode & 0x00FF)
			{
			case 0x0007:
				register[(opcode & 0x0F00) >> 8] = delayTimer;
				pc += 2;
				break;
			case 0x000A:
				//TODO: Must handle the keys.
				pc += 2;
				break;
			case 0x0015:
				delayTimer = register[(opcode & 0x0F00) >> 8];
				pc += 2;
				break;
			case 0x0018:
				soundTimer = register[(opcode & 0x0F00) >> 8];
				pc += 2;
				break;
			case 0x001E:
				if (I + register[(opcode & 0x0F00) >> 8] > 0xFFF)
					register[0xF] = 1;
				else
					register[0xF] = 0;
				I += register[(opcode & 0x0F00) >> 8];
				pc += 2;
				break;
			case 0x0029:
				I = (short) (register[(opcode & 0x0F00) >> 8] * 0x5);
				pc += 2;
				break;
			case 0x0033:
				mem[I]     = (char) (register[(opcode & 0x0F00) >> 8] / 100);
				mem[I + 1] = (char) ((register[(opcode & 0x0F00) >> 8] / 10) % 10);
				mem[I + 2] = (char) ((register[(opcode & 0x0F00) >> 8] % 100) % 10);					
				pc += 2;
				break;
			case 0x0055:
				for (int i = 0; i < 0xF; i++)
					mem[I + i] = register[i];
				pc += 2;
				break;
			case 0x0065:
				for (int i = 0; i < 0xF; i++)
					register[i] = mem[I + i];
				pc += 2;
				break;
			}
			break;
		default:
			System.err.print("Unknown opcode: " + opcode);	
		}
	}
	
	public void run()
	{
		initializeChip();
		
		for (;;)
		{
			cycle();
		}
	}
}
