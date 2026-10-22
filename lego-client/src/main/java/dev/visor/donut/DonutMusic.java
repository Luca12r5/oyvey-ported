package dev.visor.donut;

import com.mojang.blaze3d.audio.Channel;
import dev.visor.VisorClient;
import dev.visor.mixin.ChannelAccessor;
import dev.visor.mixin.SoundEngineAccessor;
import dev.visor.mixin.SoundManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

/**
 * Doctor Donut's song. The dance follows the song's real playback position (asked from OpenAL on
 * the sound thread), so moves and music stay together through pauses, lag and loops.
 */
final class DonutMusic {
	private static final SoundEvent SONG = SoundEvent.createVariableRangeEvent(VisorClient.id("doctor_donut.dance"));
	private static final long RETRY_NANOS = 2_000_000_000L;

	private record Cursor(double seconds, long nanos, boolean playing) {
	}

	private final double duration;
	private volatile @Nullable Cursor cursor;
	private volatile long generation;
	private volatile boolean pollPending;
	private @Nullable Song sound;
	private double clock;
	private long retryAt;
	private long lastNanos;

	DonutMusic(double duration) {
		this.duration = duration;
	}

	/**
	 * Keeps the song playing (or silent) and returns the position in it, in seconds. Without sound
	 * the position simply follows the clock, so the dance never stops.
	 */
	double update(boolean audible, float volume) {
		Minecraft minecraft = Minecraft.getInstance();
		long now = System.nanoTime();
		if (this.lastNanos != 0L && !minecraft.isPaused()) {
			this.clock += Math.max(0.0, (now - this.lastNanos) / 1.0E9);
		}
		this.lastNanos = now;
		SoundManager manager = minecraft.getSoundManager();
		if (this.sound != null && (!audible || !manager.isActive(this.sound))) {
			boolean ended = audible;
			this.stopSound();
			if (ended) {
				this.retryAt = now + RETRY_NANOS;
			}
		}
		if (audible && this.sound == null && now >= this.retryAt && !minecraft.isPaused()) {
			Song song = new Song(volume);
			if (manager.play(song) == SoundEngine.PlayResult.NOT_STARTED) {
				// Muted (Jukebox/Note Blocks at 0) or no audio device: try again later.
				this.retryAt = now + RETRY_NANOS;
			} else {
				this.sound = song;
				this.cursor = null;
				this.clock = 0.0;
			}
		}
		if (this.sound != null) {
			this.sound.volume(volume);
			this.pollCursor(manager);
			Cursor current = this.cursor;
			if (current != null) {
				double ahead = current.playing() && !minecraft.isPaused() ? Math.min(0.06, Math.max(0.0, (now - current.nanos()) / 1.0E9)) : 0.0;
				this.clock = current.seconds() + ahead;
			}
		}
		return this.clock % this.duration;
	}

	private void pollCursor(SoundManager manager) {
		if (this.pollPending || this.sound == null) {
			return;
		}
		SoundEngine engine = ((SoundManagerAccessor) manager).visor$soundEngine();
		ChannelAccess.ChannelHandle handle = ((SoundEngineAccessor) engine).visor$instanceToChannel().get(this.sound);
		if (handle == null || handle.isStopped()) {
			return;
		}
		long token = this.generation;
		this.pollPending = true;
		handle.execute(channel -> {
			try {
				this.read(channel, token);
			} finally {
				if (token == this.generation) {
					this.pollPending = false;
				}
			}
		});
	}

	/** Runs on the sound thread. */
	private void read(Channel channel, long token) {
		int source = ((ChannelAccessor) channel).visor$source();
		boolean playing = channel.playing();
		double seconds = AL10.alGetSourcef(source, AL11.AL_SEC_OFFSET);
		if (token == this.generation && Double.isFinite(seconds) && (seconds > 0.0 || playing)) {
			this.cursor = new Cursor(seconds, System.nanoTime(), playing);
		}
	}

	private void stopSound() {
		this.generation++;
		if (this.sound != null) {
			Minecraft.getInstance().getSoundManager().stop(this.sound);
		}
		this.sound = null;
		this.cursor = null;
		this.pollPending = false;
	}

	/** Stops the song; the next {@link #update} starts it again from the beginning. */
	void stop() {
		this.stopSound();
		this.clock = 0.0;
		this.lastNanos = 0L;
		this.retryAt = 0L;
	}

	/** Looping, non-positional song whose volume follows the setting while it plays. */
	private static final class Song extends AbstractTickableSoundInstance {
		Song(float volume) {
			super(SONG, SoundSource.RECORDS, RandomSource.create());
			this.looping = true;
			this.delay = 0;
			this.relative = true;
			this.attenuation = SoundInstance.Attenuation.NONE;
			this.volume = volume;
		}

		void volume(float volume) {
			this.volume = volume;
		}

		@Override
		public void tick() {
		}
	}
}
