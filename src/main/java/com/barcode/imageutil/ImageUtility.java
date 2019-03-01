package com.barcode.imageutil;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.springframework.http.MediaType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.front.component.FileWrapper;
import com.front.exceptions.UploadException;
import com.google.common.io.ByteStreams;

public class ImageUtility {
	
	private static final int BOTTOM_RIGHT = 3;
	private static final int LEFT_TOP = 5;
	private static final int RIGHT_TOP = 6;
	private static final int RIGHT_BOTTOM = 7;
	private static final int LEFT_BOTTOM = 8;
	
	private ImageUtility() {
		
	}
	
	public static byte[] rotateImage(FileWrapper imageFile, int angle) {
		try {
			try (InputStream inputStream = imageFile.getInputStream()) {
				BufferedImage bufferedImage = ImageIO.read(inputStream);
				bufferedImage = getInvertedImage(bufferedImage, angle);
				return toByteArray(bufferedImage);
			}
			
		} catch (IOException exception) {
			exception.printStackTrace();
			throw new RuntimeException(exception);
		}
	}
	
	public static BufferedImage autoRotateImage(byte[] image) throws IOException {
		try (InputStream inputStream = new ByteArrayInputStream(image)) {
			BufferedImage bufferedImage = ImageIO.read(inputStream);
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
				Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
				if (imageReaders.hasNext()) {
					ImageReader imageReader = imageReaders.next();
					imageReader.setInput(imageInputStream, true);
					IIOMetadata metadata = imageReader.getImageMetadata(0);
					if (metadata != null) {
						Node root = metadata.getAsTree(metadata.getNativeMetadataFormatName());
						byte[] exifData = null;
						for (Node node : getNodes(root, new ArrayList<Node>(), "unknown")) {
							Node temporaryNode = node.getAttributes().item(0);
							if (temporaryNode.getNodeValue().equals("225")) {
								exifData = (byte[]) ((IIOMetadataNode) node).getUserObject();
								break;
							}
						}
						if (exifData != null) {
							int result;
							try {
								result = getOrientation(exifData);
							} catch (IllegalArgumentException exception) {
								exception.printStackTrace();
								result = -1;
							}
							switch (result) {
							case BOTTOM_RIGHT:
								bufferedImage = getInvertedImage(bufferedImage, 180);
								break;
							case LEFT_TOP:
								bufferedImage = getInvertedImage(bufferedImage, 90);
								break;
							case RIGHT_TOP:
								bufferedImage = getInvertedImage(bufferedImage, 90);
								break;
							case RIGHT_BOTTOM:
								bufferedImage = getInvertedImage(bufferedImage, 270);
								break;
							case LEFT_BOTTOM:
								bufferedImage = getInvertedImage(bufferedImage, 270);
								break;
							}
						}
					}
				}
			}
			return bufferedImage;
		}
	}

	private static ArrayList<Node> getNodes(Node parent, ArrayList<Node> nodes, String description) {
		if (parent == null)
			return nodes;
		if (parent.getNodeName().equals(description))
			nodes.add(parent);
		if (parent.hasAttributes()) {
			NamedNodeMap map = parent.getAttributes();
			for (int i = 0; i < map.getLength(); i++)
				nodes = getNodes(map.item(i), nodes, description);
		}
		Node child = parent.getFirstChild();
		while (child != null) {
			nodes = getNodes(child, nodes, description);
			child = child.getNextSibling();
		}
		return nodes;
	}

	private static int getOrientation(byte[] exifData) throws IllegalArgumentException {
		ByteBuffer buffer = ByteBuffer.wrap(exifData);

		if (buffer.getInt() != 0x45786966 || buffer.getShort() != 0x0000)
			throw new IllegalArgumentException("Invalid EXIF Header");

		buffer = buffer.slice();

		switch (buffer.getShort()) {
		case 0x4949:
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			break;
		case 0x4D4D:
			buffer.order(ByteOrder.BIG_ENDIAN);
			break;
		default:
			throw new IllegalArgumentException("Invalid Byte Order");
		}

		if (buffer.getShort() != 0x002A)
			throw new IllegalArgumentException("Invalid TIFF Header");

		buffer.position(buffer.getInt());

		int quantity = buffer.getShort() & 0xFFFF;
		for (int i = 0; i < quantity; i++) {
			if (buffer.getShort() == 0x112) {
				buffer.position(buffer.position() + 6);
				return buffer.getShort() & 0xFF;
			} else
				buffer.position(buffer.position() + 10);
		}
		return -1;
	}

	private static BufferedImage getInvertedImage(BufferedImage bufferedImage, int angle) {
		double sine = Math.abs(Math.sin(Math.toRadians(angle)));
		double cosine = Math.abs(Math.cos(Math.toRadians(angle)));
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		int newWidth = (int) Math.floor(width * cosine + height * sine);
		int newHeight = (int) Math.floor(height * cosine + width * sine);

		BufferedImage invertedImage = new BufferedImage(newWidth, newHeight, bufferedImage.getType());
		Graphics2D canvas = invertedImage.createGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		canvas.translate((newWidth - width) / 2, (newHeight - height) / 2);
		canvas.rotate(Math.toRadians(angle), width / 2, height / 2);
		canvas.drawRenderedImage(bufferedImage, null);
		canvas.dispose();

		return invertedImage;
	}
	public static Optional<byte[]> getScaledImage(FileWrapper thumbnail, int width, int height, boolean shouldSaveProportion) {
		byte[] thumbnailAsByteArray = null;
		String contentType = thumbnail.getContentType();
		if (contentType.contains("image")) {
			try (InputStream inputStream = thumbnail.getInputStream()) {
				BufferedImage bufferedImage = ImageIO.read(inputStream);
				// default player width = 480, default player height = 270
				if (bufferedImage.getWidth() > width || bufferedImage.getHeight() > height) {
					bufferedImage = getScaledImage(bufferedImage, contentType, width, height, shouldSaveProportion);
					thumbnailAsByteArray = toByteArray(bufferedImage);
				} else {
					thumbnailAsByteArray = ByteStreams.toByteArray(thumbnail.getInputStream());
				}
			} catch (IOException exception) {
				throw new UploadException("Cannot upload an image!", exception);
			}
		}
		return Optional.ofNullable(thumbnailAsByteArray);
	}
	
	public static Optional<byte[]> getGreyscaleImage(FileWrapper image) {
		byte[] thumbnailAsByteArray = null;
		String contentType = image.getContentType();
		if (contentType.contains("image")) {
			try (InputStream inputStream = image.getInputStream()) {
				BufferedImage bufferedImage = ImageIO.read(inputStream);
				BufferedImage result = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
						bufferedImage.getType());
				ColorConvertOp operation = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
				operation.filter(bufferedImage, result);
				return Optional.of(toByteArray(result));
			} catch (IOException exception) {
				throw new UploadException("Cannot upload an image!", exception);
			}
		}
		return Optional.ofNullable(thumbnailAsByteArray);
	}
	
	public static Optional<byte[]> getSepiaImage(FileWrapper image) {
		byte[] thumbnailAsByteArray = null;
		String contentType = image.getContentType();
		if (contentType.contains("image")) {
			try (InputStream inputStream = image.getInputStream()) {
				BufferedImage bufferedImage = ImageIO.read(inputStream);
				BufferedImage result = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
						bufferedImage.getType());

				int sepiaDepth = 20;

				int w = bufferedImage.getWidth();
				int h = bufferedImage.getHeight();

				int[] pixels = new int[w * h * 3];
				bufferedImage.getRaster().getPixels(0, 0, w, h, pixels);

				for (int x = 0; x < bufferedImage.getWidth(); x++) {
					for (int y = 0; y < bufferedImage.getHeight(); y++) {
						int rgb = bufferedImage.getRGB(x, y);
						Color color = new Color(rgb, true);
						int r = color.getRed();
						int g = color.getGreen();
						int b = color.getBlue();
						int gry = (r + g + b) / 3;

						r = g = b = gry;
						r = r + (sepiaDepth * 2);
						g = g + sepiaDepth;

						if (r > 255) {
							r = 255;
						}
						if (g > 255) {
							g = 255;
						}
						if (b > 255) {
							b = 255;
						}

						b -= 10;

						if (b < 0) {
							b = 0;
						}
						if (b > 255) {
							b = 255;
						}

						color = new Color(r, g, b, color.getAlpha());
						result.setRGB(x, y, color.getRGB());
					}
				}
				return Optional.of(toByteArray(result));
			} catch (IOException exception) {
				throw new UploadException("Cannot upload an image!", exception);
			}
		}
		return Optional.ofNullable(thumbnailAsByteArray);
	}
	

	public static Optional<byte[]> getScaledImage(byte[] imageAsByteArray, String contentType, int width, int height,
			boolean shouldSaveProportion) {
		try (InputStream inputStream = new ByteArrayInputStream(imageAsByteArray)) {
			BufferedImage bufferedImage = ImageIO.read(inputStream);
			if (bufferedImage.getWidth() > width || bufferedImage.getHeight() > height) {
				bufferedImage = getScaledImage(bufferedImage, contentType, width, height, shouldSaveProportion);
				return Optional.of(toByteArray(bufferedImage));
			} else {
				return Optional.of(imageAsByteArray);
			}
		} catch (IOException exception) {
			throw new UploadException("Cannot upload an image!", exception);
		}
	}

	public static BufferedImage getScaledImage(BufferedImage bufferedImage, String contentType, int width, int height,
			boolean shouldSaveProportion) {
		if (shouldSaveProportion) {
			double rate;
			if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
				rate = ((double) bufferedImage.getHeight() / (double) bufferedImage.getWidth());
				height = (int) (width * rate);
			} else {
				rate = ((double) bufferedImage.getWidth() / (double) bufferedImage.getHeight());
				width = (int) (height * rate);
			}
		}
		if (width == 0 || height == 0) {
			width = bufferedImage.getWidth();
			height = bufferedImage.getHeight();
		}
		int type = contentType.equals(MediaType.IMAGE_PNG_VALUE) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage scaledBufferedImage = new BufferedImage(width, height, type);
		Graphics2D canvas = scaledBufferedImage.createGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		canvas.drawImage(bufferedImage, 0, 0, width, height, null);
		canvas.dispose();

		return scaledBufferedImage;
	}
	
	public static BufferedImage addSignOnImage(BufferedImage bufferedImage, Position position, Color color,
			int fontSize, String sign) {
		BufferedImage signedBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
				bufferedImage.getType());
		Graphics2D canvas = signedBufferedImage.createGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		canvas.drawImage(bufferedImage, 0, 0, null);
		canvas.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
		canvas.setPaint(color);
		canvas.setFont(new Font("Serif", Font.PLAIN, fontSize));

		Rectangle2D stringBounds = canvas.getFont().getStringBounds(sign, canvas.getFontRenderContext());
		int width = signedBufferedImage.getWidth();
		int height = signedBufferedImage.getHeight();
		double x = 0;
		double y = 0;
		if (position == Position.UPPER_LEFT_CORNER) {
			x = 0;
			y = 0;
		} else if (position == Position.UPPER_RIGHT_CORNER) {
			x = width - stringBounds.getWidth();
			y = 0;
		} else if (position == Position.CENTER) {
			x = (width - stringBounds.getWidth()) / 2;
			y = (height - stringBounds.getHeight()) / 2;
		} else if (position == Position.LOWER_LEFT_CORNER) {
			x = 0;
			y = height - stringBounds.getHeight();
		} else if (position == Position.LOWER_RIGHT_CORNER) {
			x = width - stringBounds.getWidth();
			y = height - stringBounds.getHeight();
		}
		double ascent = -stringBounds.getY();
		double baseline = y + ascent;

		canvas.drawString(sign, (int) x, (int) baseline);
		canvas.dispose();

		return signedBufferedImage;
	}

	
	public static byte[] toByteArray(BufferedImage bufferedImage) throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		}
	}
	
	public enum Position {
		UPPER_LEFT_CORNER, UPPER_RIGHT_CORNER, CENTER, LOWER_LEFT_CORNER, LOWER_RIGHT_CORNER
	}

	public static byte[] addSignOnImage(FileWrapper image, Position position, Color color, String sign) {
		try {
			BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
			BufferedImage signedBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
					bufferedImage.getType());
			Graphics2D canvas = signedBufferedImage.createGraphics();
			canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			canvas.drawImage(bufferedImage, 0, 0, null);
			canvas.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
			canvas.setPaint(color);
			canvas.setFont(new Font("Serif", Font.PLAIN, 32));
	
			Rectangle2D stringBounds = canvas.getFont().getStringBounds(sign, canvas.getFontRenderContext());
			int width = signedBufferedImage.getWidth();
			int height = signedBufferedImage.getHeight();
			double x = 0;
			double y = 0;
			if (position == Position.UPPER_LEFT_CORNER) {
				x = 0;
				y = 0;
			} else if (position == Position.UPPER_RIGHT_CORNER) {
				x = width - stringBounds.getWidth();
				y = 0;
			} else if (position == Position.CENTER) {
				x = (width - stringBounds.getWidth()) / 2;
				y = (height - stringBounds.getHeight()) / 2;
			} else if (position == Position.LOWER_LEFT_CORNER) {
				x = 0;
				y = height - stringBounds.getHeight();
			} else if (position == Position.LOWER_RIGHT_CORNER) {
				x = width - stringBounds.getWidth();
				y = height - stringBounds.getHeight();
			}
			double ascent = -stringBounds.getY();
			double baseline = y + ascent;
	
			canvas.drawString(sign, (int) x, (int) baseline);
			canvas.dispose();
			return toByteArray(signedBufferedImage);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

}
