package gash.router.server.queue.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.commandmessage.routerhandlers.DeleteRouterHandler;
import gash.router.server.commandmessage.routerhandlers.ReadRouterHandler;
import gash.router.server.commandmessage.routerhandlers.UpdateRouterHandler;
import gash.router.server.commandmessage.routerhandlers.WriteRouterHandler;


public class InboundCommandQueueThread extends Thread {

	private QueueManager manager;
	private WriteRouterHandler writerRouter;
	private ReadRouterHandler readRouter;
	private DeleteRouterHandler deleteRouter;
	private UpdateRouterHandler updateRouter;
	protected static Logger logger = LoggerFactory.getLogger(InboundCommandQueueThread.class);

	public InboundCommandQueueThread(QueueManager manager) {
		super();
		this.manager = manager;
		if (manager.inboundCommandQueue == null)
			throw new RuntimeException("Manager has no inbound command queue");
		
		writerRouter = new WriteRouterHandler();
		readRouter = new ReadRouterHandler();
		deleteRouter = new DeleteRouterHandler();
		updateRouter = new UpdateRouterHandler();
		readRouter.setNextChainHandler(writerRouter);
		writerRouter.setNextChainHandler(deleteRouter);
		deleteRouter.setNextChainHandler(updateRouter);
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
