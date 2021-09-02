package com.barcode.imageutil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;

import com.barcode.component.FileWrapper;
import com.barcode.component.WebFileWrapperImplementation;
import com.barcode.exceptions.UploadException;
import com.google.common.io.ByteStreams;

public class ImageUtility {

	private static final int BOTTOM_RIGHT = 3;
	private static final int LEFT_TOP = 5;
	private static final int RIGHT_TOP = 6;
	private static final int RIGHT_BOTTOM = 7;
	private static final int LEFT_BOTTOM = 8;

	private ImageUtility() {

	}

	public static Optional<byte[]> getScaledImage(FileWrapper thumbnail, int width, int height,
			boolean shouldSaveProportion) {
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
		int type = contentType.equals(MediaType.IMAGE_PNG_VALUE) ? BufferedImage.TYPE_INT_ARGB
				: BufferedImage.TYPE_INT_RGB;
		BufferedImage scaledBufferedImage = new BufferedImage(width, height, type);
		Graphics2D canvas = scaledBufferedImage.createGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		canvas.drawImage(bufferedImage, 0, 0, width, height, null);
		canvas.dispose();

		return scaledBufferedImage;
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

	public static File pngToJpg(WebFileWrapperImplementation file) {
		BufferedImage bufferedImage;
		File fileT = new File(getRoot() + "/temp/" + UUID.randomUUID().toString());
		try {

			// read image file
			bufferedImage = ImageIO.read(file.getInputStream());

			// create a blank, RGB, same width and height, and a white background
			BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

			// write to jpeg file
			ImageIO.write(newBufferedImage, "jpg", fileT);

		} catch (IOException e) {

			e.printStackTrace();

		}
		return fileT;
	}

	public static String getRoot() {
		return new File("/").getAbsolutePath();
	}

}
