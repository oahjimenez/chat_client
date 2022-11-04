package chatroom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class GradientPanel extends JPanel {

	private static final long serialVersionUID = -4117447462742022152L;

	public GradientPanel(GridLayout layout) {
		super(layout);
	}

	public GradientPanel(BorderLayout layout) {
		super(layout);
	}

	public GradientPanel(boolean layout) {
		super(layout);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		int w = getWidth(), h = getHeight();
		Color color1 = new Color(27, 2, 117);
		Color color2 = new Color(125, 1, 111);
		GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, w, h);
	}

}
