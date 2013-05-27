package Chip8;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class DrawPanel extends JFrame implements KeyListener
{
    private static final int     width     = 650;
    private static final int     height    = 330;
    private Chip                 chip      = null;
    private DrawCanvas           canvas    = null;

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("You need to specify the rom location.");
            return;
        }
        final String rom = args[0];
        SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        DrawPanel dp = new DrawPanel(rom);
                    }
                });
    }

    public DrawPanel(String file)
    {
        this.canvas = new DrawCanvas();
        this.canvas.setPreferredSize(new Dimension(width, height));
        this.setContentPane(canvas);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE); // Handle the CLOSE button
        this.pack();                                  // Either pack() the components; or setSize()
        this.setTitle("Chip8-Emulator");
        this.setVisible(true);                        // this JFrame show
        this.addKeyListener(this);

        this.chip = new Chip();
        this.chip.PowerOn(file);

        Thread updateThread = new Thread()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    chip.Step();
                    if (chip.getSoundTimer() == 0)
                        getToolkit().beep();
                    repaint();
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e)
                    {
                        System.err.println("Error in sleeping the thread.");
                        e.printStackTrace();
                    }
                }
            }
        };
        updateThread.start();
    }

    private class DrawCanvas extends JPanel
    {
        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            g.setColor(Color.black);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.green);
            for (int i = 0; i < 64; ++i)
                for (int j = 0; j < 32; ++j)
                    if (chip.getGraphics()[i + 64 * j] != 0)
                        g.fillRect(i * 10, j * 10, 10, 10);
        }
    }

    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyChar())
        {
        case 'é':
            chip.setKey(0);
            break;
        case '"':
            chip.setKey(1);
            break;
        case '\'':
            chip.setKey(2);
            break;
        case 'a':
            chip.setKey(3);
            break;
        case 'z':
            chip.setKey(4);
            break;
        case 'e':
            chip.setKey(5);
            break;
        case 'r':
            chip.setKey(6);
            break;
        case 'q':
            chip.setKey(7);
            break;
        case 's':
            chip.setKey(8);
            break;
        case 'd':
            chip.setKey(9);
            break;
        case 'f':
            chip.setKey(10);
            break;
        case 'g':
            chip.setKey(11);
            break;
        case 'w':
            chip.setKey(12);
            break;
        case 'x':
            chip.setKey(13);
            break;
        case 'c':
            chip.setKey(14);
            break;
        case 'v':
            chip.setKey(15);
            break;
        default:
            break;
        }
    }

    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent arg0)
    {
        // TODO Auto-generated method stub
    }
}
