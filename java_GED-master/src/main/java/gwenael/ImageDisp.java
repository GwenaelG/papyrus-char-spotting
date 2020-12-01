package gwenael;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.Buffer;
import javax.imageio.*;
import javax.swing.*;

/*
taken from: https://alvinalexander.com/blog/post/jfc-swing/complete-java-program-code-open-read-display-image-file/
 */
public class ImageDisp {
	public ImageDisp(final String filename) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame editorFrame = new JFrame("ImageDemo");
				editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

				BufferedImage image = null;
				try {
					image = ImageIO.read(new File(filename));
				} catch (Exception e) {
					e.printStackTrace();
				}
				ImageIcon imageIcon = new ImageIcon(image);
				JLabel jLabel = new JLabel();
				jLabel.setIcon(imageIcon);
				editorFrame.getContentPane().add(jLabel, BorderLayout.CENTER);

				editorFrame.pack();
				editorFrame.setLocationRelativeTo(null);
				editorFrame.setVisible(true);
			}
		});
	}

	public ImageDisp(BufferedImage img) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame editorFrame = new JFrame("ImageDemo");
				editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

				ImageIcon imageIcon = new ImageIcon(img);
				JLabel jLabel = new JLabel();
				jLabel.setIcon(imageIcon);
				editorFrame.getContentPane().add(jLabel, BorderLayout.CENTER);

				editorFrame.pack();
				editorFrame.setLocationRelativeTo(null);
				editorFrame.setVisible(true);
			}
		});
	}
}
