package com.kevin.spectralflux;

import java.awt.Color;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.io.Mpg123Decoder;

/**
 * Takes a plot and a decoder and plays back the audio form the decoder as well
 * as setting the marker in the plot accordingly.
 * 
 * @author mzechner
 * 
 */
public class PlaybackVisualizer {
	/**
	 * Consturctor, plays back the audio form the decoder and sets the marker of
	 * the plot accordingly. This will return when the playback is done.
	 * 
	 * @param plot
	 *            The plot.
	 * @param samplesPerPixel
	 *            the numbe of samples per pixel.
	 * @param decoder
	 *            The decoder.
	 * @throws Exception
	 */
	public PlaybackVisualizer(Plot plot, int samplesPerPixel,
			Mpg123Decoder decoder) throws Exception {
		AudioDevice device = Gdx.audio.newAudioDevice(decoder.getRate(),
				decoder.getChannels() == 1 ? true : false);
		float[] samples = new float[1024];

		long startTime = 0;
		while ((decoder.readSamples(samples, 0, samples.length)) > 0) {
			device.writeSamples(samples, 0, samples.length);
			if (startTime == 0)
				startTime = System.nanoTime();
			float elapsedTime = (System.nanoTime() - startTime) / 1000000000.0f;
			int position = (int) (elapsedTime * (44100 / samplesPerPixel));
			plot.setMarker(position, Color.white);
		}
	}
}
