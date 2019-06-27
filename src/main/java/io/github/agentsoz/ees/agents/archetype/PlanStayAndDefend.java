package io.github.agentsoz.ees.agents.archetype;

/*-
 * #%L
 * Emergency Evacuation Simulator
 * %%
 * Copyright (C) 2014 - 2018 by its authors. See AUTHORS file.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import io.github.agentsoz.ees.Constants;
import io.github.agentsoz.jill.lang.*;
import io.github.agentsoz.util.Location;

import java.util.Map;


@PlanInfo(postsGoals = {
		"io.github.agentsoz.ees.agents.archetype.GoalGoto",
})
public class PlanStayAndDefend extends Plan {

	ArchetypeAgent agent = null;
	Location xyNow;
	Location xyHome;

	public PlanStayAndDefend(Agent agent, Goal goal, String name) {
		super(agent, goal, name);
		this.agent = (ArchetypeAgent)agent;
		body = steps;
	}

	public boolean context() {
		setName(this.getClass().getSimpleName()); // give this plan a user friendly name for logging purposes
		boolean hasDependents = Boolean.valueOf(agent.getBelief(ArchetypeAgent.Beliefname.HasDependents.name()));
		boolean isExperienced = agent.getBelief(ArchetypeAgent.Beliefname.Archetype.name()).equals("ExperiencedIndependent");
		boolean applicable = isExperienced && !hasDependents;
		agent.out("thinks " + getFullName() + " is " + (applicable ? "" : "not ") + "applicable");
		return applicable;
	}

	PlanStep[] steps = {
			() -> {
				xyHome = agent.parseLocation(agent.getBelief(ArchetypeAgent.Beliefname.LocationHome.name()));
				xyNow = ((Location[])agent.getQueryPerceptInterface().queryPercept(
						String.valueOf(agent.getId()), Constants.REQUEST_LOCATION, null))[1];
				double distHome = Location.distanceBetween(xyNow,xyHome);
				boolean atHome = (distHome <= 0);
				if (!atHome) {
					// Go home first
					agent.out("will go home to " + xyHome + " #" + getFullName());
					subgoal(new GoalGoto(GoalGoto.class.getSimpleName(), xyHome));
					// subgoal should be last call in any plan step
				}
				else {
					agent.out("will stay and defend now #" + getFullName());
					this.drop(); // all done, drop the remaining plan steps
				}
			},
			() -> {
				// Check if we have arrived
				xyNow = ((Location[])agent.getQueryPerceptInterface().queryPercept(
						String.valueOf(agent.getId()), Constants.REQUEST_LOCATION, null))[1];
				double distHome = Location.distanceBetween(xyNow,xyHome);
				boolean reached = (distHome <= 0);
				if (reached) {
					agent.out("will stay and defend now #" + getFullName());
				} else {
					agent.out("is stuck at location " + xyNow + " #" + getFullName());
				}
			},
	};

	@Override
	public void setPlanVariables(Map<String, Object> vars) {
	}
}
