package gash.router.server;

import gash.router.container.RoutingConf;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.election.RaftElectionContext;
import gash.router.server.tasks.TaskList;

public class ServerState {
	private RoutingConf conf;
	private EdgeMonitor emon;
	private TaskList tasks;
	private RaftElectionContext electionCtx;

	public RoutingConf getConf() {
		return conf;
	}

	public void setConf(RoutingConf conf) {
		this.conf = conf;
	}

	public EdgeMonitor getEmon() {
		return emon;
	}

	public void setEmon(EdgeMonitor emon) {
		this.emon = emon;
	}

	public TaskList getTasks() {
		return tasks;
	}

	public void setTasks(TaskList tasks) {
		this.tasks = tasks;
	}

	public void setElectionCtx(RaftElectionContext ctx) {
		this.electionCtx = ctx;
	}
	
	public RaftElectionContext getElectionCtx(){
		return electionCtx;
	}

}
