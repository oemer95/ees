package io.github.agentsoz.ees;

/*-
 * #%L
 * Emergency Evacuation Simulator
 * %%
 * Copyright (C) 2014 - 2021 by its authors. See AUTHORS file.
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

public class DiffusionContent {



    // data structures accessed by the Diffusion model
    HashMap<String,Object[]> contentsMapFromDiffusionModel;  // content type, parameters

    // data structures accessed by the BDI model
    private HashMap<String,Object[]> contentsMapFromBDIModel; // content type, parameters
    private HashMap<String,Object[]> broadcastContentsMapFromBDIModel; // contents that should be broadcasted to all agents in the social netwotrk.
    private HashMap<String,Object[]> snActionsMapFromBDIModel; // action type, parameters


    public DiffusionContent()
    {

        this.contentsMapFromDiffusionModel = new HashMap<>();
        this.contentsMapFromBDIModel = new HashMap<>();
        this.broadcastContentsMapFromBDIModel = new HashMap<>();
        this.snActionsMapFromBDIModel = new HashMap<>();
    }


    public HashMap<String, Object[]> getContentsMapFromDiffusionModel() {
        return contentsMapFromDiffusionModel;
    }

    public HashMap<String, Object[]> getSnActionsMapFromBDIModel() {
        return snActionsMapFromBDIModel;
    }

    public HashMap<String, Object[]> getContentsMapFromBDIModel() {
        return contentsMapFromBDIModel;
    }

    public HashMap<String, Object[]> getBroadcastContentsMapFromBDIModel() {
        return broadcastContentsMapFromBDIModel;
    }

    public boolean isEmpty(){
        return contentsMapFromDiffusionModel.isEmpty();
    }

    public String toString() {
        return contentsMapFromDiffusionModel.toString();
    }
}
