import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import javax.swing.JPanel;

public class MandelPanel extends JPanel {
    public double xMin = -2, xMax = 2, yMin = -2, yMax = 2;
    public int MAX_ITERS = 200;
    public int numThreads = 1;
    public boolean antiAliasing = false;
    public int[] colors = new int[200];
    public int w, h;
    public int[] pixels;
    public boolean initialized = false;
    
    class PartialPainter implements Runnable {
        public int numPainters;
        public int myNum;
        
        public PartialPainter(int numPainters, int myNum) {
            this.numPainters = numPainters;
            this.myNum = myNum;
        }
        
        @Override
        public void run() {
            if (antiAliasing) {
                paintAAOn();
            } else {
                paintAAOff();
            }
        }
        
        public void paintAAOn() {
            int index = myNum;
            int i, j;
            double x, y;
            double pixW = (xMax - xMin) / w;
            double pixH = (yMax - yMin) / h;
            while (index < pixels.length) {
                i = index / w;
                j = index % w;
                y = yMin + i * pixH;
                x = xMin + j * pixW;
                pixels[index] = AAColor(x, y, pixW, pixH);
                index += numPainters;
            }
        }
        
        public void paintAAOff() {
            int index = myNum;
            int numIters;
            int i, j;
            double x, y;
            double pixW = (xMax - xMin) / w;
            double pixH = (yMax - yMin) / h;
            while (index < pixels.length) {
                i = index / w;
                j = index % w;
                y = yMin + i * pixH;
                x = xMin + j * pixW;
                numIters = numIters(x, y);
                if (numIters == -1) {
                    pixels[index] = (255 << 24) | Integer.MIN_VALUE;
                } else {
                    pixels[index] = colors[numIters % 200];
                }
                index += numPainters;
            }          
        }
    }
        
    public void initialize() {

        for (int i = 0; i < 200; ++i) {
            float hue = i / 200.0f;
            colors[i] = Color.HSBtoRGB(hue, 0.9f, 0.9f);
        }
        /*
        int ltBlue = Color.cyan.getRGB();
        int dkBlue = Color.blue.getRGB();
        int orange = Color.orange.getRGB();
        for (int i = 0; i < 100; ++i) {
            colors[i] = gradient(ltBlue, dkBlue, i / 100.0f);
            colors[i + 100] = gradient(dkBlue, orange, i / 100.0f);
        }
        */
        w = this.getWidth();
        h = this.getHeight();
        pixels = new int[w * h];
        initialized = true;
    }
    
    public int gradient(int color1, int color2, float val) {
        int alpha = 255 << 24;
        
        int r1 = (color1 >> 16) & 0xff;
        int g1 = (color1 >> 8) & 0xff;
        int b1 = color1 & 0xff;
        
        int r2 = (color2 >> 16) & 0xff;
        int g2 = (color2 >> 8) & 0xff;
        int b2 = color2 & 0xff;
        
        int r = (int) (r1 + val * (r2 - r1));
        int g = (int) (g1 + val * (g2 - g1));
        int b = (int) (b1 + val * (b2 - b1));
        
        return alpha | (r << 16) | (g << 8) | b;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        if (!initialized) {
            initialize();
        }
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; ++i) {
            threads[i] = new Thread(new PartialPainter(numThreads, i));
            threads[i].start();
        }
        for (int i = 0; i < numThreads; ++i) {
            try {
                threads[i].join();
            } catch (Exception ex) {
                //Ignore
            }
        }
        Image img = createImage(new MemoryImageSource(w, h, pixels, 0, w));
        g.drawImage(img, 0, 0, null);   
    }
    
    public void paintThreadOffAAOff(Graphics g) {
        int index = 0;
        int numIters;
        double x, y;
        double pixW = (xMax - xMin) / w;
        double pixH = (yMax - yMin) / h;
        for (int i = 0; i < w; ++i) {            
            y = yMin + i * pixH;
            for (int j = 0; j < h; ++j) {
                x = xMin + j * pixW;
                numIters = numIters(x, y);
                if (numIters == -1) {
                    pixels[index++] = (255 << 24) | Integer.MIN_VALUE;
                } else {
                    pixels[index++] = colors[numIters % 200];
                }
            }
        }
        Image img = createImage(new MemoryImageSource(w, h, pixels, 0, w));
        g.drawImage(img, 0, 0, null);
    }
        
    public int AAColor(double x, double y, double pixW, double pixH) {
        double x1, x2, x3, x4, y1, y2, y3, y4;
        x1 = x3 = x + (pixW / 4);
        x2 = x4 = x + 3 * (pixW / 4);
        y1 = y2 = y + (pixH / 4);
        y3 = y4 = y + 3 * (pixH / 4);
        int iters1 = numIters(x1, y1);
        int iters2 = numIters(x2, y2);
        int iters3 = numIters(x3, y3);
        int iters4 = numIters(x4, y4);
        int color1 = iters1 == -1 ? (255 << 24) | Integer.MIN_VALUE : colors[iters1 % colors.length];
        int color2 = iters2 == -1 ? (255 << 24) | Integer.MIN_VALUE : colors[iters2 % colors.length];
        int color3 = iters3 == -1 ? (255 << 24) | Integer.MIN_VALUE : colors[iters3 % colors.length];
        int color4 = iters4 == -1 ? (255 << 24) | Integer.MIN_VALUE : colors[iters4 % colors.length];
        return averageColor(color1, color2, color3, color4);
    }
    
    public int averageColor(int color1, int color2, int color3, int color4) {
        int r1, r2, r3, r4, g1, g2, g3, g4, b1, b2, b3, b4, r, g, b;
        r1 = (color1 >> 16) & 0xff;
        r2 = (color2 >> 16) & 0xff;
        r3 = (color3 >> 16) & 0xff;
        r4 = (color4 >> 16) & 0xff;
        g1 = (color1 >> 8) & 0xff;
        g2 = (color2 >> 8) & 0xff;
        g3 = (color3 >> 8) & 0xff;
        g4 = (color4 >> 8) & 0xff;
        b1 = color1 & 0xff;
        b2 = color2 & 0xff;
        b3 = color3 & 0xff;
        b4 = color4 & 0xff;
        r = (r1 + r2 + r3 + r4) / 4;
        g = (g1 + g2 + g3 + g4) / 4;
        b = (b1 + b2 + b3 + b4) / 4;
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }
    
    public int numIters(double x, double y) {
        // Mandelbrot set logic
        double re = 0;
        double im = 0;
        double tempRe;
        for (int i = 0; i < MAX_ITERS; ++i) {
            if (Math.abs(re) > 10 || Math.abs(im) > 10) {
                return i;
            } else {
                tempRe = re;
                re = re * re - im * im + x;
                im = 2 * tempRe * im + y;
            }
        }
        return -1;
        /*
        // Julia set logic
        double tempX;
        for (int i = 0; i < MAX_ITERS; ++i) {
            if (Math.abs(x) > 10 || Math.abs(y) > 10) {
                return i;
            } else {
                tempX = x;
                x = x * x - y * y - 0.4;
                y = 2 * tempX * y + 0.6;
            }
        }
        return -1;
        */
    }
}
