import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class DrawPanel extends JFrame implements KeyListener
{
    public static final int     width     = 650;
    public static final int     height    = 330;
    Chip    chip      = null;

    private DrawCanvas canvas;

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
        canvas = new DrawCanvas();
        canvas.setPreferredSize(new Dimension(width, height));
        this.setContentPane(canvas);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);   // Handle the CLOSE button
        this.pack();              // Either pack() the components; or setSize()
        this.setTitle("Chip8-Emulator");
        this.setVisible(true);    // this JFrame show
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
                    repaint();
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e)
                    {
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
            for (int i = 0; i < 64; i++)
                for (int j = 0; j < 32; j++)
                    if (chip.graphics[i + 64 * j] != 0)
                        g.fillRect(i * 10, j * 10, 10, 10);
        }
    }

    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyChar())
        {
        case 'é':
            chip.keys[0] = 1;
            break;
        case '"':
            chip.keys[1] = 1;
            break;
        case '\'':
            chip.keys[2] = 1;
            break;
        case 'a':
            chip.keys[3] = 1;
            break;
        case 'z':
            chip.keys[4] = 1;
            break;
        case 'e':
            chip.keys[5] = 1;
            break;
        case 'r':
            chip.keys[6] = 1;
            break;
        case 'q':
            chip.keys[7] = 1;
            break;
        case 's':
            chip.keys[8] = 1;
            break;
        case 'd':
            chip.keys[9] = 1;
            break;
        case 'f':
            chip.keys[10] = 1;
            break;
        case 'g':
            chip.keys[11] = 1;
            break;
        case 'w':
            chip.keys[12] = 1;
            break;
        case 'x':
            chip.keys[13] = 1;
            break;
        case 'c':
            chip.keys[14] = 1;
            break;
        case 'v':
            chip.keys[15] = 1;
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
