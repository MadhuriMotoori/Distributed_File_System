/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.server.workmessage.handler.WorkElectionMessageHandler;
import gash.router.server.workmessage.handler.WorkFailureHandler;
import gash.router.server.workmessage.handler.WorkHeartBeatHandler;
import gash.router.server.workmessage.handler.IWorkChainHandler;
import gash.router.server.workmessage.handler.NewNodeChainHandlerV2;
import gash.router.server.workmessage.handler.WorkPingHandler;
import gash.router.server.workmessage.handler.WorkTaskHandler;
import gash.router.server.workmessage.handler.WorkStealHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common.Failure;
import pipe.work.Work.WorkMessage;

/**
 * The message handler processes json messages that are delimited by a 'newline'
 * 
 * TODO replace println with logging!
 * 
 * @author gash
 * 
 */
public class WorkHandler extends SimpleChannelInboundHandler<WorkMessage> {
	protected static Logger logger = LoggerFactory.getLogger(WorkHandler.class);
	protected ServerState state;
	protected boolean debug = false;

	IWorkChainHandler heartBeatChainHandler;
	IWorkChainHandler pingMessageChainHandler;
	IWorkChainHandler failureMessageChainHandler;
	IWorkChainHandler taskMessageChainHandler;
	IWorkChainHandler workStealMessageChainHandler;
	IWorkChainHandler electionMessageChainHandler;
	IWorkChainHandler newNodeChainHandler;
	public WorkHandler(ServerState state) {
		if (state != null) {
			this.state = state;
		} else {
			return;
		}
		this.heartBeatChainHandler = new WorkHeartBeatHandler();
		this.pingMessageChainHandler = new WorkPingHandler();
		this.failureMessageChainHandler = new WorkFailureHandler();
		this.taskMessageChainHandler = new WorkTaskHandler();
		this.workStealMessageChainHandler = new WorkStealHandler();
		this.electionMessageChainHandler = new WorkElectionMessageHandler();
		this.newNodeChainHandler = new NewNodeChainHandlerV2();

		this.heartBeatChainHandler.setNextChain(electionMessageChainHandler,state);
		this.electionMessageChainHandler.setNextChain(newNodeChainHandler,state);
		this.newNodeChainHandler.setNextChain(pingMessageChainHandler,state);		
		this.pingMessageChainHandler.setNextChain(failureMessageChainHandler,state);
		this.failureMessageChainHandler.setNextChain(workStealMessageChainHandler, state);
		this.workStealMessageChainHandler.setNextChain(taskMessageChainHandler, state);
	}

	/**
	 * override this method to provide processing behavior. T
	 * 
	 * @param msg
	 */
	public void handleMessage(WorkMessage msg, Channel channel) {
		if (msg == null) {
			logger.info("Error: Received empty WorkMessage: " + msg);
			System.out.println("Error: Received empty WorkMessage: " + msg);
			return;
		}

		if (debug)
			PrintUtil.printWork(msg);
		try {
			heartBeatChainHandler.handle(msg, channel);
		} catch (Exception e) {
			logger.debug("Error: error in handling heartBeat: " + e.getMessage());
			Failure.Builder eb = Failure.newBuilder();
			eb.setId(state.getConf().getNodeId());
			eb.setRefId(msg.getHeader().getNodeId());
			// changing e.getMessage to some string
			//eb.setMessage(e.getMessage());
			eb.setMessage("fixing the null pointer");
			WorkMessage.Builder rb = WorkMessage.newBuilder(msg);
			rb.setErr(eb);
			channel.write(rb.build());
		}

		System.out.flush();

	}

	/**
	 * a message was received from the server. Here we dispatch the message to
	 * the client's thread pool to minimize the time it takes to process other
	 * messages.
	 * 
	 * @param ctx
	 *            The channel the message was received from
	 * @param msg
	 *            The message
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WorkMessage msg) throws Exception {
		handleMessage(msg, ctx.channel());
	}

	/**
	 * 
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
	}

}