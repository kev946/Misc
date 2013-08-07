package com.kevin.spectralflux;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.analysis.KissFFT;
import com.badlogic.gdx.audio.io.Mpg123Decoder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SpectralFlux implements ApplicationListener {
	public static final String FILE = "C:/Users/Kevin/Desktop/jazz.mp3";
	public static final int THRESHOLD_WINDOW_SIZE = 10;
	public static final float MULTIPLIER = 1.5f;

	private OrthographicCamera camera;
	private SpriteBatch batch;
	Mpg123Decoder decoder;
	AudioDevice device;
	KissFFT fft;

	short[] samples = new short[1024];
	float[] spectrum = new float[1024 / 2 + 1];
	float[] lastSpectrum = new float[1024 / 2 + 1];
	List<Float> spectralFlux = new ArrayList<Float>();
	List<Float> threshold = new ArrayList<Float>();
	List<Float> prunnedSpectralFlux = new ArrayList<Float>();
	List<Float> peaks = new ArrayList<Float>();

	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		camera = new OrthographicCamera(1, h / w);
		batch = new SpriteBatch();

		fft = new KissFFT(1024);
		FileHandle musicFile = Gdx.files.absolute(FILE);
		decoder = new Mpg123Decoder(musicFile);
		device = Gdx.audio.newAudioDevice(decoder.getRate(),
				decoder.getChannels() == 1 ? true : false);
		
		// Decode mp3 to PCM data
		while (decoder.readSamples(samples, 0, samples.length) > 0) {

			// Hamming window on PCM data
			for (int i = 0; i < samples.length; i++) {
				samples[i] *= (0.54f - 0.46f * Math.cos((2 * Math.PI) * i
						/ (samples.length - 1)));
			}

			// FFT on smoothened PCM data
			System.arraycopy(spectrum, 0, lastSpectrum, 0, spectrum.length);
			fft.spectrum(samples, spectrum);

			// Spectral Flux
			float flux = 0;
			for (int i = 0; i < spectrum.length; i++) {
				float value = (spectrum[i] - lastSpectrum[i]);
				flux += value < 0 ? 0 : value;
			}
			spectralFlux.add(flux);
		}

		// Threshold Function
		for (int i = 0; i < spectralFlux.size(); i++) {
			int start = Math.max(0, i - THRESHOLD_WINDOW_SIZE);
			int end = Math.min(spectralFlux.size() - 1, i
					+ THRESHOLD_WINDOW_SIZE);
			float mean = 0;
			for (int j = start; j <= end; j++)
				mean += spectralFlux.get(j);
			mean /= (end - start);
			threshold.add(mean * MULTIPLIER);
		}

		// "Prune" the Spectral Flux
		for (int i = 0; i < threshold.size(); i++) {
			if (threshold.get(i) <= spectralFlux.get(i))
				prunnedSpectralFlux.add(spectralFlux.get(i) - threshold.get(i));
			else
				prunnedSpectralFlux.add((float) 0);
		}

		// Take peaks of prunned spectral flux
		for (int i = 0; i < prunnedSpectralFlux.size() - 1; i++) {
			if (prunnedSpectralFlux.get(i) > prunnedSpectralFlux.get(i + 1))
				peaks.add(prunnedSpectralFlux.get(i));
			else
				peaks.add((float) 0);
		}
		
		Plot plot = new Plot( "Spectral Flux", 1024, 512 );
		plot.plot( peaks, 1, Color.red );	
		
		try {
			new PlaybackVisualizer(plot, 512, new Mpg123Decoder(musicFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void dispose() {
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
