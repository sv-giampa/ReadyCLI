package examples.readycli.minishell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Forwards the data read from an input stream to an output stream.
 * 
 * @author Salvatore Giampa'
 *
 */
public class StreamCarrier {
	private InputStream in;
	private OutputStream out;
	private int bufferSize;
	private boolean pause = false;
	private Lock mutex = new ReentrantLock();
	private Condition pauseCondition = mutex.newCondition();

	private Thread thread = new Thread() {
		@Override
		public void run() {
			byte[] buffer = new byte[bufferSize];
			try {
				while (!Thread.currentThread()
						.isInterrupted()) {
					mutex.lock();
					try {
						while (pause)
							pauseCondition.await();
					} finally {
						mutex.unlock();
					}
					if (in.available() > 0) {
						int n = in.read(buffer);
						if (n == -1)
							break;
						out.write(buffer, 0, n);
						out.flush();
					} else {
						Thread.sleep(5);
						out.flush();
					}
				}
			} catch (InterruptedException e) {
				// interrupted
			} catch (IOException e) {
				// stream closed
			}
		}
	};

	/**
	 * Create a new {@link StreamCarrier} instance, as
	 * {@link StreamCarrier#StreamCarrier(InputStream, OutputStream, int)} does,
	 * using a default buffer size of 8192 bytes (8 Kib).
	 * 
	 * @param in         the input stream from which data will be read.
	 * @param out        the output stream to which data will be forwarded.
	 * @param bufferSize the size of the internal buffer, in bytes.
	 */
	public StreamCarrier(InputStream in, OutputStream out) {
		this(in, out, 8192);
	}

	/**
	 * Create a new {@link StreamCarrier} instance.
	 * 
	 * @param in         the input stream from which data will be read.
	 * @param out        the output stream to which data will be forwarded.
	 * @param bufferSize the size of the internal buffer, in bytes.
	 */
	public StreamCarrier(InputStream in, OutputStream out, int bufferSize) {
		super();
		this.in = in;
		this.out = out;
		this.bufferSize = bufferSize;
		thread.start();
	}

	/**
	 * Resume the stream carrying operation.
	 * 
	 * @throws IllegalStateException If the {@link #stop()} has been invoked.
	 */
	public void resume() throws IllegalStateException {
		if (thread == null)
			throw new IllegalStateException("the StreamCarrier has been stopped.");
		mutex.lock();
		try {
			pause = false;
			pauseCondition.signal();
		} finally {
			mutex.unlock();
		}
	}

	/**
	 * Pause the stream carrying operation.
	 * 
	 * @throws IllegalStateException If the {@link #stop()} has been invoked.
	 */
	public void pause() throws IllegalStateException {
		if (thread == null)
			throw new IllegalStateException("the StreamCarrier has been stopped.");
		mutex.lock();
		try {
			pause = true;
		} finally {
			mutex.unlock();
		}
	}

	/**
	 * Stop the stream carrying operation. After invoking this method, the
	 * {@link StreamCarrier} cannot be used anymore.
	 */
	public void stop() {
		thread.interrupt();
		thread = null;
	}
}