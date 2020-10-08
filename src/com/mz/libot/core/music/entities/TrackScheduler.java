package com.mz.libot.core.music.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.mz.libot.core.music.entities.exceptions.QueueFullException;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter implements Iterable<AudioTrack> {

	public static final int QUEUE_MAX_SIZE = 200;

	private final AudioPlayer player;
	private Queue<AudioTrack> queue;
	private volatile boolean loop;

	/**
	 * @param player
	 *            The audio player this scheduler uses
	 */
	public TrackScheduler(AudioPlayer player) {
		this.player = player;
		this.queue = new ArrayBlockingQueue<>(QUEUE_MAX_SIZE);
		this.loop = false;

	}

	/**
	 * Resumes the AudioPlayer if it's paused.
	 */
	public void resumePlayer() {
		if (this.player.isPaused())
			this.player.setPaused(false);
	}

	/**
	 * Queues a track or plays it if nothing is already playing.
	 *
	 * @param track
	 *            The track to play or add to queue.
	 *
	 * @return true if track was successfully queued, false if not
	 *
	 * @throws QueueFullException
	 *             in case the queue would exceed {@link #QUEUE_MAX_SIZE} elements.
	 */
	public boolean queue(AudioTrack track) throws QueueFullException {
		return queue(track, true);
	}

	/**
	 * Queues a track or plays it if nothing is already playing.
	 *
	 * @param track
	 *            The track to play or add to queue.
	 * @param resume
	 *            whether to resume the player (if it's paused)
	 *
	 * @return true if the track has started playing
	 *
	 * @throws QueueFullException
	 *             in case the queue would exceed {@link #QUEUE_MAX_SIZE} elements.
	 */
	public boolean queue(AudioTrack track, boolean resume) throws QueueFullException {
		if (!this.player.startTrack(track, true)) {
			if (!this.queue.offer(track)) {
				throw new QueueFullException();
			}

			return false;
		}

		if (resume)
			resumePlayer();

		return true;
	}

	/**
	 * Skips to the next track.
	 *
	 * @return the next audio track
	 */
	public AudioTrack nextTrack() {
		return skipTrack(1);
	}

	/**
	 * Skips to the n-th track.
	 *
	 * @param n
	 *            the amount of tracks to skip
	 *
	 * @return the next audio track
	 */
	public AudioTrack skipTrack(int n) {
		for (int i = 0; i < n - 1; i++)
			this.queue.poll();

		AudioTrack track = this.queue.poll();
		resumePlayer();
		this.player.startTrack(track, false);
		return track;
	}

	/**
	 * Shuffles the queue
	 */
	public void shuffle() {
		List<AudioTrack> queueList = new ArrayList<>(this.queue);
		Collections.shuffle(queueList);
		clear();
		this.queue.addAll(queueList);
	}

	/**
	 * Clears the queue.
	 */
	public void clear() {
		this.queue.clear();
	}

	/**
	 * @return size of this queue
	 */
	public int size() {
		return this.queue.size();
	}

	/**
	 * @return true if this {@link TrackScheduler} is looping the current track
	 */
	public boolean isLoop() {
		return this.loop;
	}

	/**
	 * @param loop
	 *            whether to loop the current track
	 */
	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			if (isLoop() && track.isSeekable()) {
				AudioTrack current = track.makeClone();
				current.setPosition(0);
				// Seeks the track

				player.startTrack(current, false);
				// Plays the track

				return;
			}

			nextTrack();
		}
	}

	@Override
	public Iterator<AudioTrack> iterator() {
		return this.queue.iterator();
	}

	public List<AudioTrack> getTracks() {
		return Collections.unmodifiableList(new ArrayList<>(this.queue));
	}
}
