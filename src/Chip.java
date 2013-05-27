import java.util.Random;
import java.io.*;

public class Chip
{

    private int[] mem = new int[4096];
    private int pc = 0x200;
    private int op = 0;
    private int I = 0;
    private int[] reg = new int[16];

    private int delayTimer = 0;
    private int soundTimer = 0;
    public char graphics[] = new char[64 * 32];

    private int stack[] = new int[16];
    private int sp = 0;
    private short chip8_fontset[] =
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
    public int[] keys = new int[16];

    public void InitializeChip()
    {
        for (int i = 0; i < 4096; i++)
            mem[i] = 0;
        pc = 0x200;
        op = 0;
        I = 0;
        for (int i = 0; i < 16; i++)
            reg[i] = 0;
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

    public void loadROM(String file) throws Exception
    {
        FileInputStream fs = new FileInputStream(file);
        int c = fs.read();
        for (int i = 0; c != -1; ++i)
        {
            mem[i + 0x200] = c;
            c = fs.read();
        }
        fs.close();
    }

    public void Cycle()
    {
        //Fetch
        op = mem[pc] << 8 | mem[pc + 1];

        System.out.println("op: " + Integer.toHexString(op));
        //Decode
        switch (op & 0xF000)
        {
        case 0x0000:
            switch (op & 0xFF00)
            {
            case 0x0000:
                if (op == 0x00E0) //Clear Screen.
                    for (int i = 0; i < 64 * 32; ++i)
                        graphics[i] = 0;
                if (op == 0x00EE) //Return from subroutine.
                    pc = stack[--sp];
                break;
            default:
                break;
            }
            pc += 2;
            break;
        case 0x1000: // Jump to to NNN from 0x1NNN.
            pc = (int) (op & 0x0FFF);
            break;
        case 0x2000: // Call subroutines at address NNN from 0x2NNN.
            stack[sp++] = (int) pc;
            pc = (int) (op & 0x0FFF);
            break;
        case 0x3000: // 3XNN : Skips the next instruction if VX equals NN.
            if (reg[(op & 0x0F00) >> 8] == (op & 0x00FF))
                pc += 2;
            pc += 2;
            break;
        case 0x4000: // 4XNN : Skips the next instruction if VX doesn't equal NN.
            if (reg[(op & 0x0F00) >> 8] != (op & 0x00FF))
                pc += 2;
            pc += 2;
            break;
        case 0x5000: // 5XY0 : Skips the next instruction if VX equals VY.
            if (reg[(op & 0x0F00) >> 8] == reg[(op & 0x00F0) >> 4])
                pc += 2;
            pc += 2;
            break;
        case 0x6000: // 6XNN : Sets VX to NN.
            reg[(op & 0x0F00) >> 8] = (int) (op & 0x00FF);
            pc += 2;
            break;
        case 0x7000: // 7XNN : Adds NN to VX.
            reg[(op & 0x0F00) >> 8] += (int) (op & 0x00FF);
            reg[(op & 0x0F00) >> 8] &= 0x00FF;
            pc += 2;
            break;
        case 0x8000:
            switch (op & 0x000F)
            {
            case 0x0: // 8XY0 : Sets VX to the value of VY.
                reg[(op & 0x0F00) >> 8] = reg[(op & 0x00F0) >> 4];
                break;
            case 0x1: // 8XY1 : Sets VX to VX or VY.
                reg[(op & 0x0F00) >> 8] |= reg[(op & 0x00F0) >> 4];
                break;
            case 0x2: // 8XY2 : Sets VX to VX and VY.
                reg[(op & 0x0F00) >> 8] &= reg[(op & 0x00F0) >> 4];
                break;
            case 0x3: // 8XY3 : Sets VX to VX xor VY.
                reg[(op & 0x0F00) >> 8] ^= reg[(op & 0x00F0) >> 4];
                break;
            case 0x4: // 8XY4 : Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                if(reg[(op & 0x00F0) >> 4] > (0xFF - (reg[(op & 0x0F00) >> 8])))
                    reg[0xF] = 1; // carry
                  else
                    reg[0xF] = 0;
                reg[(op & 0x0F00) >> 8] += reg[(op & 0x00F0) >> 4];
                reg[(op & 0x0F00) >> 8] &= 0x00FF;
                break;
            case 0x5: // 8XY5 : VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                if(reg[(op & 0x00F0) >> 4] > reg[(op & 0x0F00) >> 8])
                    reg[0xF] = 0; // there is a borrow
                else
                    reg[0xF] = 1;
                reg[(op & 0x0F00) >> 8] -= reg[(op & 0x00F0) >> 4];
                reg[(op & 0x0F00) >> 8] &= 0x00FF;
                break;
            case 0x6: // 8XY6 : Shifts VX right by one. VF is set to the value of the least significant bit of VX before the shift.
                reg[0xF] = (int) (reg[(op & 0x0F00) >> 8] & 0x1);
                reg[(op & 0x0F00) >> 8] >>= 1;
                break;
            case 0x7: // 8XY7 : Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                if(reg[(op & 0x0F00) >> 8] > reg[(op & 0x00F0) >> 4])
                    reg[0xF] = 0; // there is a borrow
                else
                    reg[0xF] = 1;
                reg[(op & 0x00F0) >> 4] -= reg[(op & 0x0F00) >> 8];
                reg[(op & 0x00F0) >> 4] &= 0x00FF;
                break;
            case 0xE: // 8XYE : Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift.
                reg[0xF] = (int) (reg[(op & 0x0F00) >> 8] & 0x8000);
                reg[(op & 0x0F00) >> 8] <<= 1;
                reg[(op & 0x0F00) >> 8] &= 0x00FF;
                break;
            }
            pc += 2;
            break;
        case 0x9000: // 9XY0 : Skips the next instruction if VX doesn't equal VY.
            if (reg[(op & 0x0F00) >> 8] != reg[(op & 0x00F0) >> 4])
                pc += 2;
            pc += 2;
            break;
        case 0xA000: // ANNN : Sets I to the address NNN.
            I = (int) (op & 0x0FFF);
            pc += 2;
            break;
        case 0xB000: // BNNN : Jumps to the address NNN plus V0.
            pc = (int) ((op & 0x0FFF) + reg[0x0]);
            break;
        case 0xC000: // CXNN : Sets VX to a random number and NN.
            Random r = new Random();
            reg[(op & 0x0F00) >> 8] = (int) ((r.nextInt() & op) & 0x00FF);
            pc += 2;
            break;
        case 0xD000: // DXYN : Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels. Each row of 8 pixels is read as bit-coded (with the most significant bit of each byte displayed on the left) starting from memory location I; I value doesn't change after the execution of this instruction. As described above, VF is set to 1 if any screen pixels are flipped from set to unset when the sprite is drawn, and to 0 if that doesn't happen.
            int x = (int) reg[(op & 0x0F00) >> 8];
            int y = (int) reg[(op & 0x00F0) >> 4];
            int height = (int) (op & 0x000F);
            int pixel;

            reg[0xF] = 0;
            for (int yline = 0; yline < height; yline++)
            {
                pixel = (int) mem[I + yline];
                for(int xline = 0; xline < 8; xline++)
                {
                    if((pixel & (0x80 >> xline)) != 0)
                    {
                        //This line should not exists.
                        if ((x + xline + ((y + yline) * 64)) >= 2048)
                            continue;
                        if(graphics[(x + xline + ((y + yline) * 64))] == 1)
                        {
                            reg[0xF] = 1;
                        }
                        graphics[x + xline + ((y + yline) * 64)] ^= 1;
                    }
                }
            }
            pc += 2;
            break;
        case 0xE000:
            switch (op & 0x00FF)
            {
                case 0x009E: // EX9E : Skips the next instruction if the key stored in VX is pressed.
                if (keys[reg[(op & 0x0F00) >> 8]] == 1)
                    pc += 2;
                pc += 2;
                break;
                case 0x00A1: // EXA1 : Skips the next instruction if the key stored in VX isn't pressed.
                if (keys[reg[(op & 0x0F00) >> 8]] == 0)
                    pc += 2;
                else
                    keys[reg[(op & 0x0F00) >> 8]] = 0;
                pc += 2;
                break;
            }
        case 0xF000:
            switch (op & 0x00FF)
            {
            case 0x0007: // FX07 : Sets VX to the value of the delay timer.
                reg[(op & 0x0F00) >> 8] = delayTimer & 0xFF;
                pc += 2;
                break;
            case 0x000A: // FX0A : A key press is awaited, and then stored in VX.
                pc -= 2;
                for (int i = 0; i < 16; i++)
                    if (keys[i] == 1)
                    {
                        reg[(op & 0x0F00) >> 8] = i;
                        keys[i] = 0;
                        pc += 2;
                        break;
                    }
                pc += 2;
                break;
            case 0x0015: // FX15 : Sets the delay timer to VX.
                delayTimer = reg[(op & 0x0F00) >> 8];
                pc += 2;
                break;
            case 0x0018: // FX18 : Sets the sound timer to VX.
                soundTimer = reg[(op & 0x0F00) >> 8];
                pc += 2;
                break;
            case 0x001E: // FX1E : Adds VX to I.
                if (I + reg[(op & 0x0F00) >> 8] > 0xFFF)
                    reg[0xF] = 1;
                else
                    reg[0xF] = 0;
                I += reg[(op & 0x0F00) >> 8];
                I &= 0x0FFF;
                pc += 2;
                break;
            case 0x0029: // FX29 : Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font.
                I = (int) (reg[(op & 0x0F00) >> 8] * 0x5);
                pc += 2;
                break;
            case 0x0033: // FX33 : Stores the Binary-coded decimal representation of VX, with the most significant of three digits at the address in I, the middle digit at I plus 1, and the least significant digit at I plus 2. (In other words, take the decimal representation of VX, place the hundreds digit in memory at location in I, the tens digit at location I+1, and the ones digit at location I+2.)
                mem[I]     = (int) (reg[(op & 0x0F00) >> 8] / 100);
                mem[I + 1] = (int) ((reg[(op & 0x0F00) >> 8] / 10) % 10);
                mem[I + 2] = (int) ((reg[(op & 0x0F00) >> 8] % 100) % 10);
                pc += 2;
                break;
            case 0x0055: // FX55 : Stores V0 to VX in memory starting at address I.
                for (int i = 0; i <= ((op & 0x0F00) >> 8); ++i)
                    mem[I + i] = reg[i];
                pc += 2;
                break;
            case 0x0065: // FX65 : Fills V0 to VX with values from memory starting at address I.
                for (int i = 0; i <= ((op & 0x0F00) >> 8); ++i)
                    reg[i] = mem[I + i];
                pc += 2;
                break;
            }
            break;
        default:
            System.err.print("Unknown op: " + op);
        }
    }

    public void PowerOn(String file)
    {
        InitializeChip();
        try
        {
            loadROM(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void Step()
    {
        Cycle();

        if (soundTimer > 0)
            soundTimer--;
        if (delayTimer > 0)
            delayTimer--;
    }
}
