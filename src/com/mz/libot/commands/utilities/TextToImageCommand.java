package com.mz.libot.commands.utilities;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.math.NumberUtils;

import com.mz.libot.core.commands.Command;
import com.mz.libot.core.commands.CommandCategory;
import com.mz.libot.core.commands.exceptions.CommandException;
import com.mz.libot.core.commands.utils.Commands;
import com.mz.libot.core.commands.utils.Parameters;
import com.mz.libot.utils.StringUtils;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class TextToImageCommand extends Command {

	@Override
	public void execute(GuildMessageReceivedEvent event, Parameters params) throws Exception {
		int size;
		String text;

		String first = params.get(0);
		if (NumberUtils.isParsable(first) && params.size() > 1) {
			// If font size was provided
			size = params.getAsInteger(0);
			text = params.get(1);

		} else {
			// If font size was not provided
			Parameters params1 = new Parameters(1, event.getMessage().getContentRaw());
			size = 18;
			text = params1.get(0);

		}

		if (size > 100) {
			throw new CommandException("Please pick font size below **100**!", false);
		}

		if (text.length() > 300) {
			throw new CommandException("Please enter text shorter that **300 characters**!", false);
		}

		if (size <= 0) {
			throw new CommandException("Please pick font size above **0**!", false);
		}
		// Gets parameters

		try {
			Font font = new Font("Whitney", Font.PLAIN, size);

			Canvas c = new Canvas();
			FontMetrics fm = c.getFontMetrics(font);

			List<String> wrapped = StringUtils.wrap(text, fm, 4500);
			// Wraps given text and font

			int width = 0;
			for (String append : wrapped) {
				int advance = fm.stringWidth(append);

				if (width < advance) {
					width = advance;
				}
			}
			width += 5;
			// Determines width

			BufferedImage img = new BufferedImage(width, (wrapped.size() * font.getSize()) + 2 * 10,
			    BufferedImage.TYPE_INT_ARGB);
			// Creates BufferedImage

			Graphics2D g = img.createGraphics();
			// Creates Graphics2D

			g.setColor(new Color(0, 0, 0, 0));
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			// Fills in the background

			g.setFont(font);
			g.setColor(new Color(192, 193, 194));
			// Sets color and font for text

			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			// Allows anti aliasing

			int i = 1;
			for (String string : wrapped) {
				g.drawString(string, 5, i * font.getSize());

				i++;
			}
			// Writes on the image line per line

			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				ImageIO.write(img, "png", os);
				// Writes the image into an OutputStream

				event.getChannel().sendFile(os.toByteArray(), "tti.png").queue();
				// Sends the final result
			}

			g.dispose();
			// Disposes Graphics2D to save up resources

		} catch (OutOfMemoryError e) {
			throw new CommandException(
			    "Looks like this command container ran out of memory. " + "Please use less text/smaller font!", false);
		}
	}

	@Override
	public String getInfo() {
		return "Converts given text into an image. If no text size is provided, 18 will be used. "
		    + "Beware that this might not map all the 'special' characters correctly.";
	}

	@Override
	public String getName() {
		return "TextToImage";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITIES;
	}

	@Override
	public int getRatelimit() {
		return 30;
	}

	@Override
	public String[] getParameters() {
		return Commands.toArray("size (optional)", "text");
	}

	@Override
	public int getMinParameters() {
		return 1;
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("tti");
	}

}
