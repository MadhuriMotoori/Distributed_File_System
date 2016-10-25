package gash.router.server.queue.management;

import gash.router.server.commandRouterHandlers.ReadRouterHandler;
import gash.router.server.commandRouterHandlers.WriteRouterHandler;
import io.netty.channel.Channel;

import java.rmi.UnexpectedException;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import routing.Pipe.CommandMessage;
import routing.Pipe.FileTask;


public class InboundQueueThread extends Thread {

	private QueueManager manager;
	private WriteRouterHandler writerRouter;
	private ReadRouterHandler readRouter;
	protected static Logger logger = LoggerFactory.getLogger(InboundQueueThread.class);

	public InboundQueueThread(QueueManager manager) {
		super();
		this.manager = manager;
		if (manager.inboundCommandQueue == null)
			throw new RuntimeException("Poller has a null queue");
		
		writerRouter = new WriteRouterHandler();
		readRouter = new ReadRouterHandler();
		
		readRouter.setNextChainHandler(writerRouter);
	}

	@Override
	public void run() {

		// Poll the queue for messages
		while (true) {
			try {
				readRouter.handleFileTask(manager.dequeueInboundCommmand());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}
