package gash.router.server.queue.management;

import io.netty.channel.Channel;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import routing.Pipe.CommandMessage;

public class QueueManager {
	protected static Logger logger = LoggerFactory.getLogger(QueueManager.class);
	protected static AtomicReference<QueueManager> instance = new AtomicReference<QueueManager>();
	
	protected LinkedBlockingDeque<InternalChannelNode> inboundCommandQueue;
	protected InboundQueueThread inboundCommmanderThread;
	
	
	public static QueueManager initManager() {
		instance.compareAndSet(null, new QueueManager());
		return instance.get();
	}

	public static QueueManager getInstance() {
		if (instance == null)
			instance.compareAndSet(null, new QueueManager());
		return instance.get();
	}
	
	public QueueManager() {
		logger.info(" Started the Manager ");

		inboundCommandQueue = new LinkedBlockingDeque<InternalChannelNode>();
		inboundCommmanderThread = new InboundQueueThread(this);
		inboundCommmanderThread.start();
	}
	
	public void enqueueInboundCommmand(CommandMessage message, Channel ch) {
		try {
			InternalChannelNode entry = new InternalChannelNode(message, ch);
			inboundCommandQueue.put(entry);
		} catch (InterruptedException e) {
			logger.error("message not enqueued for processing", e);
		}
	}
	
	public InternalChannelNode dequeueInboundCommmand() throws InterruptedException {
			return inboundCommandQueue.take();
	}
}
