import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

public class Main
{
	public static void main(String[] args)
	{
		DrawPanel dp = new DrawPanel();
		dp.go();
	}
}

@SuppressWarnings("serial")
class DrawPanel extends JPanel implements KeyListener
{
	int		width		= 650;
	int		height	= 330;
	Chip	chip		= null;

	public void go()
	{
		JFrame frame = new JFrame();
		chip = new Chip();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
		frame.setVisible(true);
		frame.getContentPane().add(this, java.awt.BorderLayout.CENTER);
		frame.addKeyListener(this);
		
		chip.run(frame);
	}

	public void paint(Graphics g)
	{
		g.setColor(Color.black);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.green);
		for (int i = 0; i < 64; i++)
			for (int j = 0; j < 32; j++)
				if (chip.graphics[i + 64 * j] != 0)
					g.fillRect(i * 10, j * 10, 10, 10);
	}// close paintComponent

	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyChar())
		{
		case '0':
			chip.keys[0] = 1;
			break;
		case '1':
			chip.keys[1] = 1;
			break;
		case '2':
			chip.keys[2] = 1;
			break;
		case '3':
			chip.keys[3] = 1;
			break;
		case '4':
			chip.keys[4] = 1;
			break;
		case '5':
			chip.keys[5] = 1;
			break;
		case '6':
			chip.keys[6] = 1;
			break;
		case '7':
			chip.keys[7] = 1;
			break;
		case '8':
			chip.keys[8] = 1;
			break;
		case '9':
			chip.keys[9] = 1;
			break;
		case 'a':
			chip.keys[10] = 1;
			break;
		case 'b':
			chip.keys[11] = 1;
			break;
		case 'c':
			chip.keys[12] = 1;
			break;
		case 'd':
			chip.keys[13] = 1;
			break;
		case 'e':
			chip.keys[14] = 1;
			break;
		case 'f':
			chip.keys[15] = 1;
			break;
		default:
			break;
		}
	}

	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyChar())
		{
		case '0':
			chip.keys[0] = 0;
			break;
		case '1':
			chip.keys[1] = 0;
			break;
		case '2':
			chip.keys[2] = 0;
			break;
		case '3':
			chip.keys[3] = 0;
			break;
		case '4':
			chip.keys[4] = 0;
			break;
		case '5':
			chip.keys[5] = 0;
			break;
		case '6':
			chip.keys[6] = 0;
			break;
		case '7':
			chip.keys[7] = 0;
			break;
		case '8':
			chip.keys[8] = 0;
			break;
		case '9':
			chip.keys[9] = 0;
			break;
		case 'a':
			chip.keys[10] = 0;
			break;
		case 'b':
			chip.keys[11] = 0;
			break;
		case 'c':
			chip.keys[12] = 0;
			break;
		case 'd':
			chip.keys[13] = 0;
			break;
		case 'e':
			chip.keys[14] = 0;
			break;
		case 'f':
			chip.keys[15] = 0;
			break;
		default:
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
}
