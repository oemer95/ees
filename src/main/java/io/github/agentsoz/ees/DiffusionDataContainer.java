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

public class DiffusionDataContainer {

    HashMap<String,DiffusionContent> diffusionDataMap; // agent id, Diffusion content

    public DiffusionDataContainer(){
         this.diffusionDataMap = new HashMap<>();
    }

    public HashMap<String, DiffusionContent> getDiffusionDataMap() {
        return diffusionDataMap;
    }


    public void putContentToContentsMapFromDiffusionModel(String agentId, String contentType, Object[] params ){

        DiffusionContent dc = getOrCreateDiffusionContent(agentId);
        dc.getContentsMapFromDiffusionModel().put(contentType,params);
    }

//    public void putContentToContentsMapFromBDIModel(String agentId, String contentType, Object[] params ){
//
//        DiffusionContent dc = getOrCreateDiffusionContent(agentId);
//        dc.getContentsMapFromBDIModel().put(contentType,params);
//    }
//
//    public void putContentToBroadcastContentsMapFromBDIModel(String agentId, String contentType, Object[] params ){
//
//        DiffusionContent dc = getOrCreateDiffusionContent(agentId);
//        dc.getBroadcastContentsMapFromBDIModel().put(contentType,params);
//    }
//
//    public void putActionToActionMapFromBDIModel(String agentId, String contentType, Object[] params ){
//
//        DiffusionContent dc = getOrCreateDiffusionContent(agentId);
//        dc.getSnActionsMapFromBDIModel().put(contentType,params);
//    }


    private DiffusionContent getOrCreateDiffusionContent(String agentId) {
        DiffusionContent content = this.getDiffusionDataMap().get(agentId);
        if (content == null) {
            content = new DiffusionContent();
            this.getDiffusionDataMap().put(agentId, content);
        }

        return content;
    }

//    public void clearDiffusionModelData(){
//
//        for (DiffusionContent dc : diffusionDataMap.values()){
//            dc.getContentsMapFromDiffusionModel().clear();
//        }
//    }
//
//    public void clearBDIModelData(){
//
//        for (DiffusionContent dc : diffusionDataMap.values()){
//            dc.getSnActionsMapFromBDIModel().clear();
//            dc.getContentsMapFromBDIModel().clear();
//            dc.getBroadcastContentsMapFromBDIModel().clear();
//        }
//    }


}
