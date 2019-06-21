package io.github.agentsoz.ees;

/*-
 * #%L
 * Emergency Evacuation Simulator
 * %%
 * Copyright (C) 2014 - 2019 by its authors. See AUTHORS file.
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

import java.util.HashMap;

public class SNUpdates {

    private int agentId;
    private HashMap<String,Object[]> contentsMap; // content type, parameters
    private HashMap<String,Object[]> broadcastContentsMap; // contents that should be broadcasted to all agents in the social netwotrk.
    private HashMap<String,Object[]> snActions; // action type, parameters



    public SNUpdates(int id){
        this.agentId = id;
        this.contentsMap = new HashMap<>();
        this.broadcastContentsMap = new HashMap<>();
        this.snActions = new HashMap<>();
    }


    public HashMap<String, Object[]> getBroadcastContentsMap() {
        return broadcastContentsMap;
    }

    public HashMap<String, Object[]> getSNActionsMap() {
        return snActions;
    }

    public HashMap<String, Object[]> getContentsMap() {
        return contentsMap;
    }

    public int getAgentId() {
        return agentId;
    }

}